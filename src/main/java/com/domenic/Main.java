package com.domenic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Hello world!
 */
public class Main {

    private static final String userHome = System.getProperty("user.home");
    private static final Path thumbnailsDir = Path.of(userHome).resolve(".photos");

    public static void main(String[] args) throws IOException {

        if (!Files.exists(thumbnailsDir))
            Files.createDirectory(thumbnailsDir);

        String directory = args.length == 1 ? args[0] : ".";
        Path sourceDir = Path.of(directory);

        AtomicInteger counter = new AtomicInteger();
        long start = System.currentTimeMillis();

        ImageMagick imageMagick = new ImageMagick();
        try (Stream<Path> filesStream = Files.walk(sourceDir)) {
            Stream<Path> imgStream = filesStream.filter(Files::isRegularFile).filter(Main::isImage);
            imgStream.forEach(file -> {
                imageMagick.createThumbnail(file, thumbnailsDir.resolve(file.getFileName()));
                counter.incrementAndGet();
            });
        }

        long end = System.currentTimeMillis();
        System.out
                .println("\nSuccessfully convert " + counter + " images to thumbnails. Took " + (end - start) + " ms");

        // Path target = Path.of("D:\\Document\\coco-lede-thumbnail.jpg");
        // boolean success = createThumbnail(source, target);
        // System.out.println("success = " + success);
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
