package es.upm.amusementpark.persistence.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import es.upm.amusementpark.business.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextUserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() {
        assertTrue(new TextUserRepository(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void roundTripsUsers() throws Exception {
        Path file = dir.resolve("users.txt");
        TextUserRepository repo = new TextUserRepository(file);
        repo.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = repo.load();
        assertEquals(2, loaded.size());
        assertEquals("87654321X", loaded.get(1).getDni());
        assertEquals("Otra2#", loaded.get(1).getPasswordHash());
    }

    @Test
    void skipsMalformedLines() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "broken;line\n\nAna;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        assertEquals(1, new TextUserRepository(file).load().size());
    }
}
