package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class UserValidatorTest {

    @Test
    void acceptsValidDni() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("87654321X"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678", "12345678z", "", "ABCDEFGHZ"})
    void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void acceptsValidEmail() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@@correo.com", "a@b@c.com", "ana@correo.comm"})
    void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void acceptsValidPassword() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
        assertTrue(UserValidator.isValidPassword("Otra2#"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave1 "})
    void rejectsInvalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void acceptsValidCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345a", "-234567890123456"})
    void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    void validatesWholeUserIgnoringPhone() {
        assertTrue(UserValidator.isValid(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "any phone text", "1234567890123456")));
        assertFalse(UserValidator.isValid(new User("Ana", "Garcia Lopez", "12345678A", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456")));
        assertFalse(UserValidator.isValid(null));
    }
}
