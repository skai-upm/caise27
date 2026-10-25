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
    Path tempDir;

    @Test
    public void missingFileMeansNoUsers() {
        UserRepository repository = new UserRepository(tempDir.resolve("users.txt"));
        assertTrue(repository.load().isEmpty());
    }

    @Test
    public void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = tempDir.resolve("data").resolve("users.txt");
        UserRepository repository = new UserRepository(file);
        repository.saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    public void roundTripRestoresUsers() {
        UserRepository repository = new UserRepository(tempDir.resolve("users.txt"));
        repository.saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456")));

        List<User> loaded = new UserRepository(tempDir.resolve("users.txt")).load();
        assertEquals(1, loaded.size());
        User ana = loaded.get(0);
        assertEquals("Ana", ana.getName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    public void malformedLinesAreSkipped() throws IOException {
        Path file = tempDir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "basura;sin;campos",
                "",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));

        List<User> loaded = new UserRepository(file).load();
        assertEquals(2, loaded.size());
        assertEquals("87654321X", loaded.get(1).getDni());
    }
}
