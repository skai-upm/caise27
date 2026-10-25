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
    void savesOneSemicolonLinePerUserAndLoadsThemBack() throws IOException {
        Path file = dir.resolve("sub").resolve("users.txt");
        UserRepository repo = new UserRepository(file);
        repo.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = repo.load();
        assertEquals(2, loaded.size());
        assertEquals("Garcia Lopez", loaded.get(0).getSurname());
        assertEquals("pedro@correo.com", loaded.get(1).getEmail());
    }

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new UserRepository(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "Ana;Garcia\n\nPedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;"
                + "9876543210987654\n");
        List<User> loaded = new UserRepository(file).load();
        assertEquals(1, loaded.size());
        assertEquals("87654321X", loaded.get(0).getDni());
    }
}
