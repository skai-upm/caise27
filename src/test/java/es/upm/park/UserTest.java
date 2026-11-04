package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void retainsAllFields() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        assertEquals("Ana", u.getName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void dniHasNoSetter() {
        Method[] methods = User.class.getMethods();
        assertTrue(Arrays.stream(methods).noneMatch(m -> m.getName().equals("setDni")));
    }

    @Test
    void editableFieldsExcludeDni() {
        assertTrue(Arrays.stream(UserField.values()).noneMatch(f -> f.name().equals("DNI")));
        assertEquals(6, UserField.values().length);
    }
}
