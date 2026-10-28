package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void holdsAllAttributes() {
        User user = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "$2a$hash", "612345678",
                "1234567890123456");
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("$2a$hash", user.getPasswordHash());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void mutableAttributesExceptDni() {
        User user = new User("12345678Z", "ana@correo.com");
        user.setEmail("ana.nueva@correo.com");
        user.setPhone("699999999");
        user.setFirstName("Ana Maria");
        assertEquals("ana.nueva@correo.com", user.getEmail());
        assertEquals("699999999", user.getPhone());
        assertEquals("Ana Maria", user.getFirstName());
    }

    @Test
    void encapsulationAndImmutableDni() throws Exception {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equals("setDni"));
        }
    }
}
