package es.upm.parque.business.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesAreSaltedBcryptValues() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$04$"));
        assertEquals(60, hash.length());
        assertNotEquals(hash, hasher.hash("Clave1!"));
        assertTrue(hasher.isHash(hash));
    }

    @Test
    void matchesOnlyTheOriginalPassword() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave1?", hash));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
    }

    @Test
    void recognisesHashes() {
        assertFalse(hasher.isHash("Clave1!"));
        assertFalse(hasher.isHash(null));
        assertFalse(hasher.isHash("$2a$" + "x".repeat(10)));
        assertTrue(hasher.isHash("$2b$" + "x".repeat(56)));
        assertTrue(hasher.isHash("$2y$" + "x".repeat(56)));
    }
}
