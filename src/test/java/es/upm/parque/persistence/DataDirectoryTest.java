package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataDirectoryTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-26T19:03:15Z"), ZoneOffset.UTC);

    @TempDir
    Path base;

    @Test
    void createsTimestampedDirectoryWithSuffixOnCollision() {
        Path first = DataDirectory.createTemporary(base, CLOCK);
        assertEquals(base.resolve("tmp").resolve("20260826_190315"), first);
        assertTrue(Files.isDirectory(first));
        assertEquals(base.resolve("tmp").resolve("20260826_190315_1"), DataDirectory.createTemporary(base, CLOCK));
        assertEquals(base.resolve("tmp").resolve("20260826_190315_2"), DataDirectory.createTemporary(base, CLOCK));
    }

    @Test
    void deletesNestedContentAndIgnoresMissingDirectory() throws IOException {
        Path dir = DataDirectory.createTemporary(base, CLOCK);
        Files.createDirectories(dir.resolve("sub"));
        Files.writeString(dir.resolve("users.txt"), "x");
        Files.writeString(dir.resolve("sub").resolve("f.txt"), "y");
        DataDirectory.deleteRecursively(dir);
        assertFalse(Files.exists(dir));
        DataDirectory.deleteRecursively(dir);
        assertFalse(Files.exists(dir));
    }

    @Test
    void creationFailureIsUnchecked() throws IOException {
        Files.writeString(base.resolve("tmp"), "not a directory");
        assertThrows(UncheckedIOException.class, () -> DataDirectory.createTemporary(base, CLOCK));
    }
}
