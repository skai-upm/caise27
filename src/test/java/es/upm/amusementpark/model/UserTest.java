package es.upm.amusementpark.model;

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
        return new User("Ana", "García López", "12345678Z", "ana@example.com", "secret1", "600123456",
                "4111111111111111");
    }

    @Test
    void keepsAllAttributes() {
        User u = ana();
        assertEquals("Ana", u.getFirstName());
        assertEquals("García López", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@example.com", u.getEmail());
        assertEquals("secret1", u.getPassword());
        assertEquals("600123456", u.getPhone());
        assertEquals("4111111111111111", u.getCardNumber());
    }

    @Test
    void nonDniAttributesCanBeUpdated() {
        User u = ana();
        u.setFirstName("Eva");
        u.setSurnames("Pérez");
        u.setEmail("new@example.com");
        u.setPassword("other22");
        u.setPhone("611111111");
        u.setCardNumber("5500000000000004");
        assertEquals("Eva", u.getFirstName());
        assertEquals("Pérez", u.getSurnames());
        assertEquals("new@example.com", u.getEmail());
        assertEquals("other22", u.getPassword());
        assertEquals("611111111", u.getPhone());
        assertEquals("5500000000000004", u.getCardNumber());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void dniCannotBeChanged() throws NoSuchFieldException {
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    void stateIsEncapsulated() {
        assertEquals(0, User.class.getFields().length);
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName());
            assertFalse(f.getType().getName().startsWith("java.io"), f.getName());
        }
    }

    @Test
    void identityIsTheDni() {
        User other = new User("X", "Y", "12345678Z", "x@example.com", "p", "1", "2");
        assertEquals(ana(), other);
        assertEquals(ana().hashCode(), other.hashCode());
    }
}
