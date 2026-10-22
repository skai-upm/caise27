package es.upm.etsisi.park.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "12345678z", "87654321X", "00000000T"})
    void acceptsValidDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678", ""})
    void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void rejectsNullDni() {
        assertFalse(UserValidator.isValidDni(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@mail.example.org"})
    void acceptsValidEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
            "ana@@correo.com", "ana@b@correo.com", "an a@correo.com", "ana@correo.c0m", "ana@correo."})
    void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Nueva2#", "Otra2#"})
    void acceptsValidPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1"})
    void rejectsInvalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void acceptsValidCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345a"})
    void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    void detectsBlankText() {
        assertTrue(UserValidator.isNotBlank("Ana"));
        assertFalse(UserValidator.isNotBlank(""));
        assertFalse(UserValidator.isNotBlank("   "));
        assertFalse(UserValidator.isNotBlank(null));
    }
}
