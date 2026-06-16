package org.andrewla.streams;

import org.andrewla.Image;
import org.andrewla.ImageManager;
import org.andrewla.ImageProcessor;
import org.andrewla.ImageStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageStreamParallel extends ImageStream implements AutoCloseable {
    private static final Task SENTINEL_TASK = new Task(null, null);

    private static final int DEFAULT_READERS_NUM = 3;
    private static final int DEFAULT_WRITERS_NUM = 3;

    private final Thread[] readers;
    private final Thread[] writers;
    private Thread proc;

    public ImageStreamParallel(ImageProcessor baseProcessor, ImageManager mgr) {
        this(baseProcessor, mgr, DEFAULT_READERS_NUM, DEFAULT_WRITERS_NUM);
    }

    public ImageStreamParallel(ImageProcessor baseProcessor, ImageManager mgr, int numReaders, int numWriters) {
        super(baseProcessor, mgr);

        if (numReaders < 1 || numWriters < 1) {
            throw new IllegalArgumentException("Thread counts must be positive");
        }

        readers = new Thread[numReaders];
        writers = new Thread[numWriters];
    }

    @Override
    public void readProcessWrite() {
        if (inputFiles.isEmpty()) {
            return;
        }

        final var readQueue = new ArrayBlockingQueue<Task>(count);
        final var writeQueue = new ArrayBlockingQueue<Task>(count);

        final var readWork = readWork(readQueue);
        final var writeWork = writeWork(writeQueue);

        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(readWork);
            readers[i].start();
        }

        proc = new Thread(processWork(readQueue, writeQueue));
        proc.start();

        for (int i = 0; i < writers.length; i++) {
            writers[i] = new Thread(writeWork);
            writers[i].start();
        }
    }

    private Runnable readWork(BlockingQueue<Task> readQueue) {
        final var index = new IndexProvider(readers.length, inputFiles.size());
        return () -> {
            try {
                while (true) {
                    final var fileIndex = index.incrementIndex();
                    if (fileIndex < 0) {
                        break;
                    }
                    else if (fileIndex >= count) {
                        throw new RuntimeException("AtomicIndex returned invalid file index!");
                    }
                    final var inputFile = inputFiles.get(fileIndex);
                    final var inputImg = mgr.readImage(inputFile);
                    final var outputFile = outputFiles.get(fileIndex);
                    readQueue.put(new Task(inputImg, outputFile));
                }
                final var last = index.decrementReader();
                if (last) {
                    readQueue.put(SENTINEL_TASK);
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable processWork(BlockingQueue<Task> readQueue, BlockingQueue<Task> writeQueue) {
        return () -> {
            try {
                while (true) {
                    final var task = readQueue.take();
                    if (Task.isSentinel(task)) {
                        for (int i = 0; i < writers.length; i++) {
                            writeQueue.put(SENTINEL_TASK);
                        }
                        break;
                    }
                    final var output = mgr.copyImage(task.image);
                    processor.process(task.image, output);
                    writeQueue.put(new Task(output, task.path));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Runnable writeWork(BlockingQueue<Task> writeQueue) {
        return () -> {
            try {
                while (true) {
                    final var task = writeQueue.take();
                    if (Task.isSentinel(task)) {
                        break;
                    }
                    mgr.writeImage(task.image, task.path);
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public void close() throws InterruptedException {
        for (Thread t : readers) t.join();
        proc.join();
        for (Thread t : writers) t.join();
    }

    private static class Task {
        private final Image image;
        private final Path path;

        public Task(Image image, Path path) {
            this.image = image;
            this.path = path;
        }

        public static boolean isSentinel(Task other) {
            return other.image == null && other.path == null;
        }
    }

    private static class IndexProvider {
        private final int size;
        private final AtomicInteger activeReaders;
        private final AtomicInteger next;

        IndexProvider(int readers, int size) {
            this.size = size;
            this.activeReaders = new AtomicInteger(readers);
            this.next = new AtomicInteger(0);
        }

        int incrementIndex() {
            if (next.get() >= size) {
                return -1;
            }
            return next.incrementAndGet();
        }

        boolean decrementReader() {
            final var result = activeReaders.decrementAndGet();
            return result == 0;
        }
    }
}
