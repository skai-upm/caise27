package es.upm.amusementpark.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.User;
import org.junit.jupiter.api.Test;

class UserRepositoryTest {

    @Test
    void addAndFind() {
        UserRepository repo = new UserRepository();
        User u = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        repo.add(u);
        assertEquals(1, repo.count());
        assertEquals(u, repo.findByDni("12345678Z").orElseThrow());
        assertEquals(u, repo.findByEmail("ana@correo.com").orElseThrow());
        assertTrue(repo.findByDni("87654321X").isEmpty());
        assertEquals(1, repo.findAll().size());
    }
}
