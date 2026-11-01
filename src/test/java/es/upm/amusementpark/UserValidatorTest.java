package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserValidatorTest {

    @Test
    public void validDni() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("87654321X"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678z", ""})
    public void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @Test
    public void validEmail() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
        "ana@correo.comm", "ana@@correo.com", "a na@correo.com", ""})
    public void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    public void validPassword() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", ""})
    public void invalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    public void validCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
        "123456789012345A", "123456789012345-"})
    public void invalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }
}
