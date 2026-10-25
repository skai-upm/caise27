package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    void storesAllAttributes() {
        User user = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "hash", "612345678",
                "1234567890123456");
        assertEquals("Ana", user.getName());
        assertEquals("Garcia Lopez", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("hash", user.getPasswordHash());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void userFieldParsesKnownNamesOnly() {
        assertEquals(UserField.EMAIL, UserField.fromName("EMAIL"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName("email"));
    }
}
