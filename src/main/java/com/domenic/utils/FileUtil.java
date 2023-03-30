package com.domenic.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import io.github.rctcwyvrn.blake3.Blake3;

public class FileUtil {

    /**
     * Judge if the file is image
     * 
     * @param filePath file path
     * @return true/false
     */
    public static boolean isFileImage(final Path filePath) {
        try {
            // get file's media type
            String mimeType = Files.probeContentType(filePath);
            return mimeType != null && mimeType.contains("image");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the hash of the file
     */
    public static String fileToHash(final Path filename) {
        // Hashing files
        Blake3 hasher = Blake3.newInstance();

        // hasher.update(new File(filename));

        // rewrite the 'update' method in hasher, which is using legacy java.io.File
        // replace with java.nio.file.Path
        try (InputStream ios = Files.newInputStream(filename)) {
            // Update the hasher 8kb at a time to avoid
            // memory issues when hashing large files
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                if (read == buffer.length) {
                    hasher.update(buffer);
                } else {
                    hasher.update(Arrays.copyOfRange(buffer, 0, read));
                }
            }
            String filehash = hasher.hexdigest();
            return filehash;
        } catch (IOException e) {
            return "N/A";
        }
    }

}
