package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class UserTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
            "Clave1!", "612345678", "1234567890123456");

    @Test
    void exposesAllAttributes() {
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void checksPassword() {
        assertTrue(ana.checkPassword("Clave1!"));
        assertFalse(ana.checkPassword("clave1!"));
    }

    @Test
    void dniCannotBeModified() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method m : User.class.getDeclaredMethods()) {
            assertFalse(m.getName().equalsIgnoreCase("setDni"), "DNI must have no setter");
        }
    }

    @Test
    void attributesAreEncapsulated() {
        for (var f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
    }
}
