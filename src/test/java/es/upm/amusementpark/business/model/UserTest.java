package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void holdsAllAttributes() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPasswordHash());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void attributesArePrivateAndDniIsFinal() throws Exception {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    void noDniSetter() {
        for (Method m : User.class.getMethods()) {
            assertTrue(!m.getName().equals("setDni"), "DNI must not be modifiable");
        }
    }

    @Test
    void fieldEnumExcludesDni() {
        assertEquals(UserField.NAME, UserField.fromName("NAME"));
        assertEquals(null, UserField.fromName("DNI"));
        assertEquals(6, UserField.values().length);
    }
}
