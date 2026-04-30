package andrewla.processors;

import andrewla.Kernel;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SequentialSingleProcessor extends BaseSingleProcessor {
    private final List<Kernel> kernels = new LinkedList<>();
    private ImageReader reader = null;

    private static BufferedImage copyImage(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaMultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaMultiplied, null);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.min(hi, Math.max(lo, v));
    }

    private static int clampPixel(int v) {
        return clamp(v, 0, 255);
    }

    @Override
    public List<BufferedImage> applyFilters() {
        BufferedImage src;

        try {
            src = reader.read(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final var width = src.getWidth() / 2;
        final var height = src.getHeight() / 2;

        BufferedImage out = copyImage(src);

        for (var k : kernels) {
            final var kSize = k.getSize();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    var r = 0.0;
                    var g = 0.0;
                    var b = 0.0;

                    for (int kx = 0; kx < kSize; kx++) {
                        for (int ky = 0; ky < kSize; ky++) {
                            final var px = clamp(x + kx, 0, width - 1);
                            final var py = clamp(y + ky, 0, height - 1);

                            final var rgb = src.getRGB(px, py);
                            final var red = (rgb >> 16) & 0xFF;
                            final var green = (rgb >> 8) & 0xFF;
                            final var blue = rgb & 0xFF;

                            final var value = k.getValue(kx + kSize, ky + kSize);

                            r += red * value;
                            g += green * value;
                            b += blue * value;
                        }
                    }

                    final var bias = k.getBias();
                    final var factor = k.getFactor();

                    final var nr = clampPixel((int) (r * factor + bias));
                    final var ng = clampPixel((int) (g * factor + bias));
                    final var nb = clampPixel((int) (b * factor + bias));

                    var pixel = 0xFF000000;

                    pixel |= nr << 16;
                    pixel |= ng << 8;
                    pixel |= nb;

                    out.setRGB(x, y, pixel);
                }
            }
        }

        final var result = new LinkedList<BufferedImage>();
        result.add(out);

        return result;
    }
}
