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

    @Test
    void missingFileMeansNoUsers() throws IOException {
        assertTrue(new UserRepository(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void writesOneSemicolonSeparatedLinePerUser() throws IOException {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(file);
        repo.saveAll(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    void roundTripsUsers() throws IOException {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(file);
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        repo.saveAll(List.of(ana));
        List<User> loaded = new UserRepository(file).load();
        assertEquals(1, loaded.size());
        assertEquals(UserRepository.toLine(ana), UserRepository.toLine(loaded.get(0)));
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        assertEquals(2, new UserRepository(file).load().size());
    }

    @Test
    void createsMissingParentDirectories() throws IOException {
        Path file = dir.resolve("nested/data/users.txt");
        new UserRepository(file).saveAll(List.of());
        assertTrue(Files.exists(file));
    }
}
