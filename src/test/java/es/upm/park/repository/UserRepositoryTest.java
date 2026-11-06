package es.upm.park.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.park.model.User;

class UserRepositoryTest {

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com",
            "Clave1!", "6", "1234567890123456");

    @Test
    void storesAndFindsUsers() {
        UserRepository repo = new UserRepository();
        assertTrue(repo.findAll().isEmpty());
        repo.add(ana);
        assertSame(ana, repo.findByDni("12345678Z").orElseThrow());
        assertFalse(repo.findByDni("00000000T").isPresent());
        assertTrue(repo.existsByEmail("ana@correo.com"));
        assertFalse(repo.existsByEmail("x@correo.com"));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void listingIsUnmodifiable() {
        UserRepository repo = new UserRepository();
        repo.add(ana);
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().clear());
    }
}
