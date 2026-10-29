package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.exception.AuthenticationException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;

/**
 * BCrypt password storage and verification (Delivery 3, F3-C05).
 */
class PasswordProtectionTest extends ParkTestSupport {

    @Test
    void storesOnlyBcryptHash() throws Exception {
        User ana = createAna();
        String file = Files.readString(dir.resolve("users.txt"));
        assertTrue(file.startsWith("Ana;Garcia Lopez;12345678Z;ana@correo.com;$2a$"));
        assertFalse(file.contains("Clave1!"));
        assertNotEquals("Clave1!", ana.getPasswordHash());
        assertTrue(TestParks.FAST_HASHER.matches("Clave1!", ana.getPasswordHash()));
    }

    @Test
    void invalidPasswordRejectedBeforeHashing() {
        assertThrows(ValidationException.class, () -> park.createUser("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "6", "1234567890123456"));
        assertNull(park.findUser("ana@correo.com"));
    }

    @Test
    void verifiesAgainstStoredHashAfterRestart() throws Exception {
        createAna();
        restart();
        park.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        assertThrows(AuthenticationException.class, () -> park.deleteUser("ana@correo.com", "Mala1!"));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertNull(park.findUser("ana@correo.com"));
    }

    @Test
    void passwordChangeStoresNewHash() throws Exception {
        String oldHash = createAna().getPasswordHash();
        park.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        restart();
        String newHash = park.findUser("ana@correo.com").getPasswordHash();
        assertNotEquals(oldHash, newHash);
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Nueva2#"));
        assertThrows(AuthenticationException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "X"));
        park.updateUser("ana@correo.com", "Nueva2#", UserField.NAME, "Ana Maria");
    }

    @Test
    void legacyPlainTextPasswordsDoNotVerify() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", ""));
        assertFalse(hasher.matches("Clave1!", null));
        assertTrue(hasher.matches("Clave1!", hasher.hash("Clave1!")));
    }
}
