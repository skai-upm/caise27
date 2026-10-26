package es.upm.parque.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher(4);

    @Test
    void hashIsNotPlaintextAndVerifies() {
        String hash = hasher.hash("Clave1!");
        assertNotEquals("Clave1!", hash);
        assertTrue(hasher.isHash(hash));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        assertNotEquals(hasher.hash("Clave1!"), hasher.hash("Clave1!"));
    }

    @Test
    void plaintextIsNotAHash() {
        assertFalse(hasher.isHash("Clave1!"));
        assertFalse(hasher.isHash(null));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
    }
}
