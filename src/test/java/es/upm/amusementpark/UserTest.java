package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    public void gettersReturnConstructorValues() {
        User u = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        assertEquals("Ana", u.getName());
        assertEquals("Garcia Lopez", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    public void dniIsNotAnEditableField() {
        assertNull(UserField.fromName("DNI"));
        assertEquals(6, UserField.values().length);
        assertEquals(UserField.PHONE, UserField.fromName("PHONE"));
    }
}
