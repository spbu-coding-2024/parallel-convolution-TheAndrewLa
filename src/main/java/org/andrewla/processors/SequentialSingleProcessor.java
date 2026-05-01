package org.andrewla.processors;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class SequentialSingleProcessor extends BaseSingleProcessor {
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
            final var kCenter = kSize / 2;

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

                            final var value = k.getValue(kx - kCenter, ky - kCenter);

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

        return List.of(out);
    }
}
