package com.domenic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.domenic.utils.FileUtil;

/**
 * Hello world!
 */
public class Main {

    private static final String userHome = System.getProperty("user.home");
    // the directory that stores thumbnails
    private static final Path thumbnailsDir = Path.of(userHome).resolve(".photos");

    public static void main(String[] args) throws Exception {

        // create the directory that stores the thumbnails
        if (!Files.exists(thumbnailsDir))
            Files.createDirectories(thumbnailsDir);

        // read the directory where source images are
        String directory = args.length == 1 ? args[0] : ".";
        Path sourceDir = Path.of(directory);

        // implement convertion
        convertToThumbnails(sourceDir);
    }

    /**
     * Convert images under a specific directory to thumbnails
     * 
     * @param sourceDir image source
     * @throws Exception
     */
    private static void convertToThumbnails(final Path sourceDir) throws Exception {

        ImageMagick imageMagick = new ImageMagick();

        // count images & record excute time
        AtomicInteger counter = new AtomicInteger();
        long start = System.currentTimeMillis();

        // Get thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // walk through the source directory (recursive)
        // and get all the images, then create thumbnails
        try (Stream<Path> filesStream = Files.walk(sourceDir)) {
            // get stream for images
            Stream<Path> imgStream = filesStream
                    .filter(Files::isRegularFile)
                    .filter(FileUtil::isFileImage);

            // create thumbnail for each image
            imgStream.forEach(file -> {
                executor.submit(() -> {
                    Path thumbnailPath = getThumbnailPath(file);

                    boolean isSuccess = imageMagick.createThumbnail(file, thumbnailPath);
                    if (isSuccess)
                        counter.incrementAndGet();
                });
            });

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }

        long end = System.currentTimeMillis();
        System.out.println("\nSuccessfully convert " + counter +
                " images to thumbnails. Took " + (end - start) + " ms");
    }

    /**
     * Resolve the path where the thumbnails will be stored
     * 
     * @param file image file
     * @return path + thumbnailFileName (.webp)
     */
    private static Path getThumbnailPath(Path file) {
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
