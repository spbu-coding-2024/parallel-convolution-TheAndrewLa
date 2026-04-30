package andrewla.kernels;

import andrewla.Kernel;

public class GaussianBlur extends AbstractKernel {
    private final double blurRadius;

    public GaussianBlur(int size, double blurRadius) {
        super(size);
        this.blurRadius = blurRadius;

        var center = size / 2;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                var x = (double) (j - center);
                var y = (double) (i - center);

                var s2 = 2 * blurRadius * blurRadius;
                var r2 = x * x + y * y;

                var value = Math.exp(-r2 / s2) / (Math.PI * s2);

                setValue(i, j, value);
            }
        }

        setFactor();
        setBias(0);
    }

    @Override
    public Kernel getResized(int newSize) {
        return new GaussianBlur(newSize, blurRadius);
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
