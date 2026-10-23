package es.upm.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.validation.UserValidator;
import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashIsSaltedBcryptAndNeverThePassword() {
        String first = hasher.hash("Clave1!");
        String second = hasher.hash("Clave1!");
        assertTrue(first.startsWith("$2a$04$"));
        assertEquals(60, first.length());
        assertFalse(first.contains("Clave1!"));
        assertNotEquals(first, second, "every hash uses a new salt");
        assertTrue(UserValidator.isPasswordHash(first));
    }

    @Test
    void verifiesAgainstTheStoredHash() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("clave1!", hash));
        assertFalse(hasher.matches("Clave1!", ""));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches(null, hash));
    }

    @Test
    void defaultWorkFactorIsTen() {
        assertTrue(new BCryptPasswordHasher().hash("Clave1!").startsWith("$2a$10$"));
    }

    @Test
    void recognizesHashShape() {
        assertFalse(UserValidator.isPasswordHash("Clave1!"));
        assertFalse(UserValidator.isPasswordHash(null));
        assertFalse(UserValidator.isPasswordHash("$2a$04$short"));
    }
}
