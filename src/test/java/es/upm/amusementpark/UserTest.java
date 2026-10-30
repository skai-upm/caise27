package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void keepsAllAttributes() {
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
    void modifiableAttributesCanChange() {
        User user = ana();
        user.setFirstName("Ana Maria");
        user.setSurnames("Garcia");
        user.setEmail("ana.nueva@correo.com");
        user.setPassword("Nueva2#");
        user.setPhone("699999999");
        user.setCard("9999888877776666");
        assertEquals("Ana Maria", user.getFirstName());
        assertEquals("Garcia", user.getSurnames());
        assertEquals("ana.nueva@correo.com", user.getEmail());
        assertTrue(user.checkPassword("Nueva2#"));
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void dniHasNoSetter() {
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equals("setDni"));
        }
    }

    @Test
    void dniIsNotAModifiableField() {
        assertNull(UserField.fromName("DNI"));
        assertEquals(UserField.EMAIL, UserField.fromName("EMAIL"));
        assertEquals(6, UserField.values().length);
    }

    @Test
    void checkPasswordRejectsWrongPassword() {
        assertFalse(ana().checkPassword("Otra2#"));
    }
}
