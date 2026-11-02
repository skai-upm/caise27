package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.business.user.User;

class FileUserRepositoryTest {

    @TempDir
    Path dir;

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new FileUserRepository(file).save(List.of(ana()));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void roundTripsUsers() throws IOException {
        FileUserRepository storage = new FileUserRepository(dir.resolve("users.txt"));
        storage.save(List.of(ana()));
        List<User> loaded = storage.load();
        assertEquals(1, loaded.size());
        User u = loaded.get(0);
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(new FileUserRepository(dir.resolve("absent.txt")).load().isEmpty());
    }

    @Test
    void skipsMalformedLinesWithWarning() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line"), StandardCharsets.UTF_8);
        FileUserRepository storage = new FileUserRepository(file);
        assertEquals(1, storage.load().size());
        assertEquals(1, storage.getWarnings().size());
    }
}
