package es.upm.etsisi.amusementpark.business.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void encapsulatedWithImmutableDni() throws NoSuchFieldException {
        for (Field f : User.class.getDeclaredFields()) {
            assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName());
        }
        assertTrue(Modifier.isFinal(User.class.getDeclaredField("dni").getModifiers()));
        assertFalse(Arrays.stream(User.class.getMethods()).map(Method::getName).anyMatch("setDni"::equals));
        assertThrows(NullPointerException.class, () -> new User("a", "b", null, "c", "d", "e", "f"));
        User u = new User("Ana", "Garcia", "12345678Z", "a@b.com", "h", "6", "1");
        u.setSurnames("X");
        assertEquals("X", u.getSurnames());
    }

    @Test
    void bcryptHasher() {
        PasswordHasher hasher = new BCryptPasswordHasher(4);
        String hash = hasher.hash("Clave1!");
        assertNotEquals("Clave1!", hash);
        assertTrue(hash.startsWith("$2a$04$"));
        assertTrue(hasher.matches("Clave1!", hash));
        assertFalse(hasher.matches("Clave2!", hash));
        assertFalse(hasher.matches("Clave1!", "Clave1!"));
        assertFalse(hasher.matches("Clave1!", null));
        assertNotEquals(hash, hasher.hash("Clave1!"));
    }
}
