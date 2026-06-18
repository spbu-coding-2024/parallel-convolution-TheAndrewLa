import org.andrewla.ImageProcessor;
import org.andrewla.ImageStream;
import org.andrewla.images.DefaultImageManager;
import org.andrewla.kernels.*;
import org.andrewla.processors.ImageProcessorParallel;
import org.andrewla.processors.ImageProcessorSequential;
import org.andrewla.streams.ImageStreamParallel;
import org.andrewla.streams.ImageStreamSequential;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.andrewla.ImageProcessor.BorderPolicy.CLAMP;
import static org.andrewla.processors.ImageProcessorParallel.PartitionPolicy.ROW;

public class Bench2 {
    static final int ITERATIONS = 30;

    static void applyKernels(ImageProcessor processor) {
        processor.addKernel(new Sharpen(3));
        processor.addKernel(new BoxBlur(7, 2.2));
        processor.addKernel(new Emboss(3));
        processor.addKernel(new GaussianBlur(7, 1.2));
        processor.addKernel(new MotionBlur(7, 45));
    }

    static void applyPaths(ImageStream stream, Path directory, Path result) {
        final var dir = new File(String.valueOf(directory));
        final var files = dir.listFiles();

        if (files == null) {
            throw new IllegalArgumentException("Invalid path to directory");
        }

        for (final var file : files) {
            stream.addPaths(file.toPath(), Paths.get(String.valueOf(result), file.getName()));
        }
    }

    static PerformanceResult measureOnDirectory(int threads, Path inputDir, Path outputDir) {
        final var mgr = new DefaultImageManager();
        final var procSeq = new ImageProcessorSequential(mgr, CLAMP);
        applyKernels(procSeq);

        final var result = new PerformanceResult();
        var startTime = 0L;

        try (final var stream = new ImageStreamParallel(procSeq, mgr, threads)) {
            applyPaths(stream, inputDir, outputDir);
            startTime = System.currentTimeMillis();
            stream.readProcessWrite();
        }

        result.parallelTime1 = (float) (System.currentTimeMillis() - startTime);

        try (final var procPar = new ImageProcessorParallel(mgr, ROW, 64)) {
            applyKernels(procPar);
            try (final var stream = new ImageStreamParallel(procPar, mgr, threads)) {
                applyPaths(stream, inputDir, outputDir);
                startTime = System.currentTimeMillis();
                stream.readProcessWrite();
            }
        }

        result.parallelTime2 = (float) (System.currentTimeMillis() - startTime);

        return result;
    }

    static void printResult(int threads, PerformanceResult result) {
        System.out.printf("Bench result, %d threads%n", threads);
        System.out.printf("Sequential: %f%n", result.sequentialTime);
        System.out.printf("Parallel (with seq proc): %f%n", result.parallelTime1);
        System.out.printf("Parallel (with par proc): %f%n", result.parallelTime2);
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            final var inputDir = Path.of(args[0]);
            final var outputDir = Path.of(args[1]);
            final var threads = Integer.parseInt(args[2]);

            final var result = new PerformanceResult();

            for (int i = 0; i < ITERATIONS; i++) {
                result.update(measureOnDirectory(threads, inputDir, outputDir), i);
                System.out.print("=");
            }

            System.out.println();
            printResult(threads, result);
        } else {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
    }

    static class PerformanceResult {
        public float sequentialTime;
        public float parallelTime1;
        public float parallelTime2;

        public void update(PerformanceResult other, int iteration) {
            assert iteration >= 0;

            // newTime = (oldTime * count + time) / (count + 1)

            sequentialTime = (sequentialTime * iteration + other.sequentialTime) / (iteration + 1);
            parallelTime1 = (parallelTime1 * iteration + other.parallelTime1) / (iteration + 1);
            parallelTime2 = (parallelTime2 * iteration + other.parallelTime2) / (iteration + 1);
        }
    }
}
