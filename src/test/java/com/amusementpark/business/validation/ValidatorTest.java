package com.amusementpark.business.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678Z", "87654321X", "00000000T"})
    public void validDni(String dni) {
        assertTrue(Validator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678A", "1234567Z", "123456789Z", "1234567AZ", "123456789", "", "12345678"})
    public void invalidDni(String dni) {
        assertFalse(Validator.isValidDni(dni));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana@correo.com", "ana.nueva@correo.com", "pedro@mail.org"})
    public void validEmail(String email) {
        assertTrue(Validator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana", "ana@correo", "@correo.com", "ana@.com", "ana@correo.es", "a@b@c.com", "ana@correo.comm", ""})
    public void invalidEmail(String email) {
        assertFalse(Validator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Clave1!", "Nueva2#", "Otra2#"})
    public void validPassword(String password) {
        assertTrue(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"clave1!", "CLAVE1!", "Clave!!", "Clave12", ""})
    public void invalidPassword(String password) {
        assertFalse(Validator.isValidPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567890123456", "9999888877776666"})
    public void validCard(String card) {
        assertTrue(Validator.isValidCard(card));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234 5678 9012 3456", "123456789012345", "12345678901234567", "123456789012345A", "123456789012345-"})
    public void invalidCard(String card) {
        assertFalse(Validator.isValidCard(card));
    }
}
