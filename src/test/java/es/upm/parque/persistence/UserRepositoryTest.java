package es.upm.parque.persistence;

import es.upm.parque.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileLoadsEmpty() throws Exception {
        assertTrue(new UserRepository(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void saveAndLoadRoundTrip() throws Exception {
        Path file = dir.resolve("sub/users.txt");
        UserRepository repo = new UserRepository(file);
        repo.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));

        List<String> lines = Files.readAllLines(file);
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456", lines.get(0));

        List<User> loaded = new UserRepository(file).load();
        assertEquals(2, loaded.size());
        assertEquals("pedro@correo.com", loaded.get(1).getEmail());
        assertEquals("9876543210987654", loaded.get(1).getCard());
    }

    @Test
    void malformedLinesAreSkipped() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "broken;line\nAna;Garcia;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        assertEquals(1, new UserRepository(file).load().size());
    }
}
