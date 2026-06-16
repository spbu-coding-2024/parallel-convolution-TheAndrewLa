package org.andrewla.images.bmp;

import java.io.IOException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

final class DataOutputLE {
    private final DataOutputStream out;

    DataOutputLE(OutputStream stream) {
        this.out = new DataOutputStream(new BufferedOutputStream(stream));
    }

    void writeInt8(byte value) throws IOException {
        out.writeByte(value);
    }

    void writeUint16(int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >>> 8) & 0xFF);
    }

    void writeUint32(long value) throws IOException {
        out.writeByte((int) (value & 0xFF));
        out.writeByte((int) ((value >>> 8) & 0xFF));
        out.writeByte((int) ((value >>> 16) & 0xFF));
        out.writeByte((int) ((value >>> 24) & 0xFF));
    }

    void writeInt32(int value) throws IOException {
        writeUint32(Integer.toUnsignedLong(value));
    }
}
