package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void exposesAllFields() {
        User u = ana();
        assertEquals("Ana", u.getFirstName());
        assertEquals("Garcia Lopez", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void editableFieldsCanChange() {
        User u = ana();
        u.setFirstName("Ana Maria");
        u.setSurnames("Garcia Ruiz");
        u.setEmail("ana.nueva@correo.com");
        u.setPassword("Nueva2#");
        u.setPhone("699999999");
        u.setCardNumber("9999888877776666");
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("Garcia Ruiz", u.getSurnames());
        assertEquals("ana.nueva@correo.com", u.getEmail());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("699999999", u.getPhone());
        assertEquals("9999888877776666", u.getCardNumber());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void dniHasNoMutator() {
        boolean hasDniSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName)
                .anyMatch(n -> n.equalsIgnoreCase("setDni"));
        assertFalse(hasDniSetter);
    }

    @Test
    void copyIsIndependent() {
        User original = ana();
        User copy = new User(original);
        copy.setFirstName("Otra");
        assertNotSame(original, copy);
        assertEquals("Ana", original.getFirstName());
        assertEquals(original.getDni(), copy.getDni());
    }

    @Test
    void updateFieldsExcludeDni() {
        assertEquals(6, UserField.values().length);
        assertFalse(Arrays.stream(UserField.values()).anyMatch(f -> f.name().equals("DNI")));
    }
}
