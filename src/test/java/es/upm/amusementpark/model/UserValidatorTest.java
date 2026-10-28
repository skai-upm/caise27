package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @Test
    void dniWithMatchingLetterIsValid() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("00000000T"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "12345678", "1234567AZ", "12345678z", ""})
    void malformedOrWrongLetterDniIsInvalid(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void emailInExpectedFormatIsValid() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "ana@", "a@b@c.com", ""})
    void malformedEmailIsInvalid(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void passwordWithAllCharacterClassesIsValid() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave1 ", ""})
    void passwordMissingAClassIsInvalid(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void sixteenDigitCardIsValid() {
        assertTrue(UserValidator.isValidCardNumber("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A"})
    void malformedCardIsInvalid(String card) {
        assertFalse(UserValidator.isValidCardNumber(card));
    }

    @Test
    void nullValuesAreInvalid() {
        assertFalse(UserValidator.isValidDni(null));
        assertFalse(UserValidator.isValidEmail(null));
        assertFalse(UserValidator.isValidPassword(null));
        assertFalse(UserValidator.isValidCardNumber(null));
        assertFalse(UserValidator.isValid(null));
    }

    private static User user(String dni, String email, String password, String phone, String card) {
        return new User("Ana", "García", dni, email, password, phone, card);
    }

    @Test
    void userWithValidDataIsValidWhateverTheTelephone() {
        assertTrue(UserValidator.isValid(user("12345678Z", "ana@correo.com", "Clave1!", "600123456", "1234567890123456")));
        assertTrue(UserValidator.isValid(user("12345678Z", "ana@correo.com", "Clave1!", "not a phone", "1234567890123456")));
    }

    @Test
    void userWithOneInvalidFieldIsInvalid() {
        assertFalse(UserValidator.isValid(user("12345678A", "ana@correo.com", "Clave1!", "600", "1234567890123456")));
        assertFalse(UserValidator.isValid(user("12345678Z", "ana@correo", "Clave1!", "600", "1234567890123456")));
        assertFalse(UserValidator.isValid(user("12345678Z", "ana@correo.com", "clave1!", "600", "1234567890123456")));
        assertFalse(UserValidator.isValid(user("12345678Z", "ana@correo.com", "Clave1!", "600", "123456789012345")));
    }
}
