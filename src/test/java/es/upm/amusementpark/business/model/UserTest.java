package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "$2a$04$hash", "612345678", "1234567890123456");
    }

    @Test
    void holdsAllAttributes() {
        User user = ana();
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("$2a$04$hash", user.getPasswordHash());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void attributesArePrivateAndDniIsImmutable() throws NoSuchFieldException {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equals("setDni"));
        }
    }

    @Test
    void copyIsIndependent() {
        User original = ana();
        User copy = new User(original);
        copy.setPhone("699999999");
        assertEquals("612345678", original.getPhone());
        assertEquals("699999999", copy.getPhone());
        assertEquals(original.getDni(), copy.getDni());
    }

    @Test
    void storesOnlyTheHash() {
        User user = ana();
        user.setPasswordHash("$2a$04$other");
        assertEquals("$2a$04$other", user.getPasswordHash());
        for (java.lang.reflect.Field field : User.class.getDeclaredFields()) {
            assertFalse(field.getName().equals("password"), "no plain password field");
        }
    }
}
