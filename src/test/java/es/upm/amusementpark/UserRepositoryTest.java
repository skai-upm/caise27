package es.upm.amusementpark;

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
    void missingFileLoadsEmpty() throws IOException {
        assertTrue(new UserRepository(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void saveWritesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserRepository(file).save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTrip() throws IOException {
        UserRepository repo = new UserRepository(dir.resolve("users.txt"));
        repo.save(List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456")));
        List<User> loaded = repo.load();
        assertEquals(1, loaded.size());
        User u = loaded.get(0);
        assertEquals("Ana", u.getName());
        assertEquals("Garcia Lopez", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void malformedAndBlankLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("bad;line", "", "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertEquals(1, new UserRepository(file).load().size());
    }
}
