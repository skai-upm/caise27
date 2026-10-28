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

    private static User sample() {
        return new User("Ana", "García López", "12345678Z", "ana@example.com", "secret1", "600123456",
                "4111111111111111");
    }

    @Test
    void retainsAllAttributesGivenAtCreation() {
        User user = sample();
        assertEquals("Ana", user.getFirstName());
        assertEquals("García López", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@example.com", user.getEmail());
        assertEquals("secret1", user.getPassword());
        assertEquals("600123456", user.getPhone());
        assertEquals("4111111111111111", user.getCardNumber());
    }

    @Test
    void settersChangeOnlyTheirAttribute() {
        User user = sample();
        user.setEmail("ana.garcia@example.com");
        user.setFirstName("Anabel");
        user.setSurnames("García");
        user.setPassword("other2");
        user.setPhone("611000000");
        user.setCardNumber("5500000000000004");
        assertEquals("ana.garcia@example.com", user.getEmail());
        assertEquals("Anabel", user.getFirstName());
        assertEquals("García", user.getSurnames());
        assertEquals("other2", user.getPassword());
        assertEquals("611000000", user.getPhone());
        assertEquals("5500000000000004", user.getCardNumber());
        assertEquals("12345678Z", user.getDni());
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
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
    }
}
