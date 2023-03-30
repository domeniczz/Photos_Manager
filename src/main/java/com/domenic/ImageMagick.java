package com.domenic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Domenic
 * @Classname ImageMagic
 * @Description 
 * @Date 3/24/2023 9:03 PM
 * @Created by Domenic
 */
public class ImageMagick {

    public enum Version {
        NA, IM_6, IM_7
    }

    public int run(String... cmds) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(cmds);
        builder.inheritIO();
        Process process = builder.start();
        boolean finished = process.waitFor(1, TimeUnit.SECONDS);
        if (!finished) {
            process.destroy();
        }
        return process.exitValue();
    }

    public Version detectVersion() {
        try {
            int exitCode = run("magick", "--version");
            if (exitCode == 0) {
                return Version.IM_7;
            }
            exitCode = run("convert", "--version");
            if (exitCode == 0) {
                return Version.IM_6;
            }
            return Version.NA;
        } catch (Exception e) {
            return Version.NA;
        }
    }

    public boolean createThumbnail(Path source, Path target) {
        try {
            // normalize and get absolute path, in case the path contains weird characters
            String src = source.normalize().toAbsolutePath().toString();
            String tar = target.normalize().toAbsolutePath().toString();
            // command to be executed
            List<String> command = List.of("magick", "convert", "-resize", "300x", source.toString(), tar);
            ProcessBuilder builder = new ProcessBuilder(command);
            // redirect the output to our console
            builder.inheritIO();
            Process process = builder.start();
            boolean isFinished = process.waitFor(3, TimeUnit.SECONDS);

            // if timeout, then destroy the process
            if (!isFinished) {
                process.destroy();
            }

            System.out.print(Thread.currentThread().getName() + " ");
            System.out.println("---- " + src.substring(src.lastIndexOf('\\') + 1) + "'s thumbnail created! ----");
            System.out.println("target dir: " + tar + "\n");

            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
