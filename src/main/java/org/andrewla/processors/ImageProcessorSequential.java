package org.andrewla.processors;

import org.andrewla.Image;
import org.andrewla.ImageManager;
import org.andrewla.ImageProcessor;
import org.andrewla.Kernel;

public class ImageProcessorSequential extends ImageProcessor {
    public ImageProcessorSequential(ImageManager mgr, BorderPolicy borderPolicy) {
        super(mgr, borderPolicy);
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
            for (int y = 0; y < source.height(); y++) {
                for (int x = 0; x < source.width(); x++) {
                    destination.setPixel(x, y, computePixel(source, x, y, kernel));
                }
            }

            final var swapped = swapBuffers(source, destination);
            source = swapped[0];
            destination = swapped[1];
        }

        copyImage(source, output);
    }
}
