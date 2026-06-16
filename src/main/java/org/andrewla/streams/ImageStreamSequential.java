package org.andrewla.streams;

import org.andrewla.ImageManager;
import org.andrewla.ImageProcessor;
import org.andrewla.ImageStream;

import java.io.IOException;

public class ImageStreamSequential extends ImageStream {
    public ImageStreamSequential(ImageProcessor baseProcessor, ImageManager mgr) {
        super(baseProcessor, mgr);
    }

    @Override
    public void readProcessWrite() {
        if (inputFiles.isEmpty()) {
            return;
        }

        try {
            for (int i = 0; i < count; i++) {
                final var inputImg = mgr.readImage(inputFiles.get(i));
                final var outputImg = mgr.copyImage(inputImg);
                processor.process(inputImg, outputImg);
                mgr.writeImage(outputImg, outputFiles.get(i));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
