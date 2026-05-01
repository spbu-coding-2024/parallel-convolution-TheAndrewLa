package org.andrewla.processors;

import org.andrewla.ImageProcessor;
import org.andrewla.Kernel;

import javax.imageio.ImageReader;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseSingleProcessor implements ImageProcessor {
    private final List<Kernel> kernels = new ArrayList<>();
    private ImageReader reader = null;

    @Override
    public void addKernel(Kernel kernel) {
        kernels.add(kernel);
    }

    @Override
    public void addImageReader(ImageReader reader) {
        if (this.reader == null) {
            this.reader = reader;
        } else {
            throw new RuntimeException("Image reader was already set");
        }
    }
}
