package org.andrewla.kernels;

import org.andrewla.Kernel;

public class BoxBlur extends AbstractKernel {
    private final double blurRadius;

    public BoxBlur(int size, double blurRadius) {
        super(size);
        this.blurRadius = blurRadius;
    }

    @Override
    public Kernel getResized(int newSize) {
        return null;
    }

    @Override
    public Kernel getExpanded(int newSize) {
        return new BoxBlur(getSize(), blurRadius).expandWithZeros(newSize);
    }
}
