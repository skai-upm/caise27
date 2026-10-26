package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TemporaryDataDirectoryTest {

    @TempDir
    Path root;

    @Test
    void namedAfterTimestampWithUniqueSuffix() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 26, 19, 3, 15);
        TemporaryDataDirectory first = TemporaryDataDirectory.create(root, now);
        TemporaryDataDirectory second = TemporaryDataDirectory.create(root, now);
        assertEquals(root.resolve("20260826_190315"), first.getPath());
        assertEquals(root.resolve("20260826_190315_1"), second.getPath());
        assertTrue(Files.isDirectory(first.getPath()));
    }

    @Test
    void deleteRemovesNestedFiles() throws Exception {
        TemporaryDataDirectory tmp = TemporaryDataDirectory.create(root, LocalDateTime.now());
        Files.writeString(tmp.getPath().resolve("users.txt"), "x");
        Files.createDirectories(tmp.getPath().resolve("sub"));
        Files.writeString(tmp.getPath().resolve("sub/groups.txt"), "y");
        tmp.delete();
        assertTrue(Files.notExists(tmp.getPath()));
        tmp.delete();
    }
}
