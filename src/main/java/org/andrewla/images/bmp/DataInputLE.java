package org.andrewla.images.bmp;

import java.io.*;

final class DataInputLE {
    private final DataInputStream in;

    DataInputLE(InputStream stream) {
        this.in = new DataInputStream(stream);
    }

    int readUint16() throws IOException {
        final var byte0 = in.readUnsignedByte();
        final var byte1 = in.readUnsignedByte();
        return byte0 | (byte1 << 8);
    }

    int readInt32() throws IOException {
        final var byte0 = in.readUnsignedByte();
        final var byte1 = in.readUnsignedByte();
        final var byte2 = in.readUnsignedByte();
        final var byte3 = in.readUnsignedByte();
        return byte0 | (byte1 << 8) | (byte2 << 16) | (byte3 << 24);
    }

    long readUint32() throws IOException {
        return Integer.toUnsignedLong(readInt32());
    }

    void readFully(byte[] bytes) throws IOException {
        in.readFully(bytes);
    }

    int readUnsignedByte() throws IOException {
        return in.readUnsignedByte();
    }

    void skip(long bytes) throws IOException {
        while (bytes > 0) {
            final var skipped = in.skip(bytes);

            if (skipped <= 0) {
                throw new EOFException();
            }

            bytes -= skipped;
        }
    }
}
