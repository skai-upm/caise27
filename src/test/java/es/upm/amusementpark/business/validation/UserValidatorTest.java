package es.upm.amusementpark.business.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T", "12345678z"})
    void acceptsValidDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "123456789", "12345678",
        "12345678ZZ", " 2345678Z"})
    void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@mail.correo.org"})
    void acceptsValidEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@@correo.com",
        "ana@b@correo.com", "ana@correo..com", "an a@correo.com", "ana@correo.comm", "ana@correo.com."})
    void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Otra2#", "Nueva2#", "aB3$"})
    void acceptsValidPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1"})
    void rejectsInvalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890123456", "9999888877776666"})
    void acceptsValidCard(String card) {
        assertTrue(UserValidator.isValidCard(card));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A",
        "1234-56789012345"})
    void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlank(String value) {
        assertFalse(UserValidator.isNonBlank(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ana", "Garcia Lopez"})
    void acceptsNonBlank(String value) {
        assertTrue(UserValidator.isNonBlank(value));
    }
}
