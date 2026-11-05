package es.upm.parque.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.model.User;
import es.upm.parque.persistence.UserFileStore;

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
    void fileBackedRepositoryLoadsAtConstructionAndSavesAfterAdd(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository first = new UserRepository(new UserFileStore(file));
        first.add(user("12345678Z", "ana@correo.com"));
        assertEquals(1, Files.readAllLines(file).size());

        UserRepository second = new UserRepository(new UserFileStore(file));
        assertEquals(1, second.size());
        assertTrue(second.existsByDni("12345678Z"));
        assertTrue(second.existsByEmail("ana@correo.com"));

        second.add(user("87654321X", "luis@correo.com"));
        assertEquals(2, Files.readAllLines(file).size());
    }

    @Test
    void findByEmailAndRemovePersist(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        repo.add(user("12345678Z", "ana@correo.com"));
        repo.add(user("87654321X", "luis@correo.com"));
        assertEquals("87654321X", repo.findByEmail("luis@correo.com").getDni());
        assertNull(repo.findByEmail("nadie@correo.com"));

        repo.remove("12345678Z");
        assertEquals(1, repo.size());
        assertEquals(1, Files.readAllLines(file).size());
        UserRepository reloaded = new UserRepository(new UserFileStore(file));
        assertFalse(reloaded.existsByDni("12345678Z"));
        assertTrue(reloaded.existsByDni("87654321X"));
    }

    @Test
    void updatePersistsInPlaceChanges(@TempDir Path dir) {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        User u = user("12345678Z", "ana@correo.com");
        repo.add(u);
        u.setCardNumber("9999888877776666");
        repo.update(u);
        assertEquals("9999888877776666",
                new UserRepository(new UserFileStore(file)).findByDni("12345678Z").getCardNumber());
    }

    @Test
    void invalidAndDuplicateStoredLinesAreIgnoredOnLoad(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        Files.write(file, java.util.List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "Mal;Dni;12345678A;mal@correo.com;Clave1!;600000000;1234567890123456",
                "Otra;Ana;12345678Z;otra@correo.com;Clave1!;600000000;1234567890123456",
                "Mismo;Email;87654321X;ana@correo.com;Clave1!;600000000;1234567890123456",
                "short;line",
                ""));
        UserRepository repo = new UserRepository(new UserFileStore(file));
        assertEquals(1, repo.size());
        assertEquals("Ana", repo.findByDni("12345678Z").getFirstName());
        assertEquals(4, repo.getIgnoredOnLoad());
    }

    @Test
    void cleanFileHasNoIgnoredLines(@TempDir Path dir) {
        Path file = dir.resolve("users.txt");
        UserRepository repo = new UserRepository(new UserFileStore(file));
        repo.add(new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(0, new UserRepository(new UserFileStore(file)).getIgnoredOnLoad());
        assertEquals(0, new UserRepository().getIgnoredOnLoad());
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
