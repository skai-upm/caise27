package es.upm.parque;

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

class DataDirectoryTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 26, 19, 3, 15);

    @TempDir
    Path dir;

    @Test
    void temporaryDirectoryIsNamedAfterTheStartTime() {
        DataDirectory data = DataDirectory.temporary(dir.resolve("tmp"), START);
        assertEquals(dir.resolve("tmp/20260826_190315"), data.getPath());
        assertTrue(data.isTemporary());
        assertTrue(Files.isDirectory(data.getPath()));
        assertEquals(data.getPath().resolve("users.txt"), data.resolve("users.txt"));
    }

    @Test
    void sameSecondGetsADistinctDirectory() {
        DataDirectory first = DataDirectory.temporary(dir, START);
        DataDirectory second = DataDirectory.temporary(dir, START);
        assertNotEquals(first.getPath(), second.getPath());
        assertEquals(dir.resolve("20260826_190315_1"), second.getPath());
    }

    @Test
    void closeDeletesTemporaryDirectoryWithItsFiles() throws IOException {
        DataDirectory data = DataDirectory.temporary(dir, START);
        Files.writeString(data.resolve("users.txt"), "x");
        Files.createDirectories(data.resolve("nested"));
        Files.writeString(data.resolve("nested/file.txt"), "y");
        data.close();
        assertFalse(Files.exists(data.getPath()));
        data.close();
    }

    @Test
    void permanentDirectoryIsNeverDeleted() throws IOException {
        Path permanent = Files.createDirectories(dir.resolve("data"));
        Files.writeString(permanent.resolve("users.txt"), "x");
        DataDirectory data = DataDirectory.permanent(permanent);
        data.close();
        assertFalse(data.isTemporary());
        assertTrue(Files.exists(permanent.resolve("users.txt")));
    }
}
