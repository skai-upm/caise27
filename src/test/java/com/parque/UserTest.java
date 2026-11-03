package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void retainsAllSevenFields() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        assertEquals("Ana", u.getName());
        assertEquals("Garcia Lopez", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void dniIsImmutableAndFieldsEncapsulated() throws Exception {
        for (Method m : User.class.getMethods()) {
            assertTrue(!m.getName().equals("setDni"), "User must not expose setDni");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (var f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
    }

    @Test
    void editableFieldsAreExactlyTheSix() {
        assertEquals("[NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD]",
                java.util.Arrays.toString(UserField.values()));
        assertEquals(null, UserField.fromName("DNI"));
    }
}
