package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    private static User validUser() {
        return new User("Ana", "García López", "12345678Z", "ana@correo.com",
                "Clave1!", "+34 600-000-000", "1234567890123456");
    }

    @Test
    void acceptsDniWithCorrectLetter() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("00000000T"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678", "", "12345678z"})
    void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    void acceptsWellFormedEmail() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "a@b@c.com", "ana@", ""})
    void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void acceptsPasswordWithAllCategories() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave1 ", ""})
    void rejectsPasswordMissingCategory(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    void acceptsSixteenDigitCard() {
        assertTrue(UserValidator.isValidCardNumber("1234567890123456"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A", "123456789012345!"})
    void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCardNumber(card));
    }

    @Test
    void fullyValidUserIsValid() {
        User user = validUser();
        assertTrue(UserValidator.isValid(user));
        assertTrue(user.isValid());
    }

    @Test
    void invalidDniMakesUserInvalidWithoutChangingIt() {
        User user = new User("Ana", "García López", "12345678A", "ana@correo.com",
                "Clave1!", "600000000", "1234567890123456");
        assertFalse(user.isValid());
        assertEquals("Ana", user.getFirstName());
        assertEquals("García López", user.getSurnames());
        assertEquals("12345678A", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("600000000", user.getTelephone());
        assertEquals("1234567890123456", user.getCardNumber());
    }

    @Test
    void missingFieldMakesUserInvalid() {
        User user = validUser();
        user.setEmail(null);
        assertFalse(user.isValid());
        assertFalse(UserValidator.isValid(null));
    }

    @Test
    void telephoneIsNotValidatedAndKeptUnchanged() {
        User user = validUser();
        user.setTelephone("not a phone #");
        assertTrue(user.isValid());
        assertEquals("not a phone #", user.getTelephone());
    }
}
