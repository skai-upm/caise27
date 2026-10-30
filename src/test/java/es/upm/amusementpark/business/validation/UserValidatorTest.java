package es.upm.amusementpark.business.validation;

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
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678z", ""})
    void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void nullDniIsInvalid() {
        assertFalse(UserValidator.isValidDni(null));
    }

    @Test
    void validEmail() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@correo.comm",
        "ana@@correo.com", "ana@correo."})
    void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void validPassword() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", ""})
    void invalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void validCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345a",
        "123456789012345-"})
    void invalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    void blankValues() {
        assertFalse(UserValidator.isNotBlank(" "));
        assertFalse(UserValidator.isNotBlank(null));
        assertTrue(UserValidator.isNotBlank("Ana"));
    }
}
