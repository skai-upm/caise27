package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T", "00000023T"})
    void acceptsValidDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "123456789", "12345678z", "", "-1234567Z"})
    void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@sub.correo.org"})
    void acceptsValidEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
            "ana@@correo.com", "ana@correo.comm", "an a@correo.com", "ana@correo.", ""})
    void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Nueva2#", "Otra2#", "aB3-"})
    void acceptsValidPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1", ""})
    void rejectsInvalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void acceptsValidCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
            "123456789012345a", "123456789012345-", ""})
    void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }
}
