package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(new FileUserRepository(dir.resolve("users.txt")).loadAll().isEmpty());
    }

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new FileUserRepository(file).saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTripRestoresAllAttributes() throws IOException {
        FileUserRepository repository = new FileUserRepository(dir.resolve("users.txt"));
        repository.saveAll(List.of(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456")));

        List<User> loaded = repository.loadAll();

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
        Path file = dir.resolve("users.txt");
        Files.writeString(file, "\nbroken;line\n"
                + "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");

        List<User> loaded = new FileUserRepository(file).loadAll();

        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getDni());
    }
}
