package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class UserTest {

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
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
    void attributesArePrivateAndDniIsFinal() throws NoSuchFieldException {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    void offersNoDniMutator() {
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().toLowerCase().contains("setdni"));
        }
    }

    @Test
    void copyIsIndependent() {
        User original = ana();
        User copy = new User(original);
        copy.setFirstName("Other");
        assertEquals("Ana", original.getFirstName());
        assertEquals(original.getDni(), copy.getDni());
    }

    @Test
    void checksPassword() {
        assertTrue(ana().checkPassword("Clave1!"));
        assertFalse(ana().checkPassword("clave1!"));
    }
}
