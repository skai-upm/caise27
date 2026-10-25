package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void dniHasNoSetterAndIsFinal() throws Exception {
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
        assertEquals(true, Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    void editableFieldsChangeThroughSetters() {
        User u = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        u.setName("Ana Maria");
        u.setPhone("699999999");
        assertEquals("Ana Maria", u.getName());
        assertEquals("699999999", u.getPhone());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void userFieldParsesCommandValues() {
        assertEquals(UserField.EMAIL, UserField.fromString("EMAIL"));
        assertEquals(null, UserField.fromString("DNI"));
    }
}
