package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordHasherTest {

    private final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashIsNotThePlaintext() {
        String hash = hasher.hash("Clave1!");
        assertNotEquals("Clave1!", hash);
        assertTrue(hash.startsWith("$2a$"));
        assertFalse(hash.contains(";"), "safe for the semicolon file format");
    }

    @Test
    void eachHashUsesANewSalt() {
        assertNotEquals(hasher.hash("Clave1!"), hasher.hash("Clave1!"));
    }

    @Test
    void matchesOnlyTheRightPassword() {
        String hash = hasher.hash("Clave1!");
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Mala1!", hash));
        assertFalse(hasher.matches(null, hash));
    }

    @Test
    void nonBcryptStoredValueNeverMatches() {
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", ""));
        assertFalse(hasher.matches("Clave1!", null));
        assertFalse(hasher.matches("Clave1!", "$2a$10$tooShort"));
        assertFalse(hasher.matches("Clave1!", "$2a$10$" + "x".repeat(53)));
    }

    @Test
    void defaultCostHashesVerify() {
        PasswordHasher production = new BCryptPasswordHasher();
        String hash = production.hash("Nueva2#");
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(production.matches("Nueva2#", hash));
    }
}
