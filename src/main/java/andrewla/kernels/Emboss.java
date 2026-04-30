package andrewla.kernels;

import andrewla.Kernel;

public class Emboss extends AbstractKernel {
    public Emboss(int size) {
        super(size);

        var center = size / 2;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j <= i; j++) {
                setValue(i, j, -1);
            }

            setValue(i, size - 1 - i, 0);

            for (int j = i + 1; j < size; j++) {
                setValue(i, j, 1);
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
        return null;
    }

    @Override
    public Kernel getComposed(Kernel other) {
        return null;
    }
}
