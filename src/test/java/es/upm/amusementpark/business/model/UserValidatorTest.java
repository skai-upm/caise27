package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @Test
    void validDni() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("00000000T"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678", "", "12345678ZZ"})
    void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void validEmail() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "a@b@c.com", "ana@correo.comm"})
    void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void validPassword() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave1 "})
    void invalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void validCard() {
        assertTrue(UserValidator.isValidCardNumber("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A", "1234-56789012345"})
    void invalidCard(String card) {
        assertFalse(UserValidator.isValidCardNumber(card));
    }

    @Test
    void nullsRejected() {
        assertFalse(UserValidator.isValidDni(null));
        assertFalse(UserValidator.isValidEmail(null));
        assertFalse(UserValidator.isValidPassword(null));
        assertFalse(UserValidator.isValidCardNumber(null));
        assertFalse(UserValidator.isValid(null));
    }

    @Test
    void wholeUser() {
        User ok = new User("Ana", "García", "12345678Z", "ana@correo.com", "Clave1!", "600 123 456", "1234567890123456");
        assertTrue(UserValidator.isValid(ok));
        User badDni = new User("Ana", "García", "12345678A", "ana@correo.com", "Clave1!", "600 123 456", "1234567890123456");
        assertFalse(UserValidator.isValid(badDni));
    }
}
