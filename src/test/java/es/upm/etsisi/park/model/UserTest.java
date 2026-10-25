package es.upm.etsisi.park.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void exposesAllAttributes() {
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
    void mutableFieldsCanChangeButDniStays() {
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
        assertEquals("Nueva2#", user.getPassword());
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void passwordMatchesIsExact() {
        User user = ana();
        assertTrue(user.passwordMatches("Clave1!"));
        assertFalse(user.passwordMatches("clave1!"));
    }

    @Test
    void usersWithSameDniAreEqual() {
        User other = new User("X", "Y", "12345678Z", "x@correo.com", "Aa1!", "1", "1234567890123456");
        assertEquals(ana(), other);
        assertEquals(ana().hashCode(), other.hashCode());
    }

    @Test
    void fieldNamesResolveCaseInsensitively() {
        assertEquals(UserField.NAME, UserField.fromName("name"));
        assertEquals(UserField.SURNAME, UserField.fromName(" Surname "));
        assertEquals(UserField.CARD, UserField.fromName("CARD"));
        assertNull(UserField.fromName("DNI"));
        assertNull(UserField.fromName(null));
        assertEquals(6, UserField.values().length);
    }
}
