package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemporaryDataDirectoryTest {

    @TempDir
    Path root;

    @Test
    void namedAfterTimestampWithSuffixWhenTaken() throws IOException {
        LocalDateTime start = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        Path first = TemporaryDataDirectory.create(root.resolve("tmp"), start);
        Path second = TemporaryDataDirectory.create(root.resolve("tmp"), start);
        assertEquals(root.resolve("tmp").resolve("20260826_190315"), first);
        assertEquals(root.resolve("tmp").resolve("20260826_190315_1"), second);
        assertTrue(Files.isDirectory(first) && Files.isDirectory(second));
    }

    @Test
    void deleteRecursivelyRemovesTreeOnly() throws IOException {
        Path dir = TemporaryDataDirectory.create(root, LocalDateTime.of(2026, 1, 1, 0, 0));
        Files.writeString(dir.resolve("users.txt"), "x");
        Files.createDirectories(dir.resolve("sub"));
        Files.writeString(dir.resolve("sub").resolve("a.txt"), "y");
        Path other = Files.createDirectories(root.resolve("other"));
        TemporaryDataDirectory.deleteRecursively(dir);
        assertFalse(Files.exists(dir));
        assertTrue(Files.exists(other));
        TemporaryDataDirectory.deleteRecursively(dir);
    }
}
