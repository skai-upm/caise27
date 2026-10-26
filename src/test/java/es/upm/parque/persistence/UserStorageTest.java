package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileLoadsNoUsers() {
        assertTrue(new UserStorage(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void saveWritesOneLinePerUserAndLoadRestoresThem() throws IOException {
        Path file = dir.resolve("data/users.txt");
        UserStorage storage = new UserStorage(file);
        storage.save(List.of(
            new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
            new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654")));

        assertEquals(List.of(
            "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
            Files.readAllLines(file));

        List<User> loaded = storage.load();
        assertEquals(2, loaded.size());
        assertEquals("87654321X", loaded.get(1).getDni());
        assertEquals("Otra2#", loaded.get(1).getPassword());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("broken;line",
            "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertEquals(1, new UserStorage(file).load().size());
    }
}
