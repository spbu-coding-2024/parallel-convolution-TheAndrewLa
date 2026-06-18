package org.andrewla;

import org.andrewla.images.DefaultImageManager;
import org.andrewla.kernels.*;
import org.andrewla.processors.ImageProcessorParallel;
import org.andrewla.processors.ImageProcessorSequential;

import java.util.List;

import static org.andrewla.ImageProcessor.BorderPolicy.CLAMP;
import static org.andrewla.ImageProcessor.BorderPolicy.IGNORE;
import static org.andrewla.processors.ImageProcessorParallel.PartitionPolicy.*;

public abstract class ProcessorsTest extends AbstractImageProcessorTest {
    static void applyKernels(ImageProcessor processor) {
        processor.addKernel(new Sharpen(3));
        processor.addKernel(new BoxBlur(7, 2.2));
        processor.addKernel(new Emboss(3));
        processor.addKernel(new GaussianBlur(7, 1.2));
        processor.addKernel(new MotionBlur(7, 45));
    }

    @Override
    protected ImageManager imageManager() {
        return new DefaultImageManager();
    }

    public static class TestOne extends ProcessorsTest {
        @Override
        protected List<ImageProcessor> processors() {
            final var mgr = imageManager();

            final var base = new ImageProcessorSequential(mgr, CLAMP);
            final var proc1 = new ImageProcessorParallel(mgr, CLAMP, ROW, 16);

            applyKernels(base);
            applyKernels(proc1);

            return List.of(base, proc1);
        }
    }

    public static class TestTwo extends ProcessorsTest {
        @Override
        protected List<ImageProcessor> processors() {
            final var mgr = imageManager();

            final var base = new ImageProcessorSequential(mgr, CLAMP);
            final var proc1 = new ImageProcessorParallel(mgr, CLAMP, COLUMN, 32);

            applyKernels(base);
            applyKernels(proc1);

            return List.of(base, proc1);
        }
    }

    public static class TestThree extends ProcessorsTest {
        @Override
        protected List<ImageProcessor> processors() {
            final var mgr = imageManager();

            final var base = new ImageProcessorSequential(mgr, IGNORE);

            final var proc1 = new ImageProcessorParallel(mgr, ROW, 16);
            final var proc2 = new ImageProcessorParallel(mgr, ROW, 32);
            final var proc3 = new ImageProcessorParallel(mgr, ROW, 64);
            final var proc4 = new ImageProcessorParallel(mgr, ROW, 128);

            final var proc5 = new ImageProcessorParallel(mgr, COLUMN, 10);
            final var proc6 = new ImageProcessorParallel(mgr, COLUMN, 20);
            final var proc7 = new ImageProcessorParallel(mgr, COLUMN, 30);
            final var proc8 = new ImageProcessorParallel(mgr, COLUMN, 40);

            final var proc9 = new ImageProcessorParallel(mgr, RECTANGLE, 33);
            final var proc10 = new ImageProcessorParallel(mgr, RECTANGLE, 66);
            final var proc11 = new ImageProcessorParallel(mgr, RECTANGLE, 99);

            applyKernels(base);
            applyKernels(proc1);
            applyKernels(proc2);
            applyKernels(proc3);
            applyKernels(proc4);
            applyKernels(proc5);
            applyKernels(proc6);
            applyKernels(proc7);
            applyKernels(proc8);
            applyKernels(proc9);
            applyKernels(proc10);
            applyKernels(proc11);

            return List.of(base, proc1, proc2, proc3, proc4, proc5, proc6, proc7, proc8, proc9, proc10, proc11);
        }
    }

    public static class TestFour extends ProcessorsTest {
        @Override
        protected List<ImageProcessor> processors() {
            final var mgr = imageManager();

            final var base = new ImageProcessorSequential(mgr, IGNORE);

            final var proc1 = new ImageProcessorParallel(mgr, PIXEL, 8);
            final var proc2 = new ImageProcessorParallel(mgr, PIXEL, 16);
            final var proc3 = new ImageProcessorParallel(mgr, PIXEL, 32);

            applyKernels(base);
            applyKernels(proc1);
            applyKernels(proc2);
            applyKernels(proc3);

            return List.of(base, proc1, proc2, proc3);
        }
    }
}
