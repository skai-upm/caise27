package es.upm.amusementpark.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.UserFileStore;

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

    @Test
    void persistsChangesAndReloadsThem(@TempDir Path dir) {
        UserFileStore store = new UserFileStore(dir.resolve("users.txt"));
        new UserRepository(store).add(new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456"));
        UserRepository reloaded = new UserRepository(store);
        assertEquals(1, reloaded.size());
        assertTrue(reloaded.existsEmail("ana@correo.com"));
    }

    @Test
    void removalIsPersisted(@TempDir Path dir) {
        UserFileStore store = new UserFileStore(dir.resolve("users.txt"));
        UserRepository repo = new UserRepository(store);
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        repo.add(ana);
        repo.remove(ana);
        assertEquals(0, new UserRepository(store).size());
    }

    @Test
    void updateIsPersisted(@TempDir Path dir) {
        UserFileStore store = new UserFileStore(dir.resolve("users.txt"));
        UserRepository repo = new UserRepository(store);
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        repo.add(ana);
        ana.setPhone("699999999");
        repo.update(ana);
        assertEquals("699999999", new UserRepository(store).findByDni("12345678Z").getPhone());
    }
}
