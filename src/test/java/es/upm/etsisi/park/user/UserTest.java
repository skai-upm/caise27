package es.upm.etsisi.park.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void settersChangeModifiableFields() {
        User user = ana();
        user.set(UserField.NAME, "Ana Maria");
        user.set(UserField.SURNAME, "Garcia");
        user.set(UserField.EMAIL, "ana.nueva@correo.com");
        user.set(UserField.PASSWORD, "Nueva2#");
        user.set(UserField.PHONE, "699999999");
        user.set(UserField.CARD, "9999888877776666");
        assertEquals("Ana Maria", user.getFirstName());
        assertEquals("Garcia", user.getSurnames());
        assertEquals("ana.nueva@correo.com", user.getEmail());
        assertEquals("Nueva2#", user.getPassword());
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void dniIsImmutable() throws Exception {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equalsIgnoreCase("setDni"));
        }
    }

    @Test
    void attributesArePrivate() {
        for (var field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
    }

    @Test
    void checksPassword() {
        assertTrue(ana().checkPassword("Clave1!"));
        assertFalse(ana().checkPassword("clave1!"));
    }

    @Test
    void fieldLookupIgnoresCaseAndExcludesDni() {
        assertEquals(UserField.PHONE, UserField.fromName(" phone "));
        assertNull(UserField.fromName("DNI"));
        assertEquals("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD", UserField.names());
    }
}
