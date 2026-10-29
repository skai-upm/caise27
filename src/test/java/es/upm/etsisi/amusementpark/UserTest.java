package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void retainsAllData() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
        assertTrue(u.checkPassword("Clave1!"));
        assertFalse(u.checkPassword("Clave1?"));
    }

    @Test
    void attributesAreEncapsulatedAndDniIsImmutable() throws Exception {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method m : User.class.getMethods()) {
            assertFalse(m.getName().equals("setDni"), "DNI must not have a setter");
        }
    }

    @Test
    void modifiableFieldsEnumHasExactlyTheAllowedFields() {
        String[] names = new String[UserField.values().length];
        for (int i = 0; i < names.length; i++) {
            names[i] = UserField.values()[i].name();
        }
        assertArrayEquals(new String[] {"NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"}, names);
        assertEquals(UserField.CARD, UserField.fromName("CARD"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("AGE"));
    }
}
