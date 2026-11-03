package es.upm.etsisi.park;

import es.upm.etsisi.park.business.PasswordHasher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PasswordSecurityTest {

    @TempDir
    Path dir;

    @Test
    void storesOnlyBcryptHashes() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA);
        String stored = Files.readString(dir.resolve("users.txt"));
        assertFalse(stored.contains("Clave1!"));
        String hash = stored.strip().split(";")[4];
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordHasher.matches("Clave1!", hash));
    }

    @Test
    void invalidPasswordRejectedBeforeHashing() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        assertTrue(f.run(ParkFixture.ANA.replace("Clave1!", "clave1!")).startsWith("ERROR: invalid password"));
        f.run(ParkFixture.ANA);
        assertTrue(f.run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave").startsWith("ERROR: invalid password"));
    }

    @Test
    void onlyNewPasswordAuthenticatesAfterChange() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA);
        String before = Files.readString(dir.resolve("users.txt"));
        assertEquals("USER UPDATED", f.run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertFalse(Files.readString(dir.resolve("users.txt")).equals(before));
        f.restart();
        assertEquals("ERROR: incorrect password", f.run("USER UPDATE;ana@correo.com;Clave1!;PHONE;600000000"));
        assertEquals("USER UPDATED", f.run("USER UPDATE;ana@correo.com;Nueva2#;PHONE;600000000"));
    }

    @Test
    void legacyPlaintextRecordFailsGracefully() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        ParkFixture f = new ParkFixture(dir);
        assertEquals("ERROR: incorrect password", f.run("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("NO USERS INSIDE", f.run("ATTRACTION CREATE;A01;X;1", "ATTRACTION LIST;A01"));
    }
}
