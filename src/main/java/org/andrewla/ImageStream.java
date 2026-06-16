package org.andrewla;

import java.nio.file.Path;
import java.util.ArrayList;

public abstract class ImageStream {
    protected final ArrayList<Path> inputFiles = new ArrayList<>();
    protected final ArrayList<Path> outputFiles = new ArrayList<>();
    protected final ImageManager mgr;
    protected final ImageProcessor processor;

    protected int count = 0;

    protected ImageStream(ImageProcessor processor, ImageManager mgr) {
        this.mgr = mgr;
        this.processor = processor;
    }

    public void addPaths(Path input, Path output) {
        count++;
        inputFiles.add(input);
        outputFiles.add(output);
    }

    public abstract void readProcessWrite();
}
