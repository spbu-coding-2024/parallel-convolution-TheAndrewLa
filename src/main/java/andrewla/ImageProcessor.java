package andrewla;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageProcessor {
    void addKernel(Kernel kernel);

    void addImageReader(ImageReader reader);

    List<BufferedImage> applyFilters();
}
