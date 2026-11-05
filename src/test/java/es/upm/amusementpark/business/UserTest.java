package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserTest {

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "$2a$10$hash", "612345678",
                "1234567890123456");
    }

    @Test
    void attributesRoundTrip() {
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
    void missingAttributeRejected() {
        assertThrows(NullPointerException.class,
                () -> new User("Ana", "Garcia Lopez", null, "ana@correo.com", "x", "612345678", "1"));
        assertThrows(NullPointerException.class,
                () -> new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "x", "612345678", null));
    }

    @Test
    void attributesAreEncapsulated() {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
    }

    @Test
    void dniIsImmutable() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
    }

    @Test
    void otherDataCanChange() {
        User user = ana();
        user.setPhone("699999999");
        user.setEmail("ana.nueva@correo.com");
        assertEquals("699999999", user.getPhone());
        assertEquals("ana.nueva@correo.com", user.getEmail());
        assertEquals("12345678Z", user.getDni());
        assertThrows(NullPointerException.class, () -> user.setPhone(null));
    }
}
