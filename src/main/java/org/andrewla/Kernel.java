package org.andrewla;

public interface Kernel {
    int getSize();

    double getValue(int x, int y);

    double getBias();

    double getFactor();

    Kernel getResized(int newSize);

    Kernel getExpanded(int newSize);
}
