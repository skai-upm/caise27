package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(new UserRepository(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUserAndLoadsThemBack() throws IOException {
        Path file = dir.resolve("data/users.txt");
        UserRepository repository = new UserRepository(file);
        repository.saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = new UserRepository(file).load();
        assertEquals(2, loaded.size());
        assertEquals("Pedro", loaded.get(1).getName());
        assertEquals("9876543210987654", loaded.get(1).getCard());
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "bad;line\n\nAna;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456\n");
        List<User> loaded = new UserRepository(file).load();
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getDni());
    }
}
