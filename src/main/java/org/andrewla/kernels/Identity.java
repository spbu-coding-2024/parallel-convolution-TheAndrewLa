package org.andrewla.kernels;

import org.andrewla.Kernel;

public class Identity extends AbstractKernel {
    public Identity(int size) {
        super(size);

        var center = size / 2;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                setValue(i, j, 0);
            }
        }

        setValue(center, center, 1);
    }

    @Override
    public Kernel getResized(int newSize) {
        return new Identity(newSize);
    }

    @Override
    public Kernel getExpanded(int newSize) {
        return new Identity(newSize);
    }
}
