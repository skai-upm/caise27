package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void holdsAllSevenFields() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        assertEquals("Ana", u.getName());
        assertEquals("Garcia Lopez", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void dniIsNotAnEditableField() {
        assertFalse(Arrays.stream(UserField.values()).anyMatch(f -> f.name().equals("DNI")));
        assertEquals(6, UserField.values().length);
    }

    @Test
    void copyConstructorCopiesAllFields() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        User copy = new User(u);
        u.setName("Other");
        assertEquals("Ana", copy.getName());
        assertEquals("12345678Z", copy.getDni());
    }
}
