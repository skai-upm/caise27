package es.upm.etsisi.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "$2a$04$hash", "612345678", "1234567890123456");
    }

    private static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "$2a$04$other", "623456789", "9876543210987654");
    }

    private static void save(UserFileStore store, List<User> users) {
        TextFile.writeAll(Map.of(store.getFile(), store.toLines(users)));
    }

    @Test
    void missingFileLoadsNoUsers() {
        assertTrue(new UserFileStore(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonLinePerUser() throws IOException {
        UserFileStore store = new UserFileStore(dir.resolve("users.txt"));
        save(store, List.of(ana(), pedro()));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$04$hash;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;$2a$04$other;623456789;9876543210987654"),
                Files.readAllLines(store.getFile()));
    }

    @Test
    void roundTripPreservesAllAttributes() {
        UserFileStore store = new UserFileStore(dir.resolve("users.txt"));
        save(store, List.of(ana()));
        assertEquals(UserFileStore.toLine(ana()), UserFileStore.toLine(store.load().get(0)));
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("broken;line", "", UserFileStore.toLine(pedro())));
        List<User> users = new UserFileStore(file).load();
        assertEquals(1, users.size());
        assertEquals("pedro@correo.com", users.get(0).getEmail());
    }
}
