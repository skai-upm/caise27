package es.upm.etsisi.park;

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

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    @Test
    void missingFileLoadsNoUsers() {
        assertTrue(new UserRepository(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        new UserRepository(file).save(List.of(ana(), pedro()));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTripPreservesAllAttributes() {
        UserRepository repo = new UserRepository(dir.resolve("users.txt"));
        repo.save(List.of(ana()));
        User loaded = repo.load().get(0);
        assertEquals(UserRepository.toLine(ana()), UserRepository.toLine(loaded));
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("broken;line", "", UserRepository.toLine(pedro())));
        List<User> users = new UserRepository(file).load();
        assertEquals(1, users.size());
        assertEquals("pedro@correo.com", users.get(0).getEmail());
    }

    @Test
    void savingReplacesPreviousContent() throws IOException {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(file);
        repo.save(List.of(ana(), pedro()));
        repo.save(List.of(pedro()));
        assertEquals(List.of(UserRepository.toLine(pedro())), Files.readAllLines(file));
    }
}
