package andrewla.kernels;

import andrewla.Kernel;

public class MotionBlur extends BaseKernel {
    private final double angle;

    public MotionBlur(int size, double angle) {
        super(size);
        this.angle = angle;

        var angleRadians = Math.toRadians(angle);

        var sin = Math.sin(angleRadians);
        var cos = Math.cos(angleRadians);

        var center = size / 2;

        for (int i = 0; i < size; i++) {
            int x = (int) Math.round((i - center) * cos);
            int y = (int) Math.round((i - center) * sin);
            if (Math.abs(x) <= center && Math.abs(y) <= center) {
                setValue(x + center, y + center, 1);
            }
        }

        setFactor();
        setBias(0.0);
    }

    @Override
    public Kernel getResized(int newSize) {
        return new MotionBlur(newSize, angle);
    }

    @Override
    public Kernel getExpanded(int newSize) {
        return new MotionBlur(newSize, angle).expandWithZeros(newSize);
    }
}
