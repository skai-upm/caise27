package es.upm.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesAreBCryptAndNeverThePlaintext() {
        String hash = hasher.hash("Clave1!");
        assertTrue(BCryptPasswordHasher.isHash(hash));
        assertFalse(hash.contains("Clave1!"));
        assertNotEquals(hash, hasher.hash("Clave1!"));
    }

    @Test
    void verifiesAgainstTheHash() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
        assertFalse(hasher.matches(null, hash));
    }

    @Test
    void legacyPlaintextIsComparedDirectly() {
        assertTrue(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Otra2#", "Clave1!"));
    }

    @Test
    void malformedHashDoesNotMatch() {
        assertFalse(hasher.matches("Clave1!", "$2a$broken"));
    }
}
