package es.upm.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.controller.ParkTestSupport;
import es.upm.amusementpark.business.controller.UserController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordSecurityTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
    }

    private String storedHash() {
        return UserController.getInstance().findByEmail("ana@correo.com").getPasswordHash();
    }

    @Test
    void hasherProducesVerifiableSaltedHashes() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
        String first = hasher.hash("Clave1!");
        String second = hasher.hash("Clave1!");
        assertTrue(BCryptPasswordHasher.isBCryptHash(first));
        assertNotEquals(first, second);
        assertTrue(hasher.matches("Clave1!", first));
        assertFalse(hasher.matches("Clave2!", first));
        assertTrue(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches(null, first));
        assertEquals(BCryptPasswordHasher.DEFAULT_LOG_ROUNDS, 10);
    }

    @Test
    void plaintextIsNotStored() throws IOException {
        s.run(ParkTestSupport.ANA);
        String file = Files.readString(dir.resolve("users.txt"), StandardCharsets.UTF_8);
        assertFalse(file.contains("Clave1!"));
        assertTrue(BCryptPasswordHasher.isBCryptHash(storedHash()));
    }

    @Test
    void formatRulesApplyBeforeHashing() {
        assertTrue(s.run("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;clave1!;6;1234567890123456").startsWith("ERROR"));
        s.run(ParkTestSupport.ANA);
        String before = storedHash();
        assertTrue(s.run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!").startsWith("ERROR"));
        assertEquals(before, storedHash());
    }

    @Test
    void deleteVerifiesPassword() {
        s.run(ParkTestSupport.ANA);
        assertTrue(s.run("USER DELETE;ana@correo.com;Wrong1!").startsWith("ERROR"));
        assertTrue(s.run("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        assertNull(UserController.getInstance().findByEmail("ana@correo.com"));
    }

    @Test
    void passwordChangeStoresNewHash() throws IOException {
        s.run(ParkTestSupport.ANA);
        String before = storedHash();
        assertTrue(s.run("USER UPDATE;ana@correo.com;Wrong1!;PASSWORD;Nueva2#").startsWith("ERROR"));
        assertTrue(s.run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK"));
        s.restart();
        String after = storedHash();
        assertNotEquals(before, after);
        assertTrue(BCryptPasswordHasher.isBCryptHash(after));
        assertTrue(s.run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999").startsWith("ERROR"));
        assertTrue(s.run("USER UPDATE;ana@correo.com;Nueva2#;PHONE;699999999").startsWith("OK"));
    }

    @Test
    void legacyPlaintextEntriesStillAuthenticate() throws IOException {
        Files.write(dir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        s.restart();
        assertTrue(s.run("USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999").startsWith("ERROR"));
        assertTrue(s.run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999").startsWith("OK"));
        assertEquals("Clave1!", storedHash());
    }
}
