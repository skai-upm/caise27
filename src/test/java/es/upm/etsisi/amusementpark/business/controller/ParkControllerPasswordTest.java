package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkControllerPasswordTest {

    private final ParkController controller = ParkController.getInstance();
    private final User ana = new User("12345678Z", "ana@correo.com");

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.addUser(ana);
    }

    @AfterEach
    void restoreHasher() {
        controller.configure(new BCryptPasswordHasher());
    }

    @Test
    void setAndAuthenticate() {
        controller.setUserPassword("ana@correo.com", "Clave1!");
        assertNotEquals("Clave1!", ana.getPasswordHash());
        assertSame(ana, controller.authenticate("ana@correo.com", "Clave1!"));
        assertThrows(AuthenticationException.class, () -> controller.authenticate("ana@correo.com", "Clave1?"));
        assertThrows(AuthenticationException.class, () -> controller.authenticate("nadie@correo.com", "Clave1!"));
    }

    @Test
    void weakPasswordRejectedAndUnchanged() {
        assertThrows(InvalidValueException.class, () -> controller.setUserPassword("ana@correo.com", "clave"));
        assertNull(ana.getPasswordHash());
        controller.setUserPassword("ana@correo.com", "Clave1!");
        String before = ana.getPasswordHash();
        assertThrows(InvalidValueException.class, () -> controller.setUserPassword("ana@correo.com", "CLAVE1!"));
        assertEquals(before, ana.getPasswordHash());
        assertThrows(NotFoundException.class, () -> controller.setUserPassword("nadie@correo.com", "Clave1!"));
    }

    @Test
    void changeProducesNewProtectedValue() {
        controller.setUserPassword("ana@correo.com", "Clave1!");
        String first = ana.getPasswordHash();
        controller.setUserPassword("ana@correo.com", "Nueva2#");
        assertNotEquals(first, ana.getPasswordHash());
        controller.authenticate("ana@correo.com", "Nueva2#");
        assertThrows(AuthenticationException.class, () -> controller.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void userWithoutPasswordCannotAuthenticate() {
        assertThrows(AuthenticationException.class, () -> controller.authenticate("ana@correo.com", ""));
    }

    @Test
    void hasherIsInjected() {
        PasswordHasher reversing = new PasswordHasher() {
            public String hash(String plain) {
                return new StringBuilder(plain).reverse().toString();
            }

            public boolean matches(String plain, String hash) {
                return hash(plain).equals(hash);
            }
        };
        controller.configure(reversing);
        controller.setUserPassword("ana@correo.com", "Clave1!");
        assertEquals("!1evalC", ana.getPasswordHash());
    }
}
