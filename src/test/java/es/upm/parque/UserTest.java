package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void storesAllAttributes() {
        User user = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        assertEquals("Ana", user.getName());
        assertEquals("Garcia Lopez", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void editableAttributesChange() {
        User user = new User("Ana", "Garcia", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        user.setName("Ana Maria");
        user.setSurname("Lopez");
        user.setEmail("ana.nueva@correo.com");
        user.setPassword("Nueva2#");
        user.setPhone("699999999");
        user.setCard("9999888877776666");
        assertEquals("Ana Maria", user.getName());
        assertEquals("Lopez", user.getSurname());
        assertEquals("ana.nueva@correo.com", user.getEmail());
        assertEquals("Nueva2#", user.getPassword());
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
        assertEquals("12345678Z", user.getDni());
    }
}
