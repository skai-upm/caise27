package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.security.PasswordProtector;
import es.upm.amusementpark.persistence.TextFileRepositories;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserModelTest {

    private final PasswordProtector protector = new PasswordProtector();

    @TempDir
    Path dir;

    private User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", protector.protect("Clave1!"),
                "612345678", "1234567890123456");
    }

    @Test
    void keepsAllAttributes() {
        User user = ana();
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("12345678Z", user.getDni());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("612345678", user.getPhone());
        assertEquals("1234567890123456", user.getCard());
        assertFalse(user.getPasswordHash().contains("Clave1!"));
        assertTrue(protector.matches("Clave1!", user.getPasswordHash()));
    }

    @Test
    void attributesAreEncapsulatedAndDniIsImmutable() throws Exception {
        for (Field field : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(field.getModifiers()), field.getName());
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        for (Method method : User.class.getMethods()) {
            assertFalse(method.getName().equals("setDni"));
        }
    }

    @Test
    void otherAttributesChangeThroughOperations() {
        User user = ana();
        user.setPhone("699999999");
        assertEquals("699999999", user.getPhone());
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void identityIsTheDni() {
        User user = ana();
        User same = new User("ana@correo.com", "12345678Z");
        user.setEmail("ana.nueva@correo.com");
        assertEquals(same, user);
        assertEquals(same.hashCode(), user.hashCode());
    }

    @Test
    void allAttributesSurviveRestart() throws IOException {
        ParkController park = ParkController.getInstance();
        park.reset();
        park.registerUser(ana());
        park.configure(TextFileRepositories.create(dir));
        park.persist();
        assertFalse(Files.readString(dir.resolve("users.txt"), StandardCharsets.UTF_8).contains("Clave1!"));
        park.reset();
        park.configure(TextFileRepositories.create(dir));
        assertTrue(park.load().isEmpty());
        User loaded = park.findUser("ana@correo.com");
        assertEquals("Ana", loaded.getFirstName());
        assertEquals("Garcia Lopez", loaded.getSurnames());
        assertEquals("12345678Z", loaded.getDni());
        assertEquals("612345678", loaded.getPhone());
        assertEquals("1234567890123456", loaded.getCard());
        park.verifyPassword("ana@correo.com", "Clave1!");
    }
}
