package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T", "11111111H"})
    void validDni(String dni) {
        assertTrue(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789", "1234567AZ", "12345678z", "", "12345678ZZ"})
    void invalidDni(String dni) {
        assertFalse(UserValidator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "pedro@correo.com", "ana.nueva@correo.com", "ana@mail.correo.com"})
    void validEmail(String email) {
        assertTrue(UserValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "a@b@c.com", ""})
    void invalidEmail(String email) {
        assertFalse(UserValidator.isValidEmail(email));
    }

    @Test
    void passwords() {
        assertTrue(UserValidator.isValidPassword("Clave1!"));
        for (String invalid : new String[] {"clave1!", "CLAVE1!", "Clave!!", "Clave12"}) {
            assertFalse(UserValidator.isValidPassword(invalid), invalid);
        }
    }

    @Test
    void cards() {
        assertTrue(UserValidator.isValidCard("1234567890123456"));
        for (String invalid : new String[] {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
                "123456789012345A", "123456789012345!"}) {
            assertFalse(UserValidator.isValidCard(invalid), invalid);
        }
    }

    @Test
    void wholeUserValidationNamesTheField() {
        assertDoesNotThrow(() -> UserValidator.validate("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456"));
        InvalidValueException card = assertThrows(InvalidValueException.class,
                () -> UserValidator.validate("12345678Z", "ana@correo.com", "Clave1!", "1234"));
        assertTrue(card.getMessage().contains("card"));
        assertTrue(assertThrows(InvalidValueException.class,
                () -> UserValidator.validate("12345678A", "ana@correo.com", "Clave1!", "1234567890123456"))
                .getMessage().contains("DNI"));
        assertTrue(assertThrows(InvalidValueException.class,
                () -> UserValidator.validate("12345678Z", "ana@correo.es", "Clave1!", "1234567890123456"))
                .getMessage().contains("email"));
        assertTrue(assertThrows(InvalidValueException.class,
                () -> UserValidator.validate("12345678Z", "ana@correo.com", "clave1!", "1234567890123456"))
                .getMessage().contains("password"));
    }

    @Test
    void noRegularExpressions() throws Exception {
        String source = Files.readString(
                Path.of("src/main/java/es/upm/etsisi/amusementpark/business/model/UserValidator.java"));
        assertFalse(source.contains("matches(") || source.contains("java.util.regex") || source.contains("replaceAll("));
    }
}
