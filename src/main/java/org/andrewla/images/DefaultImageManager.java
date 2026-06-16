package org.andrewla.images;

import org.andrewla.Image;
import org.andrewla.ImageManager;

import java.io.IOException;
import java.nio.file.Path;

public class DefaultImageManager implements ImageManager {
    @Override
    public Image createImage(int width, int height) {
        return new DefaultImage(SourceManager.allocate(width, height));
    }

    @Override
    public Image copyImage(Image image) {
        return new DefaultImage(SourceManager.copy(image.getSource()));
    }

    @Override
    public Image readImage(Path path) throws IOException {
        return new DefaultImage(SourceManager.readFromFile(path));
    }

    @Override
    public void writeImage(Image image, Path path) throws IOException {
        SourceManager.writeToFile(image.getSource(), path);
    }
}
