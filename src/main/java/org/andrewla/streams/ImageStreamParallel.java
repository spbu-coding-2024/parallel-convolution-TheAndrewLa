package org.andrewla.streams;

import org.andrewla.ImageManager;
import org.andrewla.ImageProcessor;
import org.andrewla.ImageStream;
import org.andrewla.streams.concurrent.IndexedWorker;
import org.andrewla.streams.concurrent.Task;
import org.andrewla.streams.concurrent.TaskQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageStreamParallel extends ImageStream implements AutoCloseable {
    private static final int NO_LIMIT_CAPACITY = -1;

    private final int capacity;
    private final ExecutorService executor;
    private final ExecutorService processExecutor;

    public ImageStreamParallel(ImageProcessor baseProcessor, ImageManager mgr, int threads) {
        this(baseProcessor, mgr, threads, NO_LIMIT_CAPACITY);
    }

    public ImageStreamParallel(ImageProcessor baseProcessor, ImageManager mgr, int threads, int capacity) {
        super(baseProcessor, mgr);

        if (threads < 2) {
            throw new IllegalArgumentException("Thread counts must be greater than 2 (at least 1 reader, 1 writer)");
        }

        this.capacity = capacity;

        this.executor = Executors.newFixedThreadPool(threads);
        this.processExecutor = Executors.newCachedThreadPool();
    }

    private static void waitTasks(List<Future<?>> futures) {
        for (final var future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void readProcessWrite() {
        if (inputFiles.isEmpty()) {
            return;
        }

        final var size = (capacity < 0) ? filesCount : (capacity - 1);
        final var readQueue = new TaskQueue(size);
        final var writeQueue = new TaskQueue(size);

        final var readFutures = new ArrayList<Future<?>>();
        final var readWorker = new IndexedWorker(filesCount);
        for (int i = 0; i < filesCount; i++) {
            readFutures.add(executor.submit(readFunc(readQueue, readWorker)));
        }

        final var processWorker = new IndexedWorker(filesCount);
        final var processFuture = processExecutor.submit(processFunc(readQueue, writeQueue, processWorker));

        final var writeFutures = new ArrayList<Future<?>>();
        for (int i = 0; i < filesCount; i++) {
            writeFutures.add(executor.submit(writeFunc(writeQueue)));
        }

        waitTasks(readFutures);
        waitTasks(List.of(processFuture));
        waitTasks(writeFutures);
    }

    private Runnable readFunc(TaskQueue readQueue, IndexedWorker worker) {
        return () -> {
            try {
                worker.start();
                while (true) {
                    final var fileIndex = worker.newTask();
                    if (fileIndex == IndexedWorker.NO_TASK) {
                        break;
                    }
                    final var path = inputFiles.get(fileIndex);
                    final var image = mgr.readImage(path);
                    readQueue.put(new Task(image, fileIndex));
                }
                final var last = worker.finishAndCheck();
                if (last) {
                    readQueue.mark();
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable processFunc(TaskQueue readQueue, TaskQueue writeQueue, IndexedWorker worker) {
        return () -> {
            try {
                worker.start();
                while (true) {
                    final var task = readQueue.take();
                    if (Task.isSentinel(task)) {
                        break;
                    }
                    final var image = mgr.copyImage(task.image());
                    processor.process(task.image(), image);
                    writeQueue.put(new Task(image, task.index()));
                }
                final var last = worker.finishAndCheck();
                if (last) {
                    writeQueue.mark();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable writeFunc(TaskQueue writeQueue) {
        return () -> {
            try {
                while (true) {
                    final var task = writeQueue.take();
                    if (Task.isSentinel(task)) {
                        break;
                    }
                    final var path = outputFiles.get(task.index());
                    mgr.writeImage(task.image(), path);
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public void close() {
        processExecutor.shutdown();
        executor.shutdown();
    }
}
