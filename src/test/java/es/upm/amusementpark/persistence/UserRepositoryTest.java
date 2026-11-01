package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import es.upm.amusementpark.business.model.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void missingFileLoadsAsEmpty() throws IOException {
        UserRepository repository = new UserRepository(tempDir.resolve("users.txt"));
        assertTrue(repository.load().isEmpty());
    }

    @Test
    void savesOneUserPerLineInSpecifiedOrder() throws IOException {
        Path file = tempDir.resolve("users.txt");
        UserRepository repository = new UserRepository(file);
        repository.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void roundTripRestoresAllAttributes() throws IOException {
        Path file = tempDir.resolve("users.txt");
        new UserRepository(file).save(List.of(new User("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456")));

        List<User> loaded = new UserRepository(file).load();

        assertEquals(1, loaded.size());
        User user = loaded.get(0);
        assertEquals("Ana", user.getName());
        assertEquals("Garcia Lopez", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void skipsBlankAndMalformedLines() throws IOException {
        Path file = tempDir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line"), StandardCharsets.UTF_8);
        List<User> loaded = new UserRepository(file).load();
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getDni());
    }
}
