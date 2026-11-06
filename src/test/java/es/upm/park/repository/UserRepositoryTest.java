package es.upm.park.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.model.User;
import es.upm.park.persistence.PersistenceException;
import es.upm.park.persistence.UserFileStore;

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

    @Test
    void persistentRepositoryReloadsUsers(@TempDir Path dir) {
        Path file = dir.resolve("users.txt");
        new UserRepository(new UserFileStore(file)).add(ana);
        UserRepository reloaded = new UserRepository(new UserFileStore(file));
        assertEquals(1, reloaded.findAll().size());
        assertEquals("ana@correo.com", reloaded.findByDni("12345678Z").orElseThrow().getEmail());
    }

    @Test
    void failedSaveLeavesRepositoryUnchanged(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        Files.createDirectory(file);
        assertThrows(PersistenceException.class, () -> repo.add(ana));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void removedUserIsGoneAfterReload(@TempDir Path dir) {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        repo.add(ana);
        assertSame(ana, repo.findByEmail("ana@correo.com").orElseThrow());
        repo.remove(ana);
        assertTrue(repo.findAll().isEmpty());
        assertTrue(new UserRepository(new UserFileStore(file)).findAll().isEmpty());
    }

    @Test
    void failedRemoveKeepsUser(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        repo.add(ana);
        Files.delete(file);
        Files.createDirectory(file);
        assertThrows(PersistenceException.class, () -> repo.remove(ana));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void replacedUserIsReloaded(@TempDir Path dir) {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        repo.add(ana);
        repo.update(ana, u -> u.setFirstName("Ana Maria"));
        assertSame(ana, repo.findByDni("12345678Z").orElseThrow());
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Ana Maria", new UserRepository(new UserFileStore(file)).findAll().get(0).getFirstName());
    }

    @Test
    void failedReplaceKeepsOriginal(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        repo.add(ana);
        Files.delete(file);
        Files.createDirectory(file);
        assertThrows(PersistenceException.class, () -> repo.update(ana, u -> u.setFirstName("X")));
        assertSame(ana, repo.findAll().get(0));
        assertEquals("Ana", ana.getFirstName());
    }
}
