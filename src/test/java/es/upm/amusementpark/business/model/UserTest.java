package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "$2a$hash", "612345678",
                "1234567890123456");
    }

    @Test
    void retainsAllAttributes() {
        User user = ana();
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("$2a$hash", user.getPasswordHash());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void attributesAreEncapsulated() {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName() + " must be private");
        }
    }

    @Test
    void dniCannotBeChanged() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        boolean hasDniSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName)
                .anyMatch(name -> name.equals("setDni"));
        assertFalse(hasDniSetter);

        User user = ana();
        user.setFirstName("Ana Maria");
        user.setSurnames("Garcia");
        user.setEmail("ana.nueva@correo.com");
        user.setPasswordHash("$2a$other");
        user.setPhone("699999999");
        user.setCard("9999888877776666");
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void modifiableFieldsAreExactlyTheSix() {
        assertArrayEquals(new UserField[] {UserField.NAME, UserField.SURNAME, UserField.EMAIL,
                UserField.PASSWORD, UserField.PHONE, UserField.CARD}, UserField.values());
        assertEquals(UserField.PHONE, UserField.fromName("PHONE"));
        assertEquals(null, UserField.fromName("DNI"));
        assertEquals(null, UserField.fromName("phone"));
    }
}
