package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordSecurityTest {

    @TempDir
    Path dir;

    @Test
    void storesOnlyTheHash() throws IOException {
        TestPark.withUsers(dir);

        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        String ana = Files.readAllLines(dir.resolve("users.txt")).get(0);
        String hash = ana.split(";")[4];
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(ana.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"));
        assertTrue(ana.endsWith(";612345678;1234567890123456"));
    }

    @Test
    void verifiesAfterRestartAndOnPasswordChange() throws IOException {
        TestPark.withUsers(dir);
        Park park = TestPark.configure(dir);
        String before = Files.readAllLines(dir.resolve("users.txt")).get(0).split(";")[4];

        park.getUserController().update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        park.getUserController().update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");

        String after = Files.readAllLines(dir.resolve("users.txt")).get(0).split(";")[4];
        assertFalse(before.equals(after));
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
        assertThrows(AuthenticationException.class,
                () -> park.getUserController().update("ana@correo.com", "Clave1!", "PHONE", "6"));
        assertEquals("ana@correo.com", park.getUserController().authenticate("ana@correo.com", "Nueva2#").getEmail());
    }

    @Test
    void wrongPasswordAndPlainTextDataNeverAuthenticate() throws IOException {
        Park park = TestPark.withUsers(dir);
        assertThrows(AuthenticationException.class, () -> park.getUserController().delete("ana@correo.com", "Wrong1!"));

        Files.write(dir.resolve("users.txt"), List.of("Old;User;87654321X;old@correo.com;Clave1!;6;1234567890123456"));
        Park reloaded = TestPark.configure(dir);
        assertThrows(AuthenticationException.class, () -> reloaded.getUserController().delete("old@correo.com", "Clave1!"));
    }
}
