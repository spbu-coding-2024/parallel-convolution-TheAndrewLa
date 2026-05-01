package org.andrewla.processors;

import org.andrewla.ImageProcessor;
import org.andrewla.Kernel;

import javax.imageio.ImageReader;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMultipleProcessor implements ImageProcessor {
    private final List<Kernel> kernels = new ArrayList<>();
    private final List<ImageReader> readers = new ArrayList<>();

    @Override
    public void addKernel(Kernel kernel) {
        kernels.add(kernel);
    }

    @Override
    public void addImageReader(ImageReader reader) {
        readers.add(reader);
    }
}
