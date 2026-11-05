package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserTest {

    private static User ana() {
        return new User("Ana", "García López", "12345678Z", "ana@example.com",
                "Secret12", "600123456", "4111111111111111");
    }

    @Test
    void returnsAllAttributesUnchanged() {
        User u = ana();
        assertEquals("Ana", u.getFirstName());
        assertEquals("García López", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@example.com", u.getEmail());
        assertEquals("Secret12", u.getPassword());
        assertEquals("600123456", u.getTelephone());
        assertEquals("4111111111111111", u.getCardNumber());
    }

    @Test
    void mutableAttributesChangeAndDniStays() {
        User u = ana();
        u.setFirstName("Ane");
        u.setSurnames("Pérez");
        u.setEmail("ana.garcia@example.com");
        u.setPassword("Other123");
        u.setTelephone("611000000");
        u.setCardNumber("5500000000000004");
        assertEquals("Ane", u.getFirstName());
        assertEquals("Pérez", u.getSurnames());
        assertEquals("ana.garcia@example.com", u.getEmail());
        assertEquals("Other123", u.getPassword());
        assertEquals("611000000", u.getTelephone());
        assertEquals("5500000000000004", u.getCardNumber());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void usersAreIndependent() {
        User a = ana();
        User b = new User("Luis", "Martín", "87654321X", "luis@example.com", "Pass1234", "622000000", "4000000000000002");
        a.setEmail("changed@example.com");
        assertEquals("luis@example.com", b.getEmail());
    }

    @Test
    void stateIsEncapsulatedAndDniHasNoSetter() {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
        }
        assertTrue(Modifier.isFinal(fieldModifiers("dni")));
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
    }

    private static int fieldModifiers(String name) {
        try {
            return User.class.getDeclaredField(name).getModifiers();
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
}
