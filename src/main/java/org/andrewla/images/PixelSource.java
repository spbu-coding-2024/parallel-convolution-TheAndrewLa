package org.andrewla.images;

public interface PixelSource {
    Pixel getPixel(int x, int y);

    void setPixel(int x, int y, Pixel pixel);

    int width();

    int height();
}
