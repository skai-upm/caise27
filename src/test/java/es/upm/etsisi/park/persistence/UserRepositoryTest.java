package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.user.User;

class UserRepositoryTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream warnings = new ByteArrayOutputStream();

    private UserRepository repository(Path file) {
        return new UserRepository(file, new PrintStream(warnings, true, StandardCharsets.UTF_8));
    }

    @Test
    void savesInSpecifiedFormatAndLoadsBack() throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repo = repository(file);
        repo.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = repository(file).load();
        assertEquals(2, loaded.size());
        assertEquals("Ana", loaded.get(0).getFirstName());
        assertEquals("9876543210987654", loaded.get(1).getCard());
        assertTrue(Files.notExists(dir.resolve("users.txt.tmp")));
    }

    @Test
    void missingFileIsEmpty() throws Exception {
        assertTrue(repository(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    void skipsMalformedAndBlankLines() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line;here"));
        List<User> loaded = repository(file).load();
        assertEquals(1, loaded.size());
        assertTrue(warnings.toString(StandardCharsets.UTF_8).contains("line 3"));
    }

    @Test
    void savingEmptyListClearsFile() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        repository(file).save(List.of());
        assertTrue(repository(file).load().isEmpty());
    }
}
