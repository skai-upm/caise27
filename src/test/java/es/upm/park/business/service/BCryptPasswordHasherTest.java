package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesAreSaltedAndVerifiable() {
        String h1 = hasher.hash("Clave1!");
        String h2 = hasher.hash("Clave1!");
        assertTrue(h1.startsWith("$2a$04$") && h1.length() == 60);
        assertNotEquals(h1, h2, "random salt");
        assertTrue(hasher.matches("Clave1!", h1) && hasher.matches("Clave1!", h2));
        assertFalse(hasher.matches("Clave1?", h1));
    }

    @Test
    void nonHashValuesNeverMatch() {
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", ""));
        assertFalse(hasher.matches("Clave1!", "$2a$broken"));
        assertFalse(hasher.matches(null, hasher.hash("x")));
        assertFalse(hasher.matches("x", null));
    }

    @Test
    void defaultCostIsTen() {
        assertTrue(new BCryptPasswordHasher().hash("Clave1!").startsWith("$2a$10$"));
    }
}
