package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordProtectionTest {

    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
    }

    @Test
    void storedValueIsBcryptNotPlainText() {
        String stored = park.protectPassword("Clave1!");
        assertTrue(stored.startsWith("$2a$"));
        assertFalse(stored.contains("Clave1!"));
    }

    @Test
    void samePasswordGivesDifferentValuesThatBothVerify() {
        String first = park.protectPassword("Clave1!");
        String second = park.protectPassword("Clave1!");
        assertNotEquals(first, second);
        assertTrue(park.verifyPassword("Clave1!", first));
        assertTrue(park.verifyPassword("Clave1!", second));
    }

    @Test
    void wrongPasswordRejected() {
        String stored = park.protectPassword("Clave1!");
        assertFalse(park.verifyPassword("clave1!", stored));
        assertFalse(park.verifyPassword(null, stored));
    }

    @Test
    void nonBcryptStoredValueNeverVerifies() {
        assertFalse(park.verifyPassword("Clave1!", "Clave1!"));
        assertFalse(park.verifyPassword("Clave1!", "$2a$xx"));
        assertFalse(park.verifyPassword("Clave1!", null));
    }

    @Test
    void passwordChangeRegeneratesValue() {
        String old = park.protectPassword("Clave1!");
        String updated = park.protectPassword("Nueva2#");
        assertTrue(park.verifyPassword("Nueva2#", updated));
        assertFalse(park.verifyPassword("Clave1!", updated));
        assertNotEquals(old, updated);
    }

    @Test
    void hasherIsInjectable() {
        park.setPasswordHasher(new PasswordHasher() {
            @Override
            public String protect(String plainPassword) {
                return "x" + plainPassword.length();
            }

            @Override
            public boolean verify(String plainPassword, String storedValue) {
                return storedValue.equals(protect(plainPassword));
            }
        });
        assertTrue(park.verifyPassword("abc", park.protectPassword("abc")));
        park.reset();
        assertTrue(park.protectPassword("abc").startsWith("$2a$"));
    }
}
