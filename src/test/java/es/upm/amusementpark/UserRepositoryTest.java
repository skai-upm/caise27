package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserRepositoryTest {

    @TempDir
    Path dir;

    private static User user(String email) {
        return new User("Ana", "Garcia Lopez", "12345678Z", email,
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void storedUserCanBeRetrievedByEmail() {
        UserRepository repository = new UserRepository();
        User ana = user("ana@correo.com");
        repository.save(ana);
        assertSame(ana, repository.findByEmail("ana@correo.com").orElseThrow());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void unknownEmailReturnsNoUser() {
        assertTrue(new UserRepository().findByEmail("nobody@correo.com").isEmpty());
    }

    @Test
    void deletedUserIsNoLongerRetrievable() {
        UserRepository repository = new UserRepository();
        repository.save(user("ana@correo.com"));
        assertTrue(repository.delete("ana@correo.com"));
        assertTrue(repository.findByEmail("ana@correo.com").isEmpty());
        assertFalse(repository.delete("ana@correo.com"));
    }

    @Test
    void savedUserIsLoadedByNewRepositoryOnSameFile() {
        Path file = dir.resolve("users.txt");
        new UserRepository(new UserFileStorage(file)).save(user("ana@correo.com"));
        UserRepository reloaded = new UserRepository(new UserFileStorage(file));
        assertEquals("12345678Z", reloaded.findByEmail("ana@correo.com").orElseThrow().getDni());
        assertTrue(reloaded.existsByDni("12345678Z"));
    }

    @Test
    void deletionIsPersisted() {
        Path file = dir.resolve("users.txt");
        UserRepository repository = new UserRepository(new UserFileStorage(file));
        repository.save(user("ana@correo.com"));
        repository.delete("ana@correo.com");
        assertTrue(new UserRepository(new UserFileStorage(file)).findAll().isEmpty());
    }

    @Test
    void updateReKeysUserByNewEmailAndPersists() {
        Path file = dir.resolve("users.txt");
        UserRepository repository = new UserRepository(new UserFileStorage(file));
        User ana = user("ana@correo.com");
        repository.save(ana);
        ana.setEmail("ana.nueva@correo.com");
        repository.update("ana@correo.com", ana);
        UserRepository reloaded = new UserRepository(new UserFileStorage(file));
        assertTrue(reloaded.findByEmail("ana@correo.com").isEmpty());
        assertTrue(reloaded.findByEmail("ana.nueva@correo.com").isPresent());
        assertEquals(1, reloaded.findAll().size());
    }
}
