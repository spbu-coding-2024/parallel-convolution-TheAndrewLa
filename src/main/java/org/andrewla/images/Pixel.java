package org.andrewla.images;

public class Pixel {
    private final byte red;
    private final byte green;
    private final byte blue;

    public Pixel(byte red, byte green, byte blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Pixel(byte grayscale) {
        this.red = grayscale;
        this.green = grayscale;
        this.blue = grayscale;
    }

    public byte red() {
        return red;
    }

    public byte green() {
        return green;
    }

    public byte blue() {
        return blue;
    }
}
