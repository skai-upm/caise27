package com.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void missingFileLoadsNoUsers() throws IOException {
        assertTrue(new UserFileStorage(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void savesOneSemicolonSeparatedLinePerUserAndReloadsThem() throws IOException {
        Path file = dir.resolve("users.txt");
        UserFileStorage storage = new UserFileStorage(file);
        storage.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));

        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));

        List<User> loaded = new UserFileStorage(file).load();
        assertEquals(2, loaded.size());
        User pedro = loaded.get(1);
        assertEquals("Pedro", pedro.getName());
        assertEquals("Lopez Martin", pedro.getSurname());
        assertEquals("87654321X", pedro.getDni());
        assertEquals("pedro@correo.com", pedro.getEmail());
        assertEquals("Otra2#", pedro.getPassword());
        assertEquals("623456789", pedro.getPhone());
        assertEquals("9876543210987654", pedro.getCard());
    }

    @Test
    void skipsMalformedAndBlankLines() throws IOException {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "broken;line"));
        List<User> loaded = new UserFileStorage(file).load();
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getDni());
    }
}
