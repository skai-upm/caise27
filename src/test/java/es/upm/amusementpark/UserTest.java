package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void attributesAreEncapsulated() {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
    }

    @Test
    void dniIsImmutable() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method m : User.class.getMethods()) {
            assertTrue(!m.getName().equals("setDni"), "User must not expose setDni");
        }
    }

    @Test
    void fieldsRoundTrip() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        u.setName("Ana Maria");
        u.setPhone("699999999");
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia Lopez", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("699999999", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void editableFieldsEnum() {
        assertArrayEquals(new String[] {"NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"},
                java.util.Arrays.stream(UserField.values()).map(Enum::name).toArray(String[]::new));
    }
}
