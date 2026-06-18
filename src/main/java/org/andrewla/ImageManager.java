package org.andrewla;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageManager {
    Image createImage(int width, int height);
    
    Image copyImage(Image image);
    
    Image readImage(Path path) throws IOException;
    
    void writeImage(Image image, Path path) throws IOException;
}
