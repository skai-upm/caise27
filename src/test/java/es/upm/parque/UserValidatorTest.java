package es.upm.parque;

import es.upm.parque.business.validation.UserValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "12345678z", "00000000T"})
    public void acceptsValidDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789", "1234567AZ", "", "12345678ZZ"})
    public void rejectsInvalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@sub.correo.org"})
    public void acceptsValidEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
        "ana@@correo.com", "ana@correo.comm", "an a@correo.com", ""})
    public void rejectsInvalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Nueva2#", "Otra2#"})
    public void acceptsValidPassword(String password) {
        assertTrue(UserValidator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", ""})
    public void rejectsInvalidPassword(String password) {
        assertFalse(UserValidator.isValidPassword(password));
    }

    @Test
    public void acceptsValidCard() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
        "123456789012345a", "123456789012345-"})
    public void rejectsInvalidCard(String card) {
        assertFalse(UserValidator.isValidCard(card));
    }

    @Test
    public void blankTextIsRejected() {
        assertTrue(UserValidator.isNotBlank("Ana"));
        assertFalse(UserValidator.isNotBlank(""));
        assertFalse(UserValidator.isNotBlank("   "));
        assertFalse(UserValidator.isNotBlank(null));
    }
}
