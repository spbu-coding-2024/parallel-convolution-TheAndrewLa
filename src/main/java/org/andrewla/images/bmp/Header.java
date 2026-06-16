package org.andrewla.images.bmp;

import java.io.IOException;
import java.io.InputStream;

public final class Header {
    public final int width;
    public final int height;
    public final int bitsPerPixel;
    public final long pixelOffset;
    public final boolean topDown;

    private Header(int width, int height, int bitsPerPixel, long pixelOffset, boolean topDown) {
        this.width = width;
        this.height = height;
        this.bitsPerPixel = bitsPerPixel;
        this.pixelOffset = pixelOffset;
        this.topDown = topDown;
    }

    public static Header read(InputStream in) throws IOException {
        return read(new DataInputLE(in));
    }

    public static Header read(DataInputLE in) throws IOException {
        int signature = in.readUint16();

        if (signature != 0x4D42) {
            throw new IOException("Not a BMP file");
        }

        in.readUint32(); // file size
        in.readUint16();
        in.readUint16();

        long pixelOffset = in.readUint32();

        long dibSize = in.readUint32();

        if (dibSize < 40) {
            throw new IOException("Unsupported DIB header");
        }

        int width = in.readInt32();
        int rawHeight = in.readInt32();

        boolean topDown = rawHeight < 0;

        int height = Math.abs(rawHeight);

        int planes = in.readUint16();
        int bitsPerPixel = in.readUint16();

        if (planes != 1) {
            throw new IOException("Invalid BMP planes");
        }

        return new Header(width, height, bitsPerPixel, pixelOffset, topDown);
    }
}