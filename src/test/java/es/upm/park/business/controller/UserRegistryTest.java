package es.upm.park.business.controller;

import es.upm.park.business.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRegistryTest {

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "h", "612345678",
            "1234567890123456");

    @Test
    void storesAndRetrievesByEmailAndDni() {
        UserRegistry registry = new UserRegistry();
        registry.save(ana);
        assertSame(ana, registry.findByEmail("ana@correo.com").orElseThrow());
        assertSame(ana, registry.findByDni("12345678Z").orElseThrow());
        assertEquals(1, registry.all().size());
        registry.remove("ana@correo.com");
        assertTrue(registry.findByEmail("ana@correo.com").isEmpty());
    }

    @Test
    void actsAsUserDirectory() {
        UserRegistry registry = new UserRegistry();
        registry.save(ana);
        assertEquals(Optional.of("12345678Z"), registry.dniOf("ana@correo.com"));
        assertTrue(registry.exists("ana@correo.com"));
        assertTrue(registry.dniOf("nadie@correo.com").isEmpty());
    }
}
