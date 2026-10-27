package es.upm.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesAndVerifies() {
        String stored = hasher.hash("Clave1!");
        assertTrue(stored.startsWith("$2a$"));
        assertFalse(stored.contains("Clave1!"));
        assertTrue(hasher.verify("Clave1!", stored));
        assertFalse(hasher.verify("Clave2!", stored));
    }

    @Test
    void eachHashIsNew() {
        assertNotEquals(hasher.hash("Clave1!"), hasher.hash("Clave1!"));
    }

    @Test
    void malformedStoredValueNeverMatches() {
        assertFalse(hasher.verify("Clave1!", "Clave1!"));
        assertFalse(hasher.verify("Clave1!", null));
    }
}
