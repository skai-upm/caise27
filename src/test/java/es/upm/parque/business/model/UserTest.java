package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void storesAllFields() {
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
    void dniHasNoSetter() {
        boolean hasSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName)
                .anyMatch("setDni"::equals);
        assertEquals(false, hasSetter);
    }

    @Test
    void dniIsNotAnEditableField() {
        assertNull(UserField.fromName("DNI"));
        assertEquals(UserField.CARD, UserField.fromName("CARD"));
    }
}
