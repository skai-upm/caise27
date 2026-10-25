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
        assertTrue(new UserRepository(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("data").resolve("users.txt");
        UserRepository repository = new UserRepository(file);
        repository.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void loadsWhatWasSaved() throws IOException {
        UserRepository repository = new UserRepository(dir.resolve("users.txt"));
        repository.save(List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456")));
        List<User> users = repository.load();
        assertEquals(1, users.size());
        User ana = users.get(0);
        assertEquals("Ana", ana.getName());
        assertEquals("Garcia Lopez", ana.getSurname());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void ignoresMalformedLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("", "solo;tres;campos",
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertEquals(1, new UserRepository(file).load().size());
    }
}
