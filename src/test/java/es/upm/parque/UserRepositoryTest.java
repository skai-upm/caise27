package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(new UserRepository(dir.resolve("users.txt")).loadAll().isEmpty());
    }

    @Test
    void savesAndLoadsUsers() throws IOException {
        Path file = dir.resolve("sub").resolve("users.txt");
        UserRepository repository = new UserRepository(file);
        repository.saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = repository.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("87654321X", loaded.get(1).getDni());
        assertEquals("Otra2#", loaded.get(1).getPassword());
    }

    @Test
    void skipsMalformedLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "broken;line\nAna;Garcia;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        assertEquals(1, new UserRepository(file).loadAll().size());
    }
}
