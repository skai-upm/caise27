package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void retainsAllSevenAttributes() {
        User u = ana();
        assertEquals("Ana", u.getName());
        assertEquals("Garcia Lopez", u.getSurname());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@correo.com", u.getEmail());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("612345678", u.getPhone());
        assertEquals("1234567890123456", u.getCard());
    }

    @Test
    void dniHasNoSetter() {
        boolean hasSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName).anyMatch("setDni"::equals);
        assertFalse(hasSetter);
    }

    @Test
    void editableFieldsCanBeSetGenerically() {
        User u = ana();
        for (UserField f : UserField.values()) {
            u.set(f, "x" + f);
            assertEquals("x" + f, u.get(f));
        }
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void userFieldParsing() {
        assertEquals(UserField.EMAIL, UserField.parse(" email "));
        assertEquals(UserField.CARD, UserField.parse("CARD"));
        assertNull(UserField.parse("DNI"));
        assertNull(UserField.parse(null));
    }
}
