package andrewla.kernels;

import andrewla.Kernel;

import java.util.Objects;

public abstract class AbstractKernel implements Kernel {
    private final int size;
    private final double[][] data;

    private double factor;
    private double bias;

    protected AbstractKernel(int size) {
        if (size % 2 == 0) {
            throw new IllegalArgumentException("Size of kernel matrix should be odd");
        }

        this.size = size;
        this.data = new double[size][size];
        this.factor = 1;
        this.bias = 0;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public double getValue(int x, int y) {
        if (x < 0 || x > size) {
            throw new IllegalArgumentException("Invalid X coordinate");
        }

        if (y < 0 || y > size) {
            throw new IllegalArgumentException("Invalid Y coordinate");
        }

        return data[y][x];
    }

    protected void setValue(int x, int y, double value) {
        if (x < 0 || x > size) {
            throw new IllegalArgumentException("Invalid X coordinate");
        }

        if (y < 0 || y > size) {
            throw new IllegalArgumentException("Invalid Y coordinate");
        }

        data[y][x] = value;
    }

    protected void setFactor() {
        var sum = 0.0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sum += data[i][j];
            }
        }

        factor = 1 / sum;
    }

    protected void setBias(double bias) {
        this.bias = bias;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) return false;

        AbstractKernel that = (AbstractKernel) other;
        return size == that.size && Double.compare(factor, that.factor) == 0 && Objects.deepEquals(data, that.data);
    }
}
