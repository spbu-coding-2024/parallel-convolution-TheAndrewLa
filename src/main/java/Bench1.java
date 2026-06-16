import org.andrewla.Image;
import org.andrewla.ImageManager;
import org.andrewla.ImageProcessor;
import org.andrewla.images.DefaultImageManager;
import org.andrewla.kernels.*;
import org.andrewla.processors.ImageProcessorParallel;
import org.andrewla.processors.ImageProcessorSequential;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.andrewla.ImageProcessor.BorderPolicy.*;

public class Bench1 {
    static final int ITERATIONS = 1;
    static final ImageManager manager = new DefaultImageManager();

    static void applyKernels(ImageProcessor processor) {
        processor.addKernel(new BoxBlur(7, 2.2));
        processor.addKernel(new Sharpen(3));
        processor.addKernel(new GaussianBlur(7, 1.2));
        processor.addKernel(new Emboss(3));
        processor.addKernel(new MotionBlur(7, 45));
    }

    static float measureTime(Image input, Image output, ImageProcessor proc) {
        final var startTime = System.currentTimeMillis();
        proc.process(input, output);
        final var endTime = System.currentTimeMillis();
        return (float) (endTime - startTime);
    }

    static PerformanceResult measureOnImage(String pathName, int threadCount, ImageProcessor.BorderPolicy policy) throws IOException {
        final var name = Path.of(pathName);
        var benchResult = new PerformanceResult();

        final var img = manager.readImage(name);

        final var resultSequential = manager.createImage(img.width(), img.height());
        final var procSeq = new ImageProcessorSequential(manager, policy);

        applyKernels(procSeq);
        benchResult.sequentialTime = measureTime(img, resultSequential, procSeq);

        try (final var proc = new ImageProcessorParallel(manager, policy, ImageProcessorParallel.PartitionPolicy.PIXEL, threadCount)) {
            final var result = manager.createImage(img.width(), img.height());
            applyKernels(proc);
            benchResult.parallelPixelsTime = measureTime(img, result, proc);
        }

        try (final var proc = new ImageProcessorParallel(manager, policy, ImageProcessorParallel.PartitionPolicy.ROW, threadCount)) {
            final var result = manager.createImage(img.width(), img.height());
            applyKernels(proc);
            benchResult.parallelRowsTime = measureTime(img, result, proc);
        }

        try (final var proc = new ImageProcessorParallel(manager, policy, ImageProcessorParallel.PartitionPolicy.COLUMN, threadCount)) {
            final var result = manager.createImage(img.width(), img.height());
            applyKernels(proc);
            benchResult.parallelColumnsTime = measureTime(img, result, proc);
        }

        try (final var proc = new ImageProcessorParallel(manager, policy, 64, 64, threadCount)) {
            final var result = manager.createImage(img.width(), img.height());
            applyKernels(proc);
            benchResult.parallelRectangleTime = measureTime(img, result, proc);
        }

        return benchResult;
    }

    static void printResult(int threads, PerformanceResult result) {
        System.out.printf("Bench result, %d threads%n", threads);
        System.out.printf("Sequential: %f%n", result.sequentialTime);
        System.out.printf("Parallel (pixel): %f%n", result.parallelPixelsTime);
        System.out.printf("Parallel (rows): %f%n", result.parallelRowsTime);
        System.out.printf("Parallel (columns): %f%n", result.parallelColumnsTime);
        System.out.printf("Parallel (arbitrary rect): %f%n", result.parallelRectangleTime);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            System.out.printf("Folder with images: %s%n", args[0]);

            final var dir = new File(args[0]);
            final var threads = Integer.parseInt(args[1]);

            final var files = dir.listFiles();

            if (files == null) {
                throw new IllegalArgumentException("Invalid path to directory");
            }

            final var result = new PerformanceResult();
            var iteration = 0;

            for (int i = 0; i < ITERATIONS; i++) {
                for (final var file : files) {
                    final var singleResult = measureOnImage(file.getAbsolutePath(), threads, IGNORE);
                    iteration = result.update(singleResult, iteration);
                    System.out.print("-");
                }
            }

            for (int i = 0; i < ITERATIONS; i++) {
                for (final var file : files) {
                    final var singleResult = measureOnImage(file.getAbsolutePath(), threads, CLAMP);
                    iteration = result.update(singleResult, iteration);
                    System.out.print("-");
                }
            }

            for (int i = 0; i < ITERATIONS; i++) {
                for (final var file : files) {
                    final var singleResult = measureOnImage(file.getAbsolutePath(), threads, MIRROR);
                    iteration = result.update(singleResult, iteration);
                    System.out.print("-");
                }
            }

            printResult(threads, result);
        } else {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
    }

    static class PerformanceResult {
        public float sequentialTime;
        public float parallelPixelsTime;
        public float parallelRowsTime;
        public float parallelColumnsTime;
        public float parallelRectangleTime;

        public int update(PerformanceResult other, int iteration) {
            assert iteration >= 0;

            // newTime = (oldTime * count + time) / (count + 1)

            sequentialTime *= iteration;
            sequentialTime += other.sequentialTime;
            sequentialTime /= (iteration + 1);

            parallelPixelsTime *= iteration;
            parallelPixelsTime += other.parallelPixelsTime;
            parallelPixelsTime /= (iteration + 1);

            parallelRowsTime *= iteration;
            parallelRowsTime += other.parallelRowsTime;
            parallelRowsTime /= (iteration + 1);

            parallelColumnsTime *= iteration;
            parallelColumnsTime += other.parallelColumnsTime;
            parallelColumnsTime /= (iteration + 1);

            parallelRectangleTime *= iteration;
            parallelRectangleTime += other.parallelRectangleTime;
            parallelRectangleTime /= (iteration + 1);

            return iteration + 1;
        }
    }
}
