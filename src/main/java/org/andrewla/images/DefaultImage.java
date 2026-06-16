package org.andrewla.images;

import org.andrewla.Image;

public class DefaultImage implements Image {
    private final PixelSource source;

    public DefaultImage(PixelSource source) {
        this.source = source;
    }

    @Override
    public Pixel getPixel(int x, int y) {
        return source.getPixel(x, y);
    }

    @Override
    public void setPixel(int x, int y, Pixel pixel) {
        source.setPixel(x, y, pixel);
    }

    @Override
    public PixelSource getSource() {
        return source;
    }

    @Override
    public int width() {
        return source.width();
    }

    @Override
    public int height() {
        return source.height();
    }
}
