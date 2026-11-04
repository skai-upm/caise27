package es.upm.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataDirectoryTest {

    @TempDir
    Path dir;

    @Test
    void runDirectoriesAreUniqueAndTimestamped() throws IOException {
        Path tmpRoot = dir.resolve("tmp");
        Path first = DataDirectory.createRunDirectory(tmpRoot);
        Path second = DataDirectory.createRunDirectory(tmpRoot);
        assertNotEquals(first, second);
        assertTrue(first.getFileName().toString().matches("\\d{8}_\\d{6}(_\\d+)?"));
        Files.writeString(first.resolve("users.txt"), "x");
        Files.createDirectories(first.resolve("nested"));
        DataDirectory.deleteQuietly(first);
        DataDirectory.deleteQuietly(second);
        try (Stream<Path> s = Files.list(tmpRoot)) {
            assertEquals(0, s.count());
        }
    }

    @Test
    void deletingMissingDirectoryIsHarmless() {
        DataDirectory.deleteQuietly(dir.resolve("absent"));
        assertFalse(Files.exists(dir.resolve("absent")));
    }
}
