package andrewla.processors;

import andrewla.Kernel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelSingleProcessor extends BaseSingleProcessor {
    private final ParallelPolicy policy;
    private final ExecutorService executor;

    public ParallelSingleProcessor(ParallelPolicy policy, ExecutorService executor) {
        this.policy = policy;
        this.executor = executor;
    }

    public ParallelSingleProcessor(ParallelPolicy policy) {
        this.policy = policy;
        this.executor = Executors.newFixedThreadPool(16);
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

        for (final var k : kernels) {
            switch (policy) {
                case ROW:
                    rows(src, out, width, height, k);
                    break;
                case COLUMN:
                    columns(src, out, width, height, k);
                    break;
                case PIXEL:
                    pixels(src, out, width, height, k);
                    break;
                default:
                    throw new IllegalStateException("Invalid policy value");
            }
        }

        return List.of(out);
    }

    private void rows(BufferedImage src, BufferedImage out, int w, int h, Kernel kernel) {
        for (int y = 0; y < h; y++) {
            final var finalY = y;
            executor.submit(() -> {
                for (int x = 0; x < w; x++) {
                    processPixel(src, out, x, finalY, kernel);
                }
            });
        }

        executor.shutdown();
    }

    private void columns(BufferedImage src, BufferedImage out, int w, int h, Kernel kernel) {
        for (int x = 0; x < w; x++) {
            final var finalX = x;
            executor.submit(() -> {
                for (int y = 0; y < h; y++) {
                    processPixel(src, out, finalX, y, kernel);
                }
            });
        }

        executor.shutdown();
    }

    private void pixels(BufferedImage src, BufferedImage out, int w, int h, Kernel kernel) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final var finalX = x;
                final var finalY = y;
                executor.submit(() -> {
                    processPixel(src, out, finalX, finalY, kernel);
                });
            }
        }

        executor.shutdown();
    }

    private void processPixel(BufferedImage src, BufferedImage out, int x, int y, Kernel kernel) {
        final var width = src.getWidth();
        final var height = src.getHeight();

        var r = 0.0;
        var g = 0.0;
        var b = 0.0;

        final var kSize = kernel.getSize();
        final var kCenter = kSize / 2;

        for (int ky = 0; ky < kSize; ky++) {
            for (int kx = 0; kx < kSize; kx++) {
                int px = clamp(x + kx, 0, width - 1);
                int py = clamp(y + ky, 0, height - 1);
                int rgb = src.getRGB(px, py);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                final var value = kernel.getValue(kx - kCenter, ky - kCenter);

                r += red * value;
                g += green * value;
                b += blue * value;
            }
        }

        final var bias = kernel.getBias();
        final var factor = kernel.getFactor();

        final var nr = clampPixel((int) (r * factor + bias));
        final var ng = clampPixel((int) (g * factor + bias));
        final var nb = clampPixel((int) (b * factor + bias));

        var pixel = 0xFF000000;

        pixel |= nr << 16;
        pixel |= ng << 8;
        pixel |= nb;

        out.setRGB(x, y, pixel);
    }

    public enum ParallelPolicy {
        ROW, COLUMN, PIXEL,
    }
}
