package es.upm.etsisi.amusementpark.business.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordHasherTest {

    final PasswordHasher hasher = new BCryptPasswordHasher(4);

    @Test
    void hashesWithFreshSaltAndVerifies() {
        String first = hasher.hash("Clave1!");
        String second = hasher.hash("Clave1!");
        assertNotEquals(first, second);
        assertEquals(60, first.length());
        assertTrue(first.startsWith("$2a$04$"));
        assertTrue(hasher.matches("Clave1!", first));
        assertTrue(hasher.matches("Clave1!", second));
        assertFalse(hasher.matches("Clave1?", first));
        assertFalse(hasher.matches(null, first));
    }

    @Test
    void defaultWorkFactorIsTheLibraryDefault() {
        assertTrue(new BCryptPasswordHasher().hash("Clave1!").startsWith("$2a$10$"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Clave1!", "$2a$04$tooShort", "$1$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
    void nonHashStoredValuesNeverMatch(String stored) {
        assertFalse(hasher.matches("Clave1!", stored));
    }
}
