package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @Test
    void validExamples() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("87654321X"));
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
        assertTrue(UserValidator.isValidPassword("Clave1!"));
        assertTrue(UserValidator.isValidPassword("Otra2#"));
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678z", ""})
    void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@@correo.com",
        "an a@correo.com", "ana@correo.comm"})
    void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", ""})
    void invalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345a"})
    void invalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    void combinedValidationNamesFirstViolation() {
        UserValidator.validate("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        ParkException e = assertThrows(ParkException.class,
                () -> UserValidator.validate("12345678Z", "ana@correo.com", "Clave1!", "123"));
        assertTrue(e.getMessage().startsWith("Invalid card number"));
        assertThrows(ParkException.class,
                () -> UserValidator.validate("12345678A", "bad", "x", "123"));
    }

    @Test
    void nullsRejected() {
        assertFalse(UserValidator.isValidDni(null));
        assertFalse(UserValidator.isValidEmail(null));
        assertFalse(UserValidator.isValidPassword(null));
        assertFalse(UserValidator.isValidCard(null));
    }
}
