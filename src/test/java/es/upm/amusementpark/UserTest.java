package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void userKeepsItsData() {
        User user = ana();
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
    }

    @Test
    void mutableAttributesCanChange() {
        User user = ana();
        user.setPhone("699999999");
        user.setEmail("ana.nueva@correo.com");
        assertEquals("699999999", user.getPhone());
        assertEquals("ana.nueva@correo.com", user.getEmail());
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void dniCannotBeChanged() throws NoSuchFieldException {
        Field dni = User.class.getDeclaredField("dni");
        assertTrue(Modifier.isFinal(dni.getModifiers()));
        assertTrue(Arrays.stream(User.class.getMethods()).map(Method::getName).noneMatch("setDni"::equals));
    }

    @Test
    void attributesAreEncapsulated() {
        assertTrue(Arrays.stream(User.class.getDeclaredFields())
                .allMatch(f -> Modifier.isPrivate(f.getModifiers())));
    }
}
