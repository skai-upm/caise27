package es.upm.etsisi.park.business.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesAndVerifies() {
        String hash = hasher.hash("Clave1!");

        assertNotEquals("Clave1!", hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        assertNotEquals(hasher.hash("Clave1!"), hasher.hash("Clave1!"));
    }

    @Test
    void invalidStoredValuesNeverMatch() {
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", ""));
        assertFalse(hasher.matches("Clave1!", null));
        assertFalse(hasher.matches(null, hasher.hash("Clave1!")));
    }
}
