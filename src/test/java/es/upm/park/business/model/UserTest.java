package es.upm.park.business.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "$2a$10$hash", "612345678",
                "1234567890123456");
    }

    @Test
    void attributesAreReadBack() {
        User user = ana();
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("$2a$10$hash", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void dniStaysWhenOtherDataChanges() {
        User user = ana();
        user.setFirstName("Ana Maria");
        user.setSurnames("Garcia");
        user.setEmail("ana.nueva@correo.com");
        user.setPhone("699999999");
        user.setCard("9999999999999999");
        user.setPassword("$2a$10$other");
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana.nueva@correo.com", user.getEmail());
    }

    @Test
    void encapsulationAndNoDniSetter() throws NoSuchFieldException {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
    }
}
