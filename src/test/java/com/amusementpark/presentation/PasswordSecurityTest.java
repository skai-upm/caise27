package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.security.BCryptPasswordHasher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordSecurityTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.ok("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
    }

    private String storedPassword() throws IOException {
        return Files.readAllLines(dir.resolve("users.txt")).get(0).split(";", -1)[4];
    }

    @Test
    void storesOnlyBCryptHash() throws IOException {
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Clave1!"));
        assertTrue(BCryptPasswordHasher.isBCryptHash(storedPassword()));
    }

    @Test
    void deleteAndUpdateVerifyAgainstHash() {
        assertTrue(f.run("USER UPDATE;ana@correo.com;Mala1!;PHONE;6").startsWith("ERROR:"));
        assertTrue(f.run("USER DELETE;ana@correo.com;Mala1!").startsWith("ERROR:"));
        f.ok("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999", "USER DELETE;ana@correo.com;Clave1!");
    }

    @Test
    void passwordChangeStoresNewHashUsableAfterRestart() throws IOException {
        String before = storedPassword();
        f.ok("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#");
        String after = storedPassword();
        assertFalse(before.equals(after));
        assertTrue(BCryptPasswordHasher.isBCryptHash(after));
        f.restart();
        assertTrue(f.run("USER UPDATE;ana@correo.com;Clave1!;PHONE;6").startsWith("ERROR:"));
        f.ok("USER UPDATE;ana@correo.com;Nueva2#;PHONE;611111111");
    }

    @Test
    void formatRulesApplyBeforeHashing() throws IOException {
        String before = storedPassword();
        assertTrue(f.run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!").startsWith("ERROR:"));
        assertTrue(f.run("USER CREATE;Pedro;L;87654321X;pedro@correo.com;Clave12;6;1234567890123456")
                .startsWith("ERROR:"));
        assertEquals(before, storedPassword());
    }

    @Test
    void legacyPlaintextFileIsReadWithoutMigration() throws IOException {
        Files.write(dir.resolve("users.txt"),
                List.of("Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        f.restart();
        f.ok("USER UPDATE;pedro@correo.com;Otra2#;PHONE;600000000");
        assertTrue(Files.readString(dir.resolve("users.txt")).contains(";Otra2#;"), "not migrated silently");
        f.ok("USER UPDATE;pedro@correo.com;Otra2#;PASSWORD;Nueva2#");
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
    }

    @Test
    void hasherRejectsEmptyOrMissingValues() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
        assertFalse(hasher.matches("", ""));
        assertFalse(hasher.matches(null, "x"));
        assertTrue(hasher.matches("Clave1!", hasher.hash("Clave1!")));
    }
}
