package com.domenic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Hello world!
 */
public class Main {

    private static final String userHome = System.getProperty("user.home");
    private static final Path thumbnailsDir = Path.of(userHome).resolve(".photos");

    public static void main(String[] args) throws IOException, InterruptedException {

        if (!Files.exists(thumbnailsDir))
            Files.createDirectory(thumbnailsDir);

        // read the directory where source images are
        String directory = args.length == 1 ? args[0] : ".";
        Path sourceDir = Path.of(directory);

        // count images & record excute time
        AtomicInteger counter = new AtomicInteger();
        long start = System.currentTimeMillis();

        ImageMagick imageMagick = new ImageMagick();

        // Get thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // walk through the source directory (recursive)
        // and get all the images, then create thumbnails
        try (Stream<Path> filesStream = Files.walk(sourceDir)) {
            // get stream for images
            Stream<Path> imgStream = filesStream
                    .filter(Files::isRegularFile)
                    .filter(Main::isImage);

            // create thumbnail for each image
            imgStream.forEach(file -> {
                executor.submit(() -> {
                    imageMagick.createThumbnail(file, thumbnailsDir.resolve(file.getFileName()));
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

    private static boolean isImage(Path filePath) {
        try {
            // get file's media type
            String mimeType = Files.probeContentType(filePath);
            return mimeType != null && mimeType.contains("image");
        } catch (IOException e) {
            return false;
        }
    }

}
