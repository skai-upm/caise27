package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void dniIsFinalAndHasNoSetter() throws NoSuchFieldException {
        Field dni = User.class.getDeclaredField("dni");
        assertEquals(true, Modifier.isFinal(dni.getModifiers()));
        assertEquals(true, Modifier.isPrivate(dni.getModifiers()));
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equalsIgnoreCase("setDni"));
        }
    }

    @Test
    void allAttributesArePrivate() {
        for (Field field : User.class.getDeclaredFields()) {
            assertEquals(true, Modifier.isPrivate(field.getModifiers()), field.getName());
        }
    }

    @Test
    void genericAccessorsMapEachField() {
        User user = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        for (UserField field : UserField.values()) {
            user.set(field, "v-" + field);
            assertEquals("v-" + field, user.get(field));
        }
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void updatableFieldsExcludeDni() {
        assertEquals(6, UserField.values().length);
        assertNull(UserField.fromName("DNI"));
        assertEquals(UserField.PHONE, UserField.fromName("PHONE"));
        assertNull(UserField.fromName("phone"));
    }
}
