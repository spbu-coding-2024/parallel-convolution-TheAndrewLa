package org.andrewla.images;

public class DefaultPixelSource implements PixelSource {
    private final int width;
    private final int height;

    private final Pixel[][] pixels;

    public DefaultPixelSource(int size) {
        this(size, size);
    }

    public DefaultPixelSource(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width of an image should be positive");
        }

        if (height <= 0) {
            throw new IllegalArgumentException("Height of an image should be positive");
        }

        this.width = width;
        this.height = height;

        this.pixels = new Pixel[height][width];
    }

    public Pixel getPixel(int x, int y) {
        checkCoordinates(x, y);
        return pixels[y][x];
    }

    public void setPixel(int x, int y, Pixel pixel) {
        checkCoordinates(x, y);
        pixels[y][x] = pixel;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    protected void checkCoordinates(int x, int y) {
        if (x < 0 || x >= width) {
            throw new IndexOutOfBoundsException("x=" + x + ", width=" + width);
        }

        if (y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("y=" + y + ", height=" + height);
        }
    }
}
