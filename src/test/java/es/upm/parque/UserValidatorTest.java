package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T"})
    void acceptsValidDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "12345678z", "1234567Z", "123456789Z", "1234567AZ", "", "ABCDEFGHZ"})
    void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@sub.correo.org"})
    void acceptsValidEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@@correo.com",
            "a@b@correo.com", "ana @correo.com", "ana@correo.c0m", "ana@correo.comm", ""})
    void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Otra2#", "Nueva2#"})
    void acceptsValidPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1", ""})
    void rejectsInvalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890123456", "9999888877776666"})
    void acceptsValidCard(String card) {
        assertTrue(UserValidator.isValidCard(card));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A",
            "123456789012345-", ""})
    void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }
}
