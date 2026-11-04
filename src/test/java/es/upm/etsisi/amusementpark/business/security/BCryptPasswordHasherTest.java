package es.upm.etsisi.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesWithRandomSaltAndVerifies() {
        String first = hasher.hash("Clave1!");
        String second = hasher.hash("Clave1!");
        assertTrue(first.startsWith("$2a$04$"));
        assertFalse(first.contains("Clave1!"));
        assertFalse(first.contains(";"));
        assertNotEquals(first, second);
        assertTrue(hasher.matches("Clave1!", first));
        assertTrue(hasher.matches("Clave1!", second));
        assertFalse(hasher.matches("Wrong1!", first));
    }

    @Test
    void valuesThatAreNotHashesNeverMatch() {
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", ""));
        assertFalse(hasher.matches("Clave1!", null));
        assertFalse(hasher.matches(null, hasher.hash("Clave1!")));
    }

    @Test
    void defaultCostIsUsedByTheApplication() {
        assertTrue(new BCryptPasswordHasher(BCryptPasswordHasher.DEFAULT_LOG_ROUNDS).hash("Clave1!").startsWith("$2a$10$"));
    }
}
