package org.andrewla;

import org.andrewla.images.Pixel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class ImageProcessor {
    protected final List<Kernel> kernels = new LinkedList<>();
    protected final BorderPolicy borderPolicy;
    protected final ImageManager mgr;

    protected ImageProcessor(ImageManager mgr, BorderPolicy borderPolicy) {
        this.mgr = mgr;
        this.borderPolicy = borderPolicy;
    }

    static protected Image[] swapBuffers(Image source, Image destination) {
        return new Image[]{destination, source};
    }

    static protected void copyImage(Image source, Image destination) {
        for (int x = 0; x < source.width(); x++) {
            for (int y = 0; y < source.height(); y++) {
                destination.setPixel(x, y, source.getPixel(x, y));
            }
        }

    }

    private int mirror(int coordinate, int limit) {
        while (coordinate < 0 || coordinate >= limit) {
            if (coordinate < 0) {
                coordinate = -coordinate - 1;
            } else {
                coordinate = 2 * limit - coordinate - 1;
            }
        }
        return coordinate;
    }

    private Pixel resolvePixel(Image image, int x, int y, BorderPolicy policy) {
        switch (policy) {
            case CLAMP:
                int clampedX = Math.max(0, Math.min(x, image.width() - 1));
                int clampedY = Math.max(0, Math.min(y, image.height() - 1));
                return image.getPixel(clampedX, clampedY);

            case MIRROR:
                int mirroredX = mirror(x, image.width());
                int mirroredY = mirror(y, image.height());
                return image.getPixel(mirroredX, mirroredY);

            case IGNORE:
                if (x < 0 || x >= image.width() || y < 0 || y >= image.height()) {
                    return new Pixel((byte) 0);
                }
                return image.getPixel(x, y);

            default:
                throw new IllegalStateException("Unsupported border policy: " + policy);
        }
    }

    private int clampColor(double value) {
        return (int) Math.max(0, Math.min(255, Math.round(value)));
    }

    protected Pixel computePixel(Image image, int centerX, int centerY, Kernel kernel) {
        var red = 0.0;
        var green = 0.0;
        var blue = 0.0;

        final var size = kernel.getSize();
        final var half = kernel.getSize() / 2;

        for (int ky = 0; ky < size; ky++) {
            for (int kx = 0; kx < size; kx++) {
                final var imageX = centerX + kx - half;
                final var imageY = centerY + ky - half;

                final var pixel = resolvePixel(image, imageX, imageY, borderPolicy);
                final var weight = kernel.getValue(kx, ky);

                red += (double) (pixel.red() & 0xFF) * weight;
                green += (double) (pixel.green() & 0xFF) * weight;
                blue += (double) (pixel.blue() & 0xFF) * weight;
            }
        }

        final var r = (byte) clampColor(red * kernel.getFactor() + kernel.getBias());
        final var g = (byte) clampColor(green * kernel.getFactor() + kernel.getBias());
        final var b = (byte) clampColor(blue * kernel.getFactor() + kernel.getBias());
        return new Pixel(r, g, b);
    }

    protected void validateImages(Image input, Image output) {
        final var wCond = input.height() == output.height();
        final var hCond = input.width() == output.width();

        if (!wCond || !hCond) {
            throw new IllegalArgumentException("Invalid sizes of input and output images");
        }
    }

    public void addKernel(Kernel kernel) {
        kernels.add(kernel);
    }

    public void addKernels(Collection<? extends Kernel> kernels) {
        this.kernels.addAll(kernels);
    }

    public abstract void process(Image input, Image output);

    public enum BorderPolicy {
        CLAMP, MIRROR, IGNORE
    }
}
