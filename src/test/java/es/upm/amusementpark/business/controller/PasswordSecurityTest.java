package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.presentation.AppBootstrap;

public class PasswordSecurityTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    public void setUp() throws IOException {
        park = start();
    }

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    private ParkController start() throws IOException {
        return AppBootstrap.start(new StoragePaths(dir), DAY, new BCryptPasswordHasher(4));
    }

    private ParkController restart() throws IOException {
        AppBootstrap.shutdown();
        return start();
    }

    private void createAna() throws OperationException {
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    public void passwordIsStoredAsHash() throws Exception {
        createAna();
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        assertTrue(content.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"));
        assertTrue(content.strip().endsWith(";612345678;1234567890123456"));
        assertNotEquals("Clave1!", park.getUsers().findByEmail("ana@correo.com").getPassword());
    }

    @Test
    public void formatRulesApplyBeforeHashing() {
        assertThrows(OperationException.class, () -> park.createUser("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertFalse(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    public void updateAndDeleteVerifyAgainstHash() throws Exception {
        createAna();
        assertThrows(OperationException.class, () -> park.updateUser("ana@correo.com", "Wrong1!", "NAME", "X"));
        park.updateUser("ana@correo.com", "Clave1!", "NAME", "X");
        assertThrows(OperationException.class, () -> park.deleteUser("ana@correo.com", "Wrong1!"));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertNull(park.getUsers().findByEmail("ana@correo.com"));
    }

    @Test
    public void changedPasswordStoresNewHashUsableAfterRestart() throws Exception {
        createAna();
        String oldHash = park.getUsers().findByEmail("ana@correo.com").getPassword();
        park.updateUser("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        assertThrows(OperationException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", "PASSWORD", "clave"));

        ParkController second = restart();
        String newHash = second.getUsers().findByEmail("ana@correo.com").getPassword();
        assertNotEquals(oldHash, newHash);
        assertTrue(newHash.startsWith("$2a$"));
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
        assertThrows(OperationException.class, () -> second.getUsers().authenticate("ana@correo.com", "Clave1!"));
        assertDoesNotThrow(() -> second.getUsers().authenticate("ana@correo.com", "Nueva2#"));
    }

    @Test
    public void legacyPlaintextPasswordStillAuthenticates() throws Exception {
        AppBootstrap.shutdown();
        Files.write(dir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        ParkController legacy = start();
        assertThrows(OperationException.class, () -> legacy.getUsers().authenticate("ana@correo.com", "Wrong1!"));
        legacy.updateUser("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
        assertEquals(1, legacy.getUsers().getUsers().size());
    }

    @Test
    public void hasherRecognisesHashes() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
        String hash = hasher.hash("Clave1!");
        assertEquals(60, hash.length());
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
        assertNotEquals(hash, hasher.hash("Clave1!"));
    }
}
