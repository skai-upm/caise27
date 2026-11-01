package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import es.upm.amusementpark.business.security.BCryptPasswordHasher;

import org.junit.jupiter.api.Test;

class PasswordSecurityTest extends ParkTestSupport {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private String storedPassword() throws IOException {
        return Files.readAllLines(tempDir.resolve("users.txt")).get(0).split(";")[4];
    }

    @Test
    void creationStoresHashNotPlaintext() throws IOException {
        ok(ANA);
        String content = Files.readString(tempDir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"), content);
        assertTrue(BCryptPasswordHasher.isHash(storedPassword()));
    }

    @Test
    void verifiesPasswordOnDeleteAndUpdate() {
        ok(ANA);
        error("USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999");
        ok("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        error("USER DELETE;ana@correo.com;Wrong1!");
        assertNotNull(users.findByEmail("ana@correo.com"));
        ok("USER DELETE;ana@correo.com;Clave1!");
        assertNull(users.findByEmail("ana@correo.com"));
    }

    @Test
    void passwordChangePersistsNewHash() throws IOException {
        ok(ANA);
        String before = storedPassword();
        ok("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#");
        String after = storedPassword();
        assertNotEquals(before, after);
        assertTrue(BCryptPasswordHasher.isHash(after));
        restart();
        error("USER UPDATE;ana@correo.com;Clave1!;PHONE;1");
        ok("USER UPDATE;ana@correo.com;Nueva2#;PHONE;1");
    }

    @Test
    void formatRulesApplyBeforeHashing() throws IOException {
        error("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456");
        assertFalse(Files.exists(tempDir.resolve("users.txt")));
        ok(ANA);
        String before = storedPassword();
        error("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!");
        assertEquals(before, storedPassword());
    }

    @Test
    void legacyPlaintextUserStillAuthenticatesWithoutSilentMigration() throws IOException {
        stopPark();
        Files.write(tempDir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        startPark();
        error("USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999");
        ok("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertEquals("Clave1!", storedPassword());
        ok("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#");
        assertTrue(BCryptPasswordHasher.isHash(storedPassword()));
    }

    @Test
    void hasherRejectsMalformedHashes() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
        String hash = hasher.hash("Clave1!");
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
        String broken = "$2b$" + hash.substring(4);
        assertFalse(hasher.matches("Clave1!", broken));
    }
}
