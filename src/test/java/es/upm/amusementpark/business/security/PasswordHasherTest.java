package es.upm.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void hashIsBCryptAndVerifies() {
        String hash = PasswordHasher.hash("Clave1!");
        assertTrue(hash.startsWith("$2a$"));
        assertFalse(hash.contains("Clave1!"));
        assertTrue(PasswordHasher.matches("Clave1!", hash));
        assertFalse(PasswordHasher.matches("Mala1!", hash));
        assertNotEquals(hash, PasswordHasher.hash("Clave1!"));
    }

    @Test
    void nonHashValuesNeverMatch() {
        assertFalse(PasswordHasher.matches("Clave1!", "Clave1!"));
        assertFalse(PasswordHasher.matches("Clave1!", null));
    }
}
