package es.upm.amusementpark.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.model.User;

class UserRepositoryTest {

    @Test
    void storesAndRetrievesUsersByDniAndEmail() {
        UserRepository repo = new UserRepository();
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        repo.add(ana);
        assertEquals(1, repo.size());
        assertSame(ana, repo.findByDni("12345678Z"));
        assertSame(ana, repo.findByEmail("ana@correo.com"));
        assertTrue(repo.existsDni("12345678Z"));
        assertTrue(repo.existsEmail("ana@correo.com"));
        assertFalse(repo.existsDni("87654321X"));
        assertNull(repo.findByEmail("pedro@correo.com"));
        assertEquals(1, repo.findAll().size());
    }
}
