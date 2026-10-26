package es.upm.parque.persistence;

import es.upm.parque.business.model.User;

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
    Path base;

    private final LocalDateTime start = LocalDateTime.of(2026, 8, 26, 19, 3, 15);

    @Test
    void namedAfterTimestamp() throws IOException {
        TemporaryDataDirectory directory = TemporaryDataDirectory.create(base.resolve("tmp"), start);
        assertEquals(base.resolve("tmp").resolve("20260826_190315"), directory.getPath());
        assertTrue(Files.isDirectory(directory.getPath()));
    }

    @Test
    void sameSecondGetsSuffix() throws IOException {
        TemporaryDataDirectory first = TemporaryDataDirectory.create(base, start);
        TemporaryDataDirectory second = TemporaryDataDirectory.create(base, start);
        assertEquals("20260826_190315_1", second.getPath().getFileName().toString());
        assertTrue(Files.isDirectory(first.getPath()));
    }

    @Test
    void deleteRemovesEverythingAndIsIdempotent() throws IOException {
        TemporaryDataDirectory directory = TemporaryDataDirectory.create(base, start);
        UserRepository repository = new UserRepository(directory.getPath().resolve("users.txt"));
        repository.saveAll(java.util.List.of(new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456")));
        Files.createDirectories(directory.getPath().resolve("sub"));
        Files.writeString(directory.getPath().resolve("sub").resolve("x.txt"), "x");

        directory.delete();
        assertFalse(Files.exists(directory.getPath()));
        directory.delete();
        assertTrue(Files.isDirectory(base));
    }
}
