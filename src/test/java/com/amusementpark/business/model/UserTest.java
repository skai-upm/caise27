package com.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    public void fieldsRoundTrip() {
        User user = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
        user.setName("Ana Maria");
        user.setSurname("Garcia");
        user.setEmail("a@b.com");
        user.setPassword("Nueva2#");
        user.setPhone("699999999");
        user.setCard("9999888877776666");
        assertEquals("Ana Maria", user.getName());
        assertEquals("Garcia", user.getSurname());
        assertEquals("12345678Z", user.getDni());
        assertEquals("a@b.com", user.getEmail());
        assertEquals("Nueva2#", user.getPassword());
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
    }

    @Test
    public void dniIsImmutable() throws Exception {
        for (Method method : User.class.getMethods()) {
            assertTrue(!method.getName().equals("setDni"), "User must not expose setDni");
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
    }

    @Test
    public void attributesArePrivate() {
        for (var field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
    }
}
