package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;

class TemporaryDirectoryTest {

    @TempDir
    Path base;

    @Test
    void namesDirectoryAfterTimestamp() throws IOException {
        Path dir = TemporaryDirectory.create(base.resolve("tmp"), Fixtures.START);
        assertEquals(base.resolve("tmp").resolve("20260826_190315"), dir);
        assertTrue(Files.isDirectory(dir));
    }

    @Test
    void addsSuffixWhenNameIsTaken() throws IOException {
        Path first = TemporaryDirectory.create(base, Fixtures.START);
        Path second = TemporaryDirectory.create(base, Fixtures.START);
        Path third = TemporaryDirectory.create(base, Fixtures.START);
        assertEquals("20260826_190315", first.getFileName().toString());
        assertEquals("20260826_190315_1", second.getFileName().toString());
        assertEquals("20260826_190315_2", third.getFileName().toString());
    }

    @Test
    void deletesDirectoryWithContents() throws IOException {
        Path dir = TemporaryDirectory.create(base, Fixtures.START);
        Files.write(dir.resolve("users.txt"), List.of("x"));
        Files.createDirectories(dir.resolve("nested"));
        Files.write(dir.resolve("nested").resolve("f.txt"), List.of("y"));
        TemporaryDirectory.deleteRecursively(dir);
        assertFalse(Files.exists(dir));
        TemporaryDirectory.deleteRecursively(dir);
        assertFalse(Files.exists(dir));
    }
}
