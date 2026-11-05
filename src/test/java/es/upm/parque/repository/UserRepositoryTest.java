package es.upm.parque.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.parque.model.User;

class UserRepositoryTest {

    private static User user(String dni, String email) {
        return new User("N", "S", dni, email, "Clave1!", "600000000", "1234567890123456");
    }

    @Test
    void addedUserCanBeFoundByDni() {
        UserRepository repo = new UserRepository();
        User u = user("12345678Z", "ana@correo.com");
        repo.add(u);
        assertSame(u, repo.findByDni("12345678Z"));
        assertNull(repo.findByDni("87654321X"));
        assertEquals(1, repo.size());
    }

    @Test
    void existenceChecksByDniAndEmail() {
        UserRepository repo = new UserRepository();
        repo.add(user("12345678Z", "ana@correo.com"));
        assertTrue(repo.existsByDni("12345678Z"));
        assertFalse(repo.existsByDni("87654321X"));
        assertTrue(repo.existsByEmail("ana@correo.com"));
        assertFalse(repo.existsByEmail("luis@correo.com"));
    }

    @Test
    void findAllKeepsInsertionOrder() {
        UserRepository repo = new UserRepository();
        repo.add(user("87654321X", "b@correo.com"));
        repo.add(user("12345678Z", "a@correo.com"));
        assertEquals("87654321X", repo.findAll().get(0).getDni());
        assertEquals("12345678Z", repo.findAll().get(1).getDni());
    }
}
