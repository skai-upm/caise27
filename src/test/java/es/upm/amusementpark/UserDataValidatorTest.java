package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.InvalidValueException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.UserDataValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserDataValidatorTest {

    @Test
    void dniLetterMustMatch() {
        assertTrue(UserDataValidator.isValidDni("12345678Z"));
        assertFalse(UserDataValidator.isValidDni("12345678A"));
        assertTrue(UserDataValidator.isValidDni("87654321X"));
        assertTrue(UserDataValidator.isValidDni("00000000T"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567Z", "123456789Z", "1234567AZ", "12345678", "12345678z", "", "ABCDEFGHZ"})
    void malformedDni(String dni) {
        assertFalse(UserDataValidator.isValidDni(dni));
    }

    @Test
    void emailSourceExamples() {
        assertTrue(UserDataValidator.isValidEmail("ana@correo.com"));
        for (String invalid : new String[] {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es",
            "ana@@correo.com", "ana@correo.com@x.com"}) {
            assertFalse(UserDataValidator.isValidEmail(invalid), invalid);
        }
        assertTrue(UserDataValidator.isValidEmail("ana@mail.correo.com"));
    }

    @Test
    void passwordSourceExamples() {
        assertTrue(UserDataValidator.isValidPassword("Clave1!"));
        for (String invalid : new String[] {"clave1!", "CLAVE1!", "Clave!!", "Clave12", "", "Clave1 "}) {
            assertFalse(UserDataValidator.isValidPassword(invalid), invalid);
        }
    }

    @Test
    void cardSourceExamples() {
        assertTrue(UserDataValidator.isValidCard("1234567890123456"));
        for (String invalid : new String[] {"1234 5678 9012 3456", "123456789012345", "12345678901234567",
            "12345678901234a6"}) {
            assertFalse(UserDataValidator.isValidCard(invalid), invalid);
        }
    }

    @Test
    void combinedCheckNamesEveryInvalidField() {
        InvalidValueException e = assertThrows(InvalidValueException.class,
                () -> UserDataValidator.validate("12345678A", "ana@correo", "Clave1!", "123"));
        assertEquals("invalid user data: DNI, email, card", e.getMessage());
        UserDataValidator.validate("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
    }

    @Test
    void weakPasswordNotStored() {
        ParkController park = ParkController.getInstance();
        park.reset();
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.setPassword("ana@correo.com", "Clave1!");
        assertThrows(InvalidValueException.class, () -> park.setPassword("ana@correo.com", "clave1!"));
        park.verifyPassword("ana@correo.com", "Clave1!");
    }

    @Test
    void validatorUsesNoRegularExpressions() throws IOException {
        String source = Files.readString(Path.of("src/main/java/es/upm/amusementpark/business/validation/"
                + "UserDataValidator.java"), StandardCharsets.UTF_8);
        assertFalse(source.contains("java.util.regex") || source.contains(".matches(")
                || source.contains("replaceAll("));
    }
}
