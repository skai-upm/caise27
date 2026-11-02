package es.upm.etsisi.amusementpark.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void exposesAllAttributes() {
        User user = ana();
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void attributesAreEncapsulated() {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName() + " must be private");
        }
    }

    @Test
    void dniIsFinalAndHasNoSetter() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        boolean hasDniSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName)
                .anyMatch(name -> name.equalsIgnoreCase("setDni"));
        assertFalse(hasDniSetter);
    }

    @Test
    void copyIsIndependent() {
        User user = ana();
        User copy = user.copy();
        copy.setFirstName("Ana Maria");
        assertNotSame(user, copy);
        assertEquals("Ana", user.getFirstName());
        assertEquals(user.getDni(), copy.getDni());
    }

    @Test
    void checksPassword() {
        assertTrue(ana().hasPassword("Clave1!"));
        assertFalse(ana().hasPassword("clave1!"));
    }
}
