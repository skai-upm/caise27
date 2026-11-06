package es.upm.park.business.validation;

import es.upm.park.business.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserValidatorTest {

    @Test
    void dni() {
        assertTrue(UserValidator.isValidDni("12345678Z"));
        assertTrue(UserValidator.isValidDni("87654321X"));
        assertTrue(UserValidator.isValidDni("00000000T"));
        for (String dni : new String[] {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "12345678",
                "12345678z", "", null}) {
            assertFalse(UserValidator.isValidDni(dni), String.valueOf(dni));
        }
    }

    @Test
    void email() {
        assertTrue(UserValidator.isValidEmail("ana@correo.com"));
        assertTrue(UserValidator.isValidEmail("ana.nueva@correo.com"));
        for (String email : new String[] {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
                "ana@@correo.com", "ana@correo.comm", "an a@correo.com", "ana@correo.", "", null}) {
            assertFalse(UserValidator.isValidEmail(email), String.valueOf(email));
        }
    }

    @Test
    void password() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
        assertTrue(UserValidator.isValidPassword("Otra2#"));
        for (String password : new String[] {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "Clave 1", "", null}) {
            assertFalse(UserValidator.isValidPassword(password), String.valueOf(password));
        }
    }

    @Test
    void card() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
        for (String card : new String[] {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
                "123456789012345a", "1234-56789012345", null}) {
            assertFalse(UserValidator.isValidCard(card), String.valueOf(card));
        }
    }

    @Test
    void wholeUserValidationNamesTheField() {
        assertDoesNotThrow(() -> UserValidator.validateUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456"));
        InvalidInputException dni = assertThrows(InvalidInputException.class, () -> UserValidator.validateUser(
                "Ana", "Garcia Lopez", "12345678A", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(dni.getMessage().contains("DNI"));
        InvalidInputException card = assertThrows(InvalidInputException.class, () -> UserValidator.validateUser(
                "Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234"));
        assertTrue(card.getMessage().contains("card"));
        assertThrows(InvalidInputException.class, () -> UserValidator.validateUser(
                " ", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertDoesNotThrow(() -> UserValidator.validateUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "any phone format", "1234567890123456"), "phone is stored as given");
    }

    @Test
    void passwordPolicyRejectsWeakPasswords() {
        assertThrows(InvalidInputException.class, () -> UserValidator.passwordPolicy().validate("clave1!"));
        assertDoesNotThrow(() -> UserValidator.passwordPolicy().validate("Clave1!"));
    }
}
