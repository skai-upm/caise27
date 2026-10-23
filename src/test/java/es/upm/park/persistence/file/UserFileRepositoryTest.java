package es.upm.park.persistence.file;

import es.upm.park.business.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserFileRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileLoadsNoUsers() {
        assertTrue(new UserFileRepository(dir.resolve("users.txt")).loadAll().isEmpty());
    }

    @Test
    void writesOneLinePerUserInSpecifiedFormat() throws IOException {
        Path file = dir.resolve("users.txt");
        UserFileRepository repository = new UserFileRepository(file);
        repository.saveAll(List.of(
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
    void roundTripsUsers() {
        Path file = dir.resolve("users.txt");
        new UserFileRepository(file).saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456")));

        List<User> loaded = new UserFileRepository(file).loadAll();
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getDni());
        assertEquals("Clave1!", loaded.get(0).getPasswordHash());
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line",
                "X;Y;;a@b.com;P;1;2"));

        List<User> loaded = new UserFileRepository(file).loadAll();
        assertEquals(1, loaded.size());
        assertEquals("ana@correo.com", loaded.get(0).getEmail());
    }

    @Test
    void savingEmptyListClearsFile() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "old content\n");
        new UserFileRepository(file).saveAll(List.of());
        assertEquals(0, Files.size(file));
    }
}
