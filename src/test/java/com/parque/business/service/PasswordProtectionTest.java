package com.parque.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.persistence.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordProtectionTest {
    @TempDir
    Path dir;
    Path file;
    BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
    UserService users;

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        users = new UserService(new UserRepository(file), hasher);
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private String storedPassword(int line) throws Exception {
        return Files.readAllLines(file).get(line).split(";")[4];
    }

    @Test
    void hasherBasics() {
        String h1 = hasher.hash("Clave1!");
        String h2 = hasher.hash("Clave1!");
        assertTrue(h1.startsWith("$2"));
        assertNotEquals("Clave1!", h1);
        assertNotEquals(h1, h2, "salted");
        assertTrue(hasher.matches("Clave1!", h1));
        assertFalse(hasher.matches("Mala1!", h1));
        assertFalse(hasher.matches("Clave1!", "Clave1!"), "plaintext is never a valid hash");
        assertFalse(hasher.matches("Clave1!", null));
        assertFalse(hasher.matches("Clave1!", "$2a$garbage"));
    }

    @Test
    void fileContainsHashNotPlaintext() throws Exception {
        String content = Files.readString(file);
        assertFalse(content.contains("Clave1!"));
        assertTrue(hasher.matches("Clave1!", storedPassword(0)));
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Clave1!", "623456789",
                "9876543210987654");
        assertNotEquals(storedPassword(0), storedPassword(1), "same password, different hashes");
    }

    @Test
    void wrongPasswordRejectedAndNothingChanges() throws Exception {
        List<String> before = Files.readAllLines(file);
        assertThrows(IllegalArgumentException.class, () -> users.delete("ana@correo.com", "Mala1!"));
        assertThrows(IllegalArgumentException.class,
                () -> users.update("ana@correo.com", "Mala1!", com.parque.business.model.UserField.PHONE, "699999999"));
        assertEquals(before, Files.readAllLines(file));
        users.delete("ana@correo.com", "Clave1!");
        assertEquals(List.of(), Files.readAllLines(file));
    }

    @Test
    void rotationPersistsNewHashAndSurvivesRestart() throws Exception {
        String old = storedPassword(0);
        users.update("ana@correo.com", "Clave1!", com.parque.business.model.UserField.PASSWORD, "Nueva2#");
        assertNotEquals(old, storedPassword(0));
        assertFalse(Files.readString(file).contains("Nueva2#"));
        UserService restarted = new UserService(new UserRepository(file), hasher);
        assertThrows(IllegalArgumentException.class, () -> restarted.authenticate("ana@correo.com", "Clave1!"));
        restarted.update("ana@correo.com", "Nueva2#", com.parque.business.model.UserField.PHONE, "699999999");
    }

    @Test
    void e1RulesApplyBeforeHashing() throws Exception {
        List<String> before = Files.readAllLines(file);
        assertThrows(IllegalArgumentException.class, () -> users.create("Pe", "X", "87654321X", "pe@correo.com",
                "clave1!", "600", "1234567890123456"));
        assertThrows(IllegalArgumentException.class,
                () -> users.update("ana@correo.com", "Clave1!", com.parque.business.model.UserField.PASSWORD, "Clave12"));
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void legacyPlaintextRecordDoesNotAuthenticate() throws Exception {
        Files.write(file, List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        UserService legacy = new UserService(new UserRepository(file), hasher);
        assertThrows(IllegalArgumentException.class, () -> legacy.delete("ana@correo.com", "Clave1!"));
    }
}
