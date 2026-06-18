package org.andrewla;

import org.andrewla.images.Pixel;
import org.andrewla.images.PixelSource;

public interface Image {
    Pixel getPixel(int x, int y);

    void setPixel(int x, int y, Pixel pixel);

    PixelSource getSource();

    int width();

    int height();
}
