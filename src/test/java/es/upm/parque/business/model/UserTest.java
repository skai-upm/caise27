package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void storesAllAttributesAndKeepsDni() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        u.setName("Ana Maria");
        u.setSurname("Garcia");
        u.setEmail("ana.nueva@correo.com");
        u.setPasswordHash("hash2");
        u.setPhone("699999999");
        u.setCard("9999888877776666");
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana.nueva@correo.com", u.getEmail());
        assertEquals("hash2", u.getPasswordHash());
        assertEquals("699999999", u.getPhone());
        assertEquals("9999888877776666", u.getCard());
    }

    @Test
    void userFieldLookupIsCaseInsensitive() {
        assertEquals(UserField.PHONE, UserField.fromText("phone"));
        assertEquals(UserField.CARD, UserField.fromText(" CARD "));
    }

    @Test
    void dniIsNotAnEditableField() {
        assertThrows(IllegalArgumentException.class, () -> UserField.fromText("DNI"));
        assertThrows(IllegalArgumentException.class, () -> UserField.fromText("AGE"));
    }
}
