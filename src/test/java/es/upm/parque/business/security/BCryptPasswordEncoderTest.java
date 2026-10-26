package es.upm.parque.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordEncoderTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Test
    void hashesAndVerifies() {
        String hash = encoder.hash("Clave1!");
        assertNotEquals("Clave1!", hash);
        assertTrue(encoder.isHash(hash));
        assertTrue(encoder.matches("Clave1!", hash));
        assertFalse(encoder.matches("Clave2!", hash));
        assertNotEquals(hash, encoder.hash("Clave1!"));
    }

    @Test
    void plaintextIsNotAHash() {
        assertFalse(encoder.isHash("Clave1!"));
        assertFalse(encoder.isHash(null));
        assertFalse(encoder.matches("Clave1!", "Clave1!"));
    }
}
