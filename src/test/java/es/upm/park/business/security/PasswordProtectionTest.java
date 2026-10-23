package es.upm.park.business.security;

import es.upm.park.business.controller.Park;
import es.upm.park.business.controller.ParkFixture;
import es.upm.park.business.controller.UserController;
import es.upm.park.business.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordProtectionTest {

    @TempDir
    Path dir;

    private UserController users;

    @BeforeEach
    void setUp() throws Exception {
        ParkFixture.openEmpty(dir);
        users = UserController.getInstance();
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void bcryptHasherRoundTrip() {
        PasswordHasher hasher = new BCryptPasswordHasher(4);
        String hash = hasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$04$"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave1?", hash));
        assertNotEquals(hash, hasher.hash("Clave1!"), "each hash uses a new salt");
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", "$2a$broken"));
        assertFalse(hasher.matches(null, hash));
    }

    @Test
    void fileStoresOnlyTheHash() throws Exception {
        String content = Files.readString(dir.resolve("users.txt"));
        assertFalse(content.contains("Clave1!"));
        assertTrue(content.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"));
        assertTrue(content.trim().endsWith(";612345678;1234567890123456"));
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThrows(AuthenticationException.class, () -> users.delete("ana@correo.com", "Clave1?"));
        assertNotNull(Park.getInstance().findUserByEmail("ana@correo.com"));
    }

    @Test
    void passwordChangeStoresNewHashThatWorksAfterRestart() throws Exception {
        String before = Park.getInstance().findUserByEmail("ana@correo.com").getPasswordHash();
        users.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        assertNotEquals(before, Park.getInstance().findUserByEmail("ana@correo.com").getPasswordHash());
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));

        ParkFixture.restart(dir);
        assertNotNull(users.authenticate("ana@correo.com", "Nueva2#"));
        assertThrows(AuthenticationException.class, () -> users.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void legacyPlainTextPasswordNeverAuthenticates() throws Exception {
        Files.write(dir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        ParkFixture.restart(dir);
        assertThrows(AuthenticationException.class, () -> users.authenticate("ana@correo.com", "Clave1!"));
    }
}
