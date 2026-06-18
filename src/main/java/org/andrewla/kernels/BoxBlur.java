package org.andrewla.kernels;

import org.andrewla.Kernel;

public class BoxBlur extends AbstractKernel {
    private final double blurRadius;

    public BoxBlur(int size, double blurRadius) {
        super(size);
        this.blurRadius = blurRadius;

        final var center = size / 2;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                final var y = (double) (i - center);
                final var x = (double) (j - center);
                final var length = Math.sqrt(x * x + y * y);

                if (length <= blurRadius) {
                    setValue(j, i, 1);
                }
            }
        }

        setFactor();
        setBias(0);
    }

    @Override
    public Kernel getResized(int newSize) {
        return new BoxBlur(newSize, blurRadius);
    }

    @Override
    public Kernel getExpanded(int newSize) {
        return new BoxBlur(getSize(), blurRadius).expandWithZeros(newSize);
    }
}
