package es.upm.parque.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @Test
    void dniWithCorrectLetterIsValid() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("00000000T"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "12345678", "1234567AZ", "12345678z", "1234 678Z"})
    void invalidDnisAreRejected(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void wellFormedEmailIsValid() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@@correo.com", "a@b@c.com", "ana@correo.comm"})
    void malformedEmailsAreRejected(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void passwordWithAllCategoriesIsValid() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave1 "})
    void passwordsMissingACategoryAreRejected(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void sixteenDigitCardIsValid() {
        assertTrue(UserValidator.isValidCardNumber("1234567890123456"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A", "1234-56789012345"})
    void invalidCardNumbersAreRejected(String card) {
        assertFalse(UserValidator.isValidCardNumber(card));
    }

    private User validUser(String card, String telephone) {
        return new User("Ana", "García", "12345678Z", "ana@correo.com", "Clave1!", telephone, card);
    }

    @Test
    void fullyValidUserIsValidRegardlessOfTelephone() {
        User u = validUser("1234567890123456", "+34 600 12 34 56");
        assertTrue(UserValidator.isValid(u));
        assertEquals("+34 600 12 34 56", u.getTelephone());
    }

    @Test
    void oneInvalidFieldMakesUserInvalidAndLeavesItUnchanged() {
        User u = validUser("123456789012345", "600123456");
        assertFalse(UserValidator.isValid(u));
        assertEquals("123456789012345", u.getCardNumber());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
    }

    @Test
    void nullUserIsInvalid() {
        assertFalse(UserValidator.isValid(null));
    }
}
