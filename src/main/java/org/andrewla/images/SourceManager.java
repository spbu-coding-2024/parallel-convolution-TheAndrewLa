package org.andrewla.images;

import org.andrewla.images.bmp.Reader;
import org.andrewla.images.bmp.Writer;

import java.io.IOException;
import java.nio.file.Path;

public final class SourceManager {
    public static DefaultPixelSource readFromFile(Path imagePath) throws IOException {
        return Reader.read(imagePath);
    }

    public static void writeToFile(PixelSource image, Path imagePath) throws IOException {
        Writer.write(image, imagePath);
    }

    public static DefaultPixelSource allocate(int width, int height) {
        return new DefaultPixelSource(width, height);
    }

    public static DefaultPixelSource allocate(int size) {
        return new DefaultPixelSource(size);
    }

    public static PixelSource copy(PixelSource source) {
        final var width = source.width();
        final var height = source.height();
        final var copied = new DefaultPixelSource(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                copied.setPixel(x, y, source.getPixel(x, y));
            }
        }
        return copied;
    }
}
