package es.upm.park.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private User ana() {
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
    void mutableAttributesCanChange() {
        User user = ana();
        user.setFirstName("Ana Maria");
        user.setPhone("699999999");
        assertEquals("Ana Maria", user.getFirstName());
        assertEquals("699999999", user.getPhone());
    }

    @Test
    void dniHasNoSetter() {
        boolean hasDniSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName)
                .anyMatch("setDni"::equals);
        assertFalse(hasDniSetter);
    }

    @Test
    void attributesArePrivate() {
        assertTrue(Arrays.stream(User.class.getDeclaredFields())
                .allMatch(f -> Modifier.isPrivate(f.getModifiers())));
    }

    @Test
    void checksPassword() {
        assertTrue(ana().hasPassword("Clave1!"));
        assertFalse(ana().hasPassword("clave1!"));
    }
}
