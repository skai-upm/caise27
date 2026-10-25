package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    public void savesOneSemicolonSeparatedLinePerUser() throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repository = new UserRepository(file);
        repository.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
    }

    @Test
    public void loadsWhatWasSaved() throws Exception {
        UserRepository repository = new UserRepository(dir.resolve("users.txt"));
        repository.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456")));
        List<User> users = repository.load();
        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals("Ana", user.getName());
        assertEquals("Garcia Lopez", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    public void missingFileLoadsEmpty() throws Exception {
        assertTrue(new UserRepository(dir.resolve("none.txt")).load().isEmpty());
    }

    @Test
    public void malformedLinesAreSkipped() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "garbage",
                "",
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "a;b;c"));
        List<User> users = new UserRepository(file).load();
        assertEquals(1, users.size());
        assertEquals("12345678Z", users.get(0).getDni());
    }
}
