package es.upm.parque.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    private User ana() {
        return new User("Ana", "García López", "12345678Z", "ana@example.com",
                "secreto1", "600123456", "4111111111111111");
    }

    @Test
    void createdUserReturnsAllSuppliedAttributes() {
        User u = ana();
        assertEquals("Ana", u.getFirstName());
        assertEquals("García López", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("ana@example.com", u.getEmail());
        assertEquals("secreto1", u.getPassword());
        assertEquals("600123456", u.getTelephone());
        assertEquals("4111111111111111", u.getCardNumber());
    }

    @Test
    void changingEmailLeavesOtherAttributesUnchanged() {
        User u = ana();
        u.setEmail("nuevo@example.com");
        assertEquals("nuevo@example.com", u.getEmail());
        assertEquals("Ana", u.getFirstName());
        assertEquals("García López", u.getSurnames());
        assertEquals("12345678Z", u.getDni());
        assertEquals("secreto1", u.getPassword());
        assertEquals("600123456", u.getTelephone());
        assertEquals("4111111111111111", u.getCardNumber());
    }

    @Test
    void usersAreIndependent() {
        User a = ana();
        User b = new User("Luis", "Pérez", "87654321X", "luis@example.com",
                "clave22", "611222333", "5500000000000004");
        a.setFirstName("Anabel");
        a.setTelephone("699999999");
        assertEquals("Luis", b.getFirstName());
        assertEquals("611222333", b.getTelephone());
        assertEquals("87654321X", b.getDni());
    }

    @Test
    void dniRemainsAfterOtherModifications() {
        User u = ana();
        u.setFirstName("X");
        u.setSurnames("Y");
        u.setPassword("Z");
        u.setCardNumber("1");
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void attributesAreEncapsulatedAndDniIsImmutable() throws Exception {
        for (Field f : User.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {
                assertTrue(Modifier.isPrivate(f.getModifiers()), "field not private: " + f.getName());
            }
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        boolean hasDniMutator = Arrays.stream(User.class.getMethods())
                .map(Method::getName)
                .anyMatch(n -> n.equalsIgnoreCase("setDni"));
        assertTrue(!hasDniMutator, "DNI must not have a mutator");
    }
}
