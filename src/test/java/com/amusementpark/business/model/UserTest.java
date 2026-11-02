package com.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void storesAllAttributes() {
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
    void attributesAreEncapsulatedAndDniIsImmutable() throws NoSuchFieldException {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        boolean hasDniSetter = Arrays.stream(User.class.getMethods()).map(Method::getName)
                .anyMatch("setDni"::equals);
        assertFalse(hasDniSetter);
    }

    @Test
    void updatableFieldsExcludeDni() {
        assertEquals("[NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD]", Arrays.toString(UserField.values()));
        assertEquals(UserField.EMAIL, UserField.fromText(" email "));
        assertEquals(null, UserField.fromText("DNI"));
    }
}
