package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    void storesAllAttributes() {
        User user = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        assertEquals("Ana", user.getName());
        assertEquals("Garcia Lopez", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void passwordMatchesOnlyExactValue() {
        User user = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertTrue(user.passwordMatches("Clave1!"));
        assertFalse(user.passwordMatches("clave1!"));
    }

    @Test
    void userFieldParsesKnownNamesOnly() {
        assertEquals(UserField.EMAIL, UserField.fromName("EMAIL"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("email"));
    }
}
