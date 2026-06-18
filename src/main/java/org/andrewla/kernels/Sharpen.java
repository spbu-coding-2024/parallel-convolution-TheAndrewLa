package org.andrewla.kernels;

import org.andrewla.Kernel;

public class Sharpen extends AbstractKernel {
    public Sharpen(int size) {
        super(size);

        if (size == 3) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    setValue(i, j, -1);
                }
            }

            setValue(1, 1, 9);
        }
        else if (size == 5) {
            for (int i = 0; i < 5; i++) {
                setValue(i, 0, -1);
                setValue(i, 4, -1);
                setValue(0, i, -1);
                setValue(4, i, -1);
            }
            for (int i = 1; i < 4; i++) {
                setValue(i, 1, 2);
                setValue(i, 3, 2);
                setValue(1, i, 2);
                setValue(3, i, 2);
            }

            setValue(2, 2, 8);
        }
        else {
            throw new IllegalArgumentException("Size of sharpen kernel can be either 3 or 5");
        }

        setFactor();
        setBias(0);
    }

    @Override
    public Kernel getResized(int newSize) {
        return new Sharpen(newSize);
    }

    @Override
    public Kernel getExpanded(int newSize) {
        return new Sharpen(getSize()).expandWithZeros(newSize);
    }
}
