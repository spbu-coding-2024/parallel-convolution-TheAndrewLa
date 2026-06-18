package org.andrewla;

import org.andrewla.images.Pixel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractImageProcessorTest {
    private static final Path INPUT_DIR = Paths.get("src/test/resources/small_images");

    static Stream<Path> allInput() throws IOException {
        return Files.list(INPUT_DIR).filter(path -> path.toString().matches(".*\\.bmp$"));
    }

    protected abstract ImageManager imageManager();

    protected abstract List<ImageProcessor> processors();

    @ParameterizedTest(name = "{0} – check consistency")
    @DisplayName("All processors must produce identical output")
    @MethodSource("allInput")
    void allProcessorsProduceSameResult(Path inputPath) throws IOException {
        final var manager = imageManager();
        final var processors = processors();

        final var inputImage = manager.readImage(inputPath);
        final var outputs = new Image[processors.size()];

        for (int i = 0; i < processors.size(); i++) {
            Image output = manager.copyImage(inputImage);
            processors.get(i).process(inputImage, output);
            outputs[i] = output;
        }

        Image reference = outputs[0];
        for (int i = 1; i < outputs.length; i++) {
            assertImagesEqual(reference, outputs[i], String.format("Processor #%d differs from processor #0", i));
        }
    }

    private void assertImagesEqual(Image expected, Image actual, String message) {
        assertEquals(expected.width(), actual.width(), message + " – width mismatch");
        assertEquals(expected.height(), actual.height(), message + " – height mismatch");

        for (int y = 0; y < expected.height(); y++) {
            for (int x = 0; x < expected.width(); x++) {
                Pixel expectedPixel = expected.getPixel(x, y);
                Pixel actualPixel = actual.getPixel(x, y);

                final var r1 = Byte.toUnsignedInt(expectedPixel.red());
                final var r2 = Byte.toUnsignedInt(actualPixel.red());

                final var g1 = Byte.toUnsignedInt(expectedPixel.green());
                final var g2 = Byte.toUnsignedInt(actualPixel.green());

                final var b1 = Byte.toUnsignedInt(expectedPixel.blue());
                final var b2 = Byte.toUnsignedInt(actualPixel.blue());

                assertEquals(r1, r2, String.format("%s – red mismatch at (%d,%d)", message, x, y));
                assertEquals(g1, g2, String.format("%s – green mismatch at (%d,%d)", message, x, y));
                assertEquals(b1, b2, String.format("%s – blue mismatch at (%d,%d)", message, x, y));
            }
        }
    }
}