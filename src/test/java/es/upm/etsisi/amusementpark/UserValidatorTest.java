package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T"})
    void validDnis(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "12345678", "1234567AZ", "12345678z", "ABCDEFGHZ"})
    void invalidDnis(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "a@b.c.com"})
    void validEmails(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@@correo.com", "ana@correo.com@x.com", "ana@correo."})
    void invalidEmails(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void validPassword() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1"})
    void invalidPasswords(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void validCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A", "123456789012345-"})
    void invalidCards(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    void blankChecks() {
        assertTrue(UserValidator.isNotBlank("Ana"));
        assertFalse(UserValidator.isNotBlank("  "));
        assertFalse(UserValidator.isNotBlank(null));
    }
}
