package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    static User sample() {
        return new User("Ana", "García López", "12345678Z", "ana@example.com",
                "secret12", "600123456", "4111111111111111");
    }

    @Test
    void createdUserExposesAllAttributes() {
        User u = sample();
        assertEquals("Ana", u.getFirstName());
        assertEquals("García López", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@example.com", u.getEmail());
        assertEquals("secret12", u.getPassword());
        assertEquals("600123456", u.getTelephone());
        assertEquals("4111111111111111", u.getCardNumber());
    }

    @Test
    void setterChangesOnlyItsAttribute() {
        User u = sample();
        u.setEmail("new@example.com");
        assertEquals("new@example.com", u.getEmail());
        assertEquals("Ana", u.getFirstName());
        assertEquals("12345678Z", u.getDni());
        assertEquals("600123456", u.getTelephone());
    }

    @Test
    void usersAreIndependent() {
        User a = sample();
        User b = new User("Luis", "Pérez", "87654321X", "luis@example.com",
                "pass5678", "611222333", "5500000000000004");
        a.setFirstName("Changed");
        assertEquals("Luis", b.getFirstName());
    }

    @Test
    void allFieldsArePrivate() {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName());
        }
    }

    @Test
    void dniIsImmutable() throws NoSuchFieldException {
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        boolean hasDniSetter = Arrays.stream(User.class.getMethods())
                .map(Method::getName).anyMatch(n -> n.equalsIgnoreCase("setDni"));
        assertTrue(!hasDniSetter, "DNI must not have a setter");
        User u = sample();
        assertEquals("12345678Z", u.getDni());
    }
}
