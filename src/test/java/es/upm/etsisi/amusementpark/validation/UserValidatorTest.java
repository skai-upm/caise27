package es.upm.etsisi.amusementpark.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    @Test
    void validDni() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("87654321X"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "12345678", "1234567AZ", "12345678z", ""})
    void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@correo.com"})
    void validEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "a@b@correo.com", "ana@correo.comm"})
    void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Otra2#", "Nueva2#"})
    void validPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1"})
    void invalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void validCard() {
        assertTrue(UserValidator.isValidCardNumber("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A", "123456789012345-"})
    void invalidCard(String card) {
        assertFalse(UserValidator.isValidCardNumber(card));
    }

    @Test
    void nullsAreRejected() {
        assertFalse(UserValidator.isValidDni(null));
        assertFalse(UserValidator.isValidEmail(null));
        assertFalse(UserValidator.isValidPassword(null));
        assertFalse(UserValidator.isValidCardNumber(null));
    }
}
