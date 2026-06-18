package org.andrewla;

import org.andrewla.images.DefaultImageManager;
import org.andrewla.kernels.Emboss;
import org.andrewla.kernels.GaussianBlur;
import org.andrewla.kernels.Identity;
import org.andrewla.processors.ImageProcessorSequential;

import java.util.List;

public abstract class KernelsTest extends AbstractImageProcessorTest {
    @Override
    protected ImageManager imageManager() {
        return new DefaultImageManager();
    }

    public static class TestOne extends KernelsTest {
        @Override
        protected List<ImageProcessor> processors() {
            final var first = new ImageProcessorSequential(imageManager(), ImageProcessor.BorderPolicy.CLAMP);
            first.addKernel(new Emboss(5));
            first.addKernel(new GaussianBlur(5, 1.5));

            final var second = new ImageProcessorSequential(imageManager(), ImageProcessor.BorderPolicy.CLAMP);
            first.addKernel(new GaussianBlur(5, 1.5));
            first.addKernel(new Emboss(5));

            return List.of(first, second);
        }
    }

    public static class TestTwo extends KernelsTest {
        @Override
        protected List<ImageProcessor> processors() {
            final var first = new ImageProcessorSequential(imageManager(), ImageProcessor.BorderPolicy.MIRROR);
            first.addKernel(new Emboss(5));
            first.addKernel(new Identity(7));
            first.addKernel(new GaussianBlur(5, 1.5));

            final var second = new ImageProcessorSequential(imageManager(), ImageProcessor.BorderPolicy.MIRROR);
            first.addKernel(new Identity(7));
            first.addKernel(new GaussianBlur(5, 1.5));
            first.addKernel(new Identity(7));
            first.addKernel(new Emboss(5));

            final var third = new ImageProcessorSequential(imageManager(), ImageProcessor.BorderPolicy.MIRROR);
            first.addKernel(new GaussianBlur(5, 1.5));
            first.addKernel(new Identity(7));
            first.addKernel(new Emboss(5));
            first.addKernel(new Identity(7));

            final var fourth = new ImageProcessorSequential(imageManager(), ImageProcessor.BorderPolicy.MIRROR);
            first.addKernel(new Identity(7));
            first.addKernel(new GaussianBlur(5, 1.5));
            first.addKernel(new Emboss(5));
            first.addKernel(new Identity(7));

            return List.of(first, second, third, fourth);
        }
    }
}
