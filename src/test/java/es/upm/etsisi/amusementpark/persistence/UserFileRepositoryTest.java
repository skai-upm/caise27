package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.user.User;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileRepositoryTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();

    private UserFileRepository repo(Path file) {
        return new UserFileRepository(file, new PrintStream(warnings, true, StandardCharsets.UTF_8));
    }

    @Test
    void missingFileGivesNoUsers() throws Exception {
        assertTrue(repo(dir.resolve("users.txt")).loadAll().isEmpty());
    }

    @Test
    void roundTripUsesSourceFormat() throws Exception {
        Path file = dir.resolve("users.txt");
        List<User> users = List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        repo(file).saveAll(users);
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
        List<User> loaded = repo(file).loadAll();
        assertEquals(2, loaded.size());
        assertEquals("Garcia Lopez", loaded.get(0).getSurnames());
        assertEquals("9876543210987654", loaded.get(1).getCard());
        assertFalse(Files.exists(dir.resolve("users.txt.tmp")));
    }

    @Test
    void malformedAndBlankLinesSkipped() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "broken;line\n\nAna;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        List<User> loaded = repo(file).loadAll();
        assertEquals(1, loaded.size());
        assertTrue(warnings.toString(StandardCharsets.UTF_8).contains("line 1"));
    }
}
