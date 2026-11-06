package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.model.User;
import es.upm.park.persistence.repository.ParkRepositories;

class PasswordProtectionTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();
    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @TempDir
    Path dir;

    @Test
    void hasherProtectsAndVerifies() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$04$"));
        assertNotEquals(hash, hasher.hash("Clave1!"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches(null, hash));
        assertFalse(hasher.matches("Clave1!", null));
    }

    @Test
    void defaultCostComesFromProperty() {
        assertTrue(new BCryptPasswordHasher().hash("Clave1!").startsWith("$2a$04$"));
    }

    @Test
    void fileNeverContainsThePasswordAndVerificationSurvivesRestart() throws Exception {
        park.start(ParkRepositories.load(dir), DAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        String line = Files.readAllLines(dir.resolve("users.txt")).get(0);
        assertFalse(line.contains("Clave1!"));
        park.start(ParkRepositories.load(dir), DAY);
        assertEquals("incorrect password",
                assertThrows(ParkException.class, () -> park.deleteUser("ana@correo.com", "Clave2!")).getMessage());
        User ana = park.getUserService().update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        String newLine = Files.readAllLines(dir.resolve("users.txt")).get(0);
        assertNotEquals(line, newLine);
        assertTrue(newLine.contains(ana.getPassword()));
        assertThrows(ParkException.class, () -> park.getUserService().authenticate("ana@correo.com", "Clave1!"));
        park.start(ParkRepositories.load(dir), DAY);
        park.deleteUser("ana@correo.com", "Nueva2#");
    }

    @Test
    void invalidPasswordRejectedBeforeHashing() throws Exception {
        park.start(ParkRepositories.load(dir), DAY);
        assertEquals("invalid password", assertThrows(ParkException.class, () -> park.getUserService()
                .create("Ana", "G", "12345678Z", "ana@correo.com", "clave1!", "6", "1234567890123456")).getMessage());
        assertTrue(Files.notExists(dir.resolve("users.txt")));
    }

    @Test
    void legacyPlainTextPasswordDoesNotVerify() throws Exception {
        Files.write(dir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        park.start(ParkRepositories.load(dir), DAY);
        assertEquals("incorrect password", assertThrows(ParkException.class,
                () -> park.deleteUser("ana@correo.com", "Clave1!")).getMessage());
    }
}
