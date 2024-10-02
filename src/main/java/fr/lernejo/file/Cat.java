package fr.lernejo.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cat {
    public static void main(String[] args) {
        int exitCode = process(args);
        System.exit(exitCode);
    }

    public static int process(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing argument");
            return 3;
        } else if (args.length > 1) {
            System.out.println("Too many arguments");
            return 4;
        }

        File file = new File(args[0]);

        if (!file.exists()) {
            System.out.println("File not found");
            return 5;
        }

        if (file.isDirectory()) {
            System.out.println("A file is required");
            return 6;
        }

        if (file.length() > 3072) {
            System.out.println("File too large");
            return 7;
        }

        try {
            String content = Files.readString(Path.of(file.getPath()));
            System.out.println(content);
        } catch (IOException e) {
            System.out.println("Can not read the file.");
            return 8;
        }

        return 0;
    }
}
