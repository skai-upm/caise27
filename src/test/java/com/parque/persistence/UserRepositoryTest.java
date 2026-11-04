package com.parque.persistence;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.business.service.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void missingFileMeansNoUsers() throws Exception {
        assertTrue(new UserRepository(dir.resolve("users.txt")).load().isEmpty());
    }

    @Test
    void roundTripsOneLinePerUser() throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(file);
        repo.save(List.of(
                new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                        "1234567890123456"),
                new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                        "9876543210987654")));
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
        List<User> loaded = repo.load();
        assertEquals(2, loaded.size());
        assertEquals("Lopez Martin", loaded.get(1).getSurname());
        assertEquals("9876543210987654", loaded.get(1).getCard());
    }

    @Test
    void skipsMalformedLines() throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, List.of("broken;line",
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456", ""));
        List<User> loaded = new UserRepository(file).load();
        assertEquals(1, loaded.size());
        assertEquals("12345678Z", loaded.get(0).getDni());
    }
}
