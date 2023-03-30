package com.domenic;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Domenic
 * @Classname MainTest
 * @Description
 * @Date 3/24/2023 8:41 PM
 * @Created by Domenic
 */
class ImageMagickTest {

    @Test
    void imageMagick_is_available() {
        assertThat(new ImageMagick().detectVersion()).isNotEqualTo(ImageMagick.Version.NA);
    }

    @Test
    @EnabledIfImageMagickIsAvailable
    void thumbnail_creation(@TempDir Path testDir) throws IOException {
        Path originalImage = copyTestImageTo(testDir.resolve("test.jpg"));
        Path thumbnail = testDir.resolve("thumbnail.jpg");

        new ImageMagick().createThumbnail(originalImage, thumbnail);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(thumbnail).exists();
        softly.assertThat(thumbnail).isNotEmptyFile();
        softly.assertThat(Files.size(thumbnail)).isLessThan(Files.size(originalImage) / 2);
        softly.assertThat(getDimensions(thumbnail).getWidth()).isEqualTo(300);
        softly.assertAll();
    }

    static final class Dimensions {
        int width;
        int height;

        public Dimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    private Dimensions getDimensions(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            BufferedImage read = ImageIO.read(is);
            return new Dimensions(read.getWidth(), read.getHeight());
        } catch (IOException e) {
            return new Dimensions(-1, -1);
        }
    }

    private static Path copyTestImageTo(Path targetFile) {
        try (InputStream resourceAsStream = ImageMagickTest.class.getResourceAsStream("/coco-lede.jpg")) {
            assert resourceAsStream != null;
            Files.copy(resourceAsStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied test image to: = " + targetFile.toAbsolutePath());
            return targetFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}