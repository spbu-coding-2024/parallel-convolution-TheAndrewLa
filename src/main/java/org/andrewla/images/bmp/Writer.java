package org.andrewla.images.bmp;

import org.andrewla.images.Pixel;
import org.andrewla.images.PixelSource;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Writer {
    private Writer() {
    }

    public static void write(PixelSource image, Path path) throws IOException {
        try (OutputStream stream = Files.newOutputStream(path)) {
            DataOutputLE out = new DataOutputLE(stream);

            final var width = image.width();
            final var height = image.height();

            final var rowSize = ((width * 3 + 3) / 4) * 4;
            final var loadSize = rowSize * height;
            final var fileSize = 14 + 40 + loadSize;

            writeFileHeader(out, fileSize);
            writeInfoHeader(out, width, height, loadSize);
            writePixelData(out, image);
        }
    }

    private static void writeFileHeader(DataOutputLE out, int fileSize) throws IOException {
        out.writeUint16(0x4D42);
        out.writeUint32(fileSize);
        out.writeUint16(0);
        out.writeUint16(0);
        out.writeUint32(54);
    }

    private static void writeInfoHeader(DataOutputLE out, int width, int height, int imageSize) throws IOException {
        out.writeUint32(40);
        out.writeInt32(width);
        out.writeInt32(height);

        out.writeUint16(1);
        out.writeUint16(24);

        out.writeUint32(0);
        out.writeUint32(imageSize);

        out.writeInt32(0);
        out.writeInt32(0);

        out.writeUint32(0);
        out.writeUint32(0);
    }

    private static void writePixelData(DataOutputLE out, PixelSource image) throws IOException {
        final var width = image.width();
        final var height = image.height();

        final var rowSize = ((width * 3 + 3) / 4) * 4;
        final var padding = rowSize - width * 3;

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                Pixel pixel = image.getPixel(x, y);

                out.writeInt8(pixel.blue());
                out.writeInt8(pixel.green());
                out.writeInt8(pixel.red());
            }

            for (int i = 0; i < padding; i++) {
                out.writeInt8((byte) 0);
            }
        }
    }
}
