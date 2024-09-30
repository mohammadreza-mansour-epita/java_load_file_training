package fr.lernejo.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cat {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing argument");
            System.exit(3);
        } else if (args.length > 1) {
            System.out.println("Too many arguments");
            System.exit(4);
        }

        File file = new File(args[0]);

        if (!file.exists()) {
            System.out.println("File not found");
            System.exit(5);
        }

        if (file.isDirectory()) {
            System.out.println("A file is required");
            System.exit(6);
        }

        if (file.length() > 3072) {
            System.out.println("File too large");
            System.exit(7);
        }

        try {
            String content = Files.readString(Path.of(file.getPath()));
            System.out.println(content);
        } catch (IOException e) {
            System.out.println("Can not read the file.");
        }
    }
}
