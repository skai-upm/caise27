package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
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
    void attributesAreEncapsulated() {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
    }

    @Test
    void dniCannotBeChanged() throws NoSuchFieldException {
        for (Method m : User.class.getMethods()) {
            assertFalse(m.getName().equals("setDni"), "User must not expose setDni");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    void checksPassword() {
        assertTrue(ana.checkPassword("Clave1!"));
        assertFalse(ana.checkPassword("clave1!"));
    }

    @Test
    void copyConstructorCopiesAllValues() {
        User copy = new User(ana);
        assertEquals(UserRepository.toLine(ana), UserRepository.toLine(copy));
    }
}
