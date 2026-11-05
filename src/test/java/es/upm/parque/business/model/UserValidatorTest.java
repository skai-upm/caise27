package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mindrot.jbcrypt.BCrypt;

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

    private static final String PROTECTED = BCrypt.hashpw("Clave1!", BCrypt.gensalt(4));

    private User validUser(String card, String telephone) {
        return new User("Ana", "García", "12345678Z", "ana@correo.com", PROTECTED, telephone, card);
    }

    @Test
    void storedPlainTextPasswordMakesTheUserInvalid() {
        User u = new User("Ana", "García", "12345678Z", "ana@correo.com", "Clave1!", "600", "1234567890123456");
        assertFalse(UserValidator.isValid(u));
    }

    @Test
    void protectedPasswordFormatIsCheckedWithoutRegularExpressions() {
        assertTrue(UserValidator.isProtectedPassword(PROTECTED));
        assertTrue(UserValidator.isProtectedPassword("$2b$12$" + "a".repeat(53)));
        assertTrue(UserValidator.isProtectedPassword("$2y$10$./" + "Z9".repeat(25) + "x"));
        assertFalse(UserValidator.isProtectedPassword(null));
        assertFalse(UserValidator.isProtectedPassword("Clave1!"));
        assertFalse(UserValidator.isProtectedPassword("$2c$10$" + "a".repeat(53)));
        assertFalse(UserValidator.isProtectedPassword("$2a$1x$" + "a".repeat(53)));
        assertFalse(UserValidator.isProtectedPassword("$2a$10$" + "a".repeat(52)));
        assertFalse(UserValidator.isProtectedPassword("$2a$10$" + "a".repeat(52) + "!"));
        assertFalse(UserValidator.isProtectedPassword("x2a$10$" + "a".repeat(53)));
        assertFalse(UserValidator.isProtectedPassword("$2a#10$" + "a".repeat(53)));
        assertFalse(UserValidator.isProtectedPassword("$2a$10#" + "a".repeat(53)));
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
        assertEquals(PROTECTED, u.getPassword());
    }

    @Test
    void nullUserIsInvalid() {
        assertFalse(UserValidator.isValid(null));
    }
}
