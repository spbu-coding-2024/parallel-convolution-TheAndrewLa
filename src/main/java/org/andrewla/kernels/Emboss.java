package org.andrewla.kernels;

import org.andrewla.Kernel;

public class Emboss extends AbstractKernel {
    public Emboss(int size) {
        super(size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= i; j++) {
                setValue(j, i, -1);
            }

            setValue(size - 1 - i, i, 0);

            for (int j = i + 1; j < size; j++) {
                setValue(j, i, 1);
            }
        }

        setFactor();
        setBias(128);
    }

    @Override
    public Kernel getResized(int newSize) {
        return new Emboss(newSize);
    }

    @Override
    public Kernel getExpanded(int newSize) {
        return new Emboss(getSize()).expandWithZeros(newSize);
    }
}
