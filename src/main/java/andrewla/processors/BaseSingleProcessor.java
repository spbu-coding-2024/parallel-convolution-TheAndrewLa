package andrewla.processors;

import andrewla.ImageProcessor;
import andrewla.Kernel;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseSingleProcessor implements ImageProcessor {
    protected final List<Kernel> kernels = new ArrayList<>();
    protected ImageReader reader = null;

    protected static BufferedImage copyImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaMultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaMultiplied, null);
    }

    protected static int clamp(int v, int lo, int hi) {
        return Math.min(hi, Math.max(lo, v));
    }

    protected static int clampPixel(int v) {
        return clamp(v, 0, 255);
    }

    @Override
    public void addKernel(Kernel kernel) {
        kernels.add(kernel);
    }

    @Override
    public void addImageReader(ImageReader reader) {
        if (this.reader == null) {
            this.reader = reader;
        } else {
            throw new RuntimeException("Image reader was already set");
        }
    }
}
