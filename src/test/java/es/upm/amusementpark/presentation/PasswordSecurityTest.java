package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.security.BCryptPasswordHasher;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

class PasswordSecurityTest extends ParkTestSupport {

    private String storedUsers() throws IOException {
        return Files.readString(dir.resolve("users.txt"));
    }

    private String storedPassword() throws IOException {
        return storedUsers().strip().split(";")[4];
    }

    @Test
    void createdUserHasNoPlaintextPassword() throws IOException {
        assertTrue(ok(run(ANA)));
        assertFalse(storedUsers().contains("Clave1!"));
        assertTrue(BCryptPasswordHasher.isHash(storedPassword()));
    }

    @Test
    void deleteAndUpdateVerifyThePassword() {
        run(ANA);
        assertTrue(error(run("USER UPDATE;ana@correo.com;Wrong1!;PHONE;699999999")));
        assertTrue(ok(run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999")));
        assertTrue(error(run("USER DELETE;ana@correo.com;Wrong1!")));
        assertTrue(ok(run("USER DELETE;ana@correo.com;Clave1!")));
    }

    @Test
    void passwordChangeStoresNewHashUsableAfterRestart() throws IOException {
        run(ANA);
        String before = storedPassword();
        assertTrue(ok(run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#")));
        String after = storedPassword();
        assertNotEquals(before, after);
        assertTrue(BCryptPasswordHasher.isHash(after));
        assertFalse(storedUsers().contains("Nueva2#"));

        restart();
        assertTrue(error(run("USER UPDATE;ana@correo.com;Clave1!;NAME;X")));
        assertTrue(ok(run("USER UPDATE;ana@correo.com;Nueva2#;NAME;Ana Maria")));
    }

    @Test
    void formatRulesApplyBeforeHashing() {
        assertTrue(error(run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;clave1!;612345678;1234567890123456")));
        run(ANA);
        assertTrue(error(run("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!")));
        assertTrue(ok(run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria")));
    }

    @Test
    void legacyPlaintextUserCanStillAuthenticate() throws IOException {
        Files.writeString(dir.resolve("users.txt"),
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        restart();
        assertNotNull(park.getUsers().findByEmail("ana@correo.com"));
        assertTrue(error(run("USER UPDATE;ana@correo.com;Wrong1!;NAME;X")));
        assertTrue(ok(run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria")));
        assertEquals("Ana Maria", park.getUsers().findByEmail("ana@correo.com").getName());
    }
}
