package es.upm.etsisi.amusementpark.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserTest {

    static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void exposesAllAttributes() {
        User u = ana();
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void attributesAreEncapsulated() {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
    }

    @Test
    void dniIsFinalAndHasNoSetter() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName)
                .anyMatch(n -> n.equals("setDni")));
    }

    @Test
    void nullDniRejected() {
        assertThrows(NullPointerException.class,
                () -> new User("a", "b", null, "c", "d", "e", "f"));
    }

    @Test
    void copyIsIndependent() {
        User original = ana();
        User copy = new User(original);
        assertNotSame(original, copy);
        copy.setFirstName("Ana Maria");
        copy.setSurnames("X");
        copy.setEmail("x@y.com");
        copy.setPassword("Otra2#");
        copy.setPhone("1");
        copy.setCard("9999888877776666");
        assertEquals("Ana", original.getFirstName());
        assertEquals("ana@correo.com", original.getEmail());
        assertEquals(original.getDni(), copy.getDni());
        assertEquals("Ana Maria", copy.getFirstName());
        assertEquals("X", copy.getSurnames());
        assertEquals("1", copy.getPhone());
        assertEquals("9999888877776666", copy.getCard());
        assertTrue(copy.hasPassword("Otra2#"));
        assertFalse(copy.hasPassword("Clave1!"));
    }
}
