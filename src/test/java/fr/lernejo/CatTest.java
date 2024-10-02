package fr.lernejo;

import fr.lernejo.file.Cat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CatTest {

    @Test
    void testMissingArguments() {
        String[] args = {};
        int exitCode = Cat.process(args);
        assertEquals(3, exitCode);
    }

    @Test
    void testTooManyArguments() {
        String[] args = {"file1.txt", "file2.txt"};
        int exitCode = Cat.process(args);
        assertEquals(4, exitCode);
    }

    @Test
    void testFileNotFound() {
        String[] args = {"nonexistentfile.txt"};
        int exitCode = Cat.process(args);
        assertEquals(5, exitCode);
    }
}
