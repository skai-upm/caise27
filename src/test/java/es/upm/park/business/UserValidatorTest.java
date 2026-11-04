package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T"})
    void validDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678", "12345678z", ""})
    void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void nullValuesAreInvalid() {
        assertFalse(UserValidator.isValidDni(null));
        assertFalse(UserValidator.isValidEmail(null));
        assertFalse(UserValidator.isValidPassword(null));
        assertFalse(UserValidator.isValidCard(null));
        assertFalse(UserValidator.isNonBlank(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@sub.correo.org"})
    void validEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
        "ana@@correo.com", "ana@correo.comm", "ana@correo."})
    void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Nueva2#", "Otra2#"})
    void validPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1", ""})
    void invalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void validCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
        "123456789012345a", "123456789012345-"})
    void invalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    void blankText() {
        assertTrue(UserValidator.isNonBlank("Ana"));
        assertFalse(UserValidator.isNonBlank(""));
        assertFalse(UserValidator.isNonBlank("   "));
    }
}
