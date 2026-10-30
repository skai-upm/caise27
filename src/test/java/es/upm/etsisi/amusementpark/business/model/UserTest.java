package es.upm.etsisi.amusementpark.business.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "hash", "612345678", "1234567890123456");
    }

    @Test
    void keepsAllAttributes() {
        User u = ana();
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("hash", u.getPasswordHash());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void attributesArePrivateAndDniIsFinal() throws NoSuchFieldException {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    void dniHasNoSetter() {
        assertTrue(Arrays.stream(User.class.getMethods()).map(Method::getName).noneMatch("setDni"::equals));
    }

    @Test
    void updateFieldsDoNotIncludeDni() {
        assertNull(UserField.fromName("DNI"));
        assertEquals(UserField.PHONE, UserField.fromName("PHONE"));
        assertNull(UserField.fromName("phone"));
    }
}
