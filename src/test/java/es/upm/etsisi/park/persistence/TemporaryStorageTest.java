package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemporaryStorageTest {

    @TempDir
    Path base;

    @Test
    void createsTimestampedDirectoryAndDeletesIt() throws IOException {
        LocalDateTime now = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        Path created;
        try (TemporaryStorage storage = TemporaryStorage.create(base, now)) {
            created = storage.getDirectory();
            assertEquals(base.resolve("tmp").resolve("20260826_190315"), created);
            Files.writeString(created.resolve("users.txt"), "x");
        }
        assertFalse(Files.exists(created));
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void sameSecondGetsUniqueDirectoryAndSharedRootIsKept() throws IOException {
        LocalDateTime now = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        try (TemporaryStorage first = TemporaryStorage.create(base, now)) {
            try (TemporaryStorage second = TemporaryStorage.create(base, now)) {
                assertNotEquals(first.getDirectory(), second.getDirectory());
            }
            assertTrue(Files.exists(first.getDirectory()), "other run's directory untouched");
        }
        assertFalse(Files.exists(base.resolve("tmp")));
    }
}
