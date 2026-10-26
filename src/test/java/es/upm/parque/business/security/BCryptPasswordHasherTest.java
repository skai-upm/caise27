package es.upm.parque.business.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashIsNotThePlainPasswordAndVerifies() {
        String hash = hasher.hash("Clave1!");
        assertNotEquals("Clave1!", hash);
        assertTrue(hash.startsWith("$2a$04$"));
        assertTrue(hasher.isHash(hash));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        assertNotEquals(hasher.hash("Clave1!"), hasher.hash("Clave1!"));
    }

    @Test
    void plainTextIsNotAHash() {
        assertFalse(hasher.isHash("Clave1!"));
        assertFalse(hasher.isHash(null));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
    }

    @Test
    void defaultWorkFactorIsTen() {
        assertTrue(new BCryptPasswordHasher().hash("Clave1!").startsWith("$2a$10$"));
    }
}
