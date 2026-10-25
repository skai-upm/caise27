package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

public class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    public void storesAllFields() {
        User user = ana();
        assertEquals("Ana", user.getName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    public void settersChangeEditableFields() {
        User user = ana();
        user.setName("Ana Maria");
        user.setSurnames("Lopez");
        user.setEmail("nueva@correo.com");
        user.setPassword("Nueva2#");
        user.setPhone("699999999");
        user.setCard("9999888877776666");
        assertEquals("Ana Maria", user.getName());
        assertEquals("Lopez", user.getSurnames());
        assertEquals("nueva@correo.com", user.getEmail());
        assertTrue(user.passwordMatches("Nueva2#"));
        assertFalse(user.passwordMatches("Clave1!"));
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
    }

    @Test
    public void dniIsImmutableAndFieldsArePrivate() throws Exception {
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equals("setDni"), "User must not expose setDni");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (var field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName() + " must be private");
        }
    }

    @Test
    public void userFieldParsesNamesCaseInsensitively() {
        assertEquals(UserField.NAME, UserField.fromName("name"));
        assertEquals(UserField.CARD, UserField.fromName("CARD"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("AGE"));
    }
}
