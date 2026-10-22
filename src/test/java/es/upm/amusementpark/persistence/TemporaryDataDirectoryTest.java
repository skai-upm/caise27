package es.upm.amusementpark.persistence;

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

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 26, 19, 3, 15);

    @TempDir
    Path dir;

    @Test
    void createsTimestampedDirectoryAndDeletesEverything() throws IOException {
        Path root = dir.resolve("tmp");
        TemporaryDataDirectory temporary = TemporaryDataDirectory.create(root, NOW);
        assertEquals(root.resolve("20260826_190315"), temporary.getDirectory());
        Files.writeString(temporary.getDirectory().resolve("users.txt"), "data");
        Files.createDirectories(temporary.getDirectory().resolve("nested"));
        Files.writeString(temporary.getDirectory().resolve("nested/x.txt"), "x");

        temporary.close();
        assertFalse(Files.exists(temporary.getDirectory()));
        assertFalse(Files.exists(root), "root created by this run is removed when empty");
    }

    @Test
    void addsSuffixWhenNameIsTaken() throws IOException {
        Path root = dir.resolve("tmp");
        try (TemporaryDataDirectory first = TemporaryDataDirectory.create(root, NOW);
             TemporaryDataDirectory second = TemporaryDataDirectory.create(root, NOW)) {
            assertEquals("20260826_190315", first.getDirectory().getFileName().toString());
            assertEquals("20260826_190315_1", second.getDirectory().getFileName().toString());
        }
    }

    @Test
    void keepsPreexistingRootAndOtherContent() throws IOException {
        Path root = Files.createDirectories(dir.resolve("tmp"));
        Path other = Files.createDirectory(root.resolve("keep"));
        TemporaryDataDirectory temporary = TemporaryDataDirectory.create(root, NOW);
        temporary.close();
        assertTrue(Files.isDirectory(other));
        assertFalse(Files.exists(temporary.getDirectory()));
    }
}
