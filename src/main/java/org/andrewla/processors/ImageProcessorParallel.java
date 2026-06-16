package org.andrewla.processors;

import org.andrewla.ImageManager;
import org.andrewla.ImageProcessor;
import org.andrewla.Kernel;
import org.andrewla.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.andrewla.ImageProcessor.BorderPolicy.IGNORE;
import static org.andrewla.processors.ImageProcessorParallel.PartitionPolicy.RECTANGLE;

public final class ImageProcessorParallel extends ImageProcessor implements AutoCloseable {
    private final PartitionPolicy policy;

    private final int rectWidth;
    private final int rectHeight;

    private final ExecutorService executor;

    private ImageProcessorParallel(ImageManager mgr, BorderPolicy borderPolicy, PartitionPolicy policy, int threads, int rectWidth, int rectHeight) {
        super(mgr, borderPolicy);

        if (threads <= 0) {
            throw new IllegalArgumentException("Count of threads must be positive");
        }

        this.executor = Executors.newFixedThreadPool(threads);
        this.policy = policy;

        if (rectWidth <= 0) {
            throw new IllegalArgumentException("Working width must be positive");
        }

        if (rectHeight <= 0) {
            throw new IllegalArgumentException("Working height must be positive");
        }

        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
    }

    public ImageProcessorParallel(ImageManager mgr, PartitionPolicy partitionPolicy, int nThreads) {
        this(mgr, IGNORE, partitionPolicy, nThreads, 1, 1);
        if (partitionPolicy == RECTANGLE) {
            throw new IllegalArgumentException("Use rectangle constructor for RECTANGLE policy");
        }
    }

    public ImageProcessorParallel(ImageManager mgr, BorderPolicy borderPolicy, PartitionPolicy partitionPolicy, int nThreads) {
        this(mgr, borderPolicy, partitionPolicy, nThreads, 1, 1);
        if (partitionPolicy == RECTANGLE) {
            throw new IllegalArgumentException("Use rectangle constructor for RECTANGLE policy");
        }
    }

    public ImageProcessorParallel(ImageManager mgr, int rectWidth, int rectHeight, int nThreads) {
        this(mgr, IGNORE, RECTANGLE, nThreads, rectWidth, rectHeight);
    }

    public ImageProcessorParallel(ImageManager mgr, BorderPolicy borderPolicy, int rectWidth, int rectHeight, int nThreads) {
        this(mgr, borderPolicy, RECTANGLE, nThreads, rectWidth, rectHeight);
    }

    @Override
    public void process(Image input, Image output) {
        validateImages(input, output);
        final var backBuffer = mgr.copyImage(input);

        if (kernels.isEmpty()) {
            return;
        }

        var source = input;
        var destination = backBuffer;

        for (Kernel kernel : kernels) {
            switch (policy) {
                case PIXEL:
                    processByPixel(source, destination, kernel);
                    break;

                case ROW:
                    processByRow(source, destination, kernel);
                    break;

                case COLUMN:
                    processByColumn(source, destination, kernel);
                    break;

                case RECTANGLE:
                    processByRectangle(source, destination, kernel);
                    break;

                default:
                    throw new IllegalStateException("Undefined partition policy");
            }

            final var swapped = swapBuffers(source, destination);
            source = swapped[0];
            destination = swapped[1];
        }

        copyImage(source, output);
    }

    private void processByPixel(Image source, Image destination, Kernel kernel) {
        List<Future<?>> futures = new ArrayList<>(source.width() * source.height());

        for (int y = 0; y < source.height(); y++) {
            for (int x = 0; x < source.width(); x++) {
                final var px = x;
                final var py = y;
                futures.add(executor.submit(() -> destination.setPixel(px, py, computePixel(source, px, py, kernel))));
            }
        }

        waitTasks(futures);
    }

    private void processByRow(Image source, Image destination, Kernel kernel) {
        final var futures = new ArrayList<Future<?>>(source.height());

        for (int y = 0; y < source.height(); y++) {
            final int row = y;
            futures.add(executor.submit(() -> {
                for (int x = 0; x < source.width(); x++) {
                    destination.setPixel(x, row, computePixel(source, x, row, kernel));
                }
            }));
        }

        waitTasks(futures);
    }

    private void processByColumn(Image source, Image destination, Kernel kernel) {
        final var futures = new ArrayList<Future<?>>(source.width());

        for (int x = 0; x < source.width(); x++) {
            final var column = x;
            futures.add(executor.submit(() -> {
                for (int y = 0; y < source.height(); y++) {
                    destination.setPixel(column, y, computePixel(source, column, y, kernel));
                }
            }));
        }

        waitTasks(futures);
    }

    private void processByRectangle(Image source, Image destination, Kernel kernel) {
        final var futures = new ArrayList<Future<?>>(source.height());

        for (int startY = 0; startY < source.height(); startY += rectHeight) {
            for (int startX = 0; startX < source.width(); startX += rectWidth) {
                final var x0 = startX;
                final var y0 = startY;
                final var x1 = Math.min(x0 + rectWidth, source.width());
                final var y1 = Math.min(y0 + rectHeight, source.height());

                futures.add(executor.submit(() -> {
                    for (int y = y0; y < y1; y++) {
                        for (int x = x0; x < x1; x++) {
                            destination.setPixel(x, y, computePixel(source, x, y, kernel));
                        }
                    }
                }));
            }
        }

        waitTasks(futures);
    }

    private void waitTasks(List<Future<?>> futures) {
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
    public void close() {
        executor.shutdown();
    }

    public enum PartitionPolicy {
        PIXEL, ROW, COLUMN, RECTANGLE
    }
}