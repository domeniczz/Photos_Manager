package com.domenic;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.domenic.beans.Dimensions;
import com.domenic.conditions.EnabledIfImageMagickIsAvailable;
import com.domenic.utils.FileUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Domenic
 * @Classname MainTest
 * @Description
 * @Date 3/24/2023 8:41 PM
 * @Created by Domenic
 */
class ImageMagickTest {

    /**
     * Test ImageMagick Detection
     */
    @Test
    void imageMagick_is_available() {
        assertThat(new ImageMagick().detectVersion()).isNotEqualTo(ImageMagick.Version.NA);
    }

    /**
     * Test SINGLE image-to-thumbnail convertion
     * 
     * @param testDir
     */
    @Test
    @EnabledIfImageMagickIsAvailable
    void single_thumbnail_convertion(@TempDir Path testDir) throws IOException {
        // copy test image to test dir, with file name test.jpg
        Path originalImage = copyTestImageTo(testDir.resolve("test.jpg"));
        // store thumbnail to test dir, with file name thumbnail.jpg
        Path thumbnail = testDir.resolve("thumbnail.jpg");

        new ImageMagick().createThumbnail(originalImage, thumbnail);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(thumbnail).exists();
        softly.assertThat(thumbnail).isNotEmptyFile();
        softly.assertThat(Files.size(thumbnail)).isLessThan(Files.size(originalImage) / 2);
        softly.assertThat(getDimensions(thumbnail).getWidth()).isEqualTo(300);
        softly.assertAll();
    }

    /**
     * Get the dimensions of the image
     * 
     * @param filePath image file path
     * @return
     */
    private Dimensions getDimensions(Path filePath) {
        try (InputStream is = Files.newInputStream(filePath)) {
            BufferedImage read = ImageIO.read(is);
            return new Dimensions(read.getWidth(), read.getHeight());
        } catch (IOException e) {
            return new Dimensions(-1, -1);
        }
    }

    /**
     * Copy the test image to a test directory
     * 
     * @param targetFile target image directory
     * @return
     */
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

    /**
     * Test MULTIPLE image-to-thumbnail convertion (multi-thread)
     * 
     * @throws InterruptedException
     */
    @Test
    @EnabledIfImageMagickIsAvailable
    public void multiple_thumbnail_convertion() throws IOException, InterruptedException {

        // Get test image resource as java.net.URL
        URL resource = ImageMagickTest.class.getResource("/coco-lede.jpg");

        // Get the absolute path of test image (compatible with different os)
        String os = System.getProperty("os.name");
        String tmp;
        if (os.contains("Linux")) {
            tmp = resource.getPath();
            // sourceDir = tmp.substring(0, tmp.lastIndexOf("/"));
        } else if (os.contains("Windows")) {
            tmp = resource.getPath().substring(1);
            // sourceDir =
        } else {
            tmp = resource.getPath();
        }
        // test image directory
        String sourceDir = tmp.substring(0, tmp.lastIndexOf("/"));

        convertImages(Path.of(sourceDir));
    }

    /**
     * Convert images to thumbnails and put them into a directory
     * 
     * @param testDir test image directory
     */
    private void convertImages(@TempDir Path testDir) throws IOException, InterruptedException {
        SoftAssertions softly = new SoftAssertions();

        AtomicInteger counter = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (Stream<Path> filesStream = Files.walk(testDir)) {

            Stream<Path> imgStream = filesStream
                    .filter(Files::isRegularFile)
                    .filter(FileUtil::isFileImage);

            imgStream.forEach(file -> {
                executor.submit(() -> {
                    Path thumbnailPath = getThumbnailPath(file, testDir);

                    boolean isSuccess = new ImageMagick().createThumbnail(file, thumbnailPath);
                    if (isSuccess)
                        counter.incrementAndGet();

                    softly.assertThat(thumbnailPath).exists();
                    softly.assertThat(thumbnailPath).isNotEmptyFile();
                    softly.assertThat(getDimensions(thumbnailPath).getWidth()).isEqualTo(300);
                    softly.assertAll();
                });
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        System.out.println("\nSuccessfully convert " + counter + " images to thumbnails.");
    }

    /**
     * Resolve the path where the thumbnails will be stored
     * 
     * @param file
     * @param thumbnailsDir the directory that stores thumbnails
     * @return path + thumbnailFileName (.webp)
     */
    private static Path getThumbnailPath(Path file, Path thumbnailsDir) {
        // get the hash of the file
        String hash = FileUtil.fileToHash(file);
        // take first 2 characters of the hash as the folder name
        String dir = hash.substring(0, 2);
        // take the rest characters as the file name
        String filename = hash.substring(2);

        // resolve and get the path
        Path thumbnailDir = thumbnailsDir.resolve(dir);
        if (!Files.exists(thumbnailDir)) {
            try {
                Files.createDirectories(thumbnailDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return thumbnailDir.resolve(filename + ".webp");
    }

}