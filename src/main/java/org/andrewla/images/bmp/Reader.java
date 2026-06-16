package org.andrewla.images.bmp;

import org.andrewla.images.Pixel;
import org.andrewla.images.DefaultPixelSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Reader {
    private Reader() {
    }

    public static Header readHeader(Path path) throws IOException {
        try (java.io.InputStream stream = Files.newInputStream(path)) {
            final var in = new DataInputLE(stream);
            return Header.read(in);
        }
    }

    public static DefaultPixelSource read(Path path) throws IOException {
        try (java.io.InputStream stream = Files.newInputStream(path)) {
            final var in = new DataInputLE(stream);
            final var header = Header.read(in);

            DefaultPixelSource image = new DefaultPixelSource(header.width, header.height);
            readPixels(in, header, image);

            return image;
        }
    }

    private static void readPixels(DataInputLE in, Header header, DefaultPixelSource image) throws IOException {
        switch (header.bitsPerPixel) {
            case 24:
                read24BitPixels(in, header, image);
                break;

            case 32:
                read32BitPixels(in, header, image);
                break;

            default:
                throw new IllegalStateException("Unsupported bits-per-pixel: " + header.bitsPerPixel);
        }
    }

    private static void read24BitPixels(DataInputLE in, Header header, DefaultPixelSource image) throws IOException {
        final var width = header.width;
        final var height = header.height;

        final var rowSize = ((width * 3 + 3) / 4) * 4;
        final var rowBuffer = new byte[rowSize];

        for (int fileRow = 0; fileRow < height; fileRow++) {
            in.readFully(rowBuffer);

            final var imageRow = header.topDown ? fileRow : height - 1 - fileRow;
            int offset = 0;

            for (int x = 0; x < width; x++) {
                final var blue = rowBuffer[offset++];
                final var green = rowBuffer[offset++];
                final var red = rowBuffer[offset++];

                image.setPixel(x, imageRow, new Pixel(red, green, blue));
            }
        }
    }

    private static void read32BitPixels(DataInputLE in, Header header, DefaultPixelSource image) throws IOException {
        final var width = header.width;
        final var height = header.height;

        for (int fileRow = 0; fileRow < height; fileRow++) {
            final var imageRow = header.topDown ? fileRow : height - 1 - fileRow;
            for (int x = 0; x < width; x++) {
                final var blue = (byte) in.readUnsignedByte();
                final var green = (byte) in.readUnsignedByte();
                final var red = (byte) in.readUnsignedByte();

                in.readUnsignedByte();

                image.setPixel(x, imageRow, new Pixel(red, green, blue));
            }
        }
    }
}
