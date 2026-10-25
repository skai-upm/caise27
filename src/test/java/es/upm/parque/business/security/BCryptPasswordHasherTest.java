package es.upm.parque.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesAndVerifiesPasswords() {
        String hash = hasher.hash("Clave1!");
        assertNotEquals("Clave1!", hash);
        assertTrue(hasher.isHash(hash));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Otra1!", hash));
        assertNotEquals(hash, hasher.hash("Clave1!"));
    }

    @Test
    void recognisesOnlyBCryptHashes() {
        assertFalse(hasher.isHash("Clave1!"));
        assertFalse(hasher.isHash(null));
        assertFalse(hasher.isHash("$2a$" + "x".repeat(10)));
        assertFalse(hasher.isHash("$1a$" + "x".repeat(56)));
        assertTrue(hasher.isHash("$2b$" + "x".repeat(56)));
        assertTrue(hasher.isHash("$2y$" + "x".repeat(56)));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
    }
}
