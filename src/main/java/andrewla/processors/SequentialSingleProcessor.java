package andrewla.processors;

import andrewla.Kernel;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SequentialSingleProcessor extends BaseSingleProcessor {
    private final List<Kernel> kernels = new ArrayList<>();
    private ImageReader reader = null;

    @Override
    public List<BufferedImage> applyFilters() {
        return List.of();
    }
}
