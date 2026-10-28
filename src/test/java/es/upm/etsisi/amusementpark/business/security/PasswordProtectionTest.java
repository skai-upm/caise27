package es.upm.etsisi.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordProtectionTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher();

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Otra2#", "aB3$"})
    void validPasswords(String password) {
        assertDoesNotThrow(() -> PasswordPolicy.validate(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1", ""})
    void weakPasswordsRejected(String password) {
        assertThrows(InvalidValueException.class, () -> PasswordPolicy.validate(password));
    }

    @Test
    void bcryptHashesAndVerifies() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$"));
        assertFalse(hash.contains("Clave1!"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave1?", hash));
        assertNotEquals(hash, hasher.hash("Clave1!"), "a new salt is used for every protected value");
        assertFalse(hasher.matches("Clave1!", "not-a-bcrypt-value"));
    }
}
