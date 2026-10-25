package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Park;
import es.upm.parque.Fixtures;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.TextFileUserRepository;

class UserManagerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = UserManager.initialize(new TextFileUserRepository(file), Park.HASHER);
    }

    @Test
    void createsAndPersistsValidUser() throws IOException {
        assertEquals(UserManager.USER_CREATED, manager.create(Fixtures.ana()));
        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        String[] fields = lines.get(0).split(";");
        assertEquals(List.of("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com"), List.of(fields).subList(0, 4));
        assertEquals(List.of("612345678", "1234567890123456"), List.of(fields).subList(5, 7));
        assertTrue(Park.HASHER.matches("Clave1!", fields[4]));
        assertFalse(lines.get(0).contains("Clave1!"));
    }

    @Test
    void rejectsInvalidData() {
        assertEquals(UserManager.EMPTY_FIELD, manager.create(new User(" ", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.EMPTY_FIELD, manager.create(new User("Ana", "", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.EMPTY_FIELD, manager.create(new User("Ana", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "", "1234567890123456")));
        assertEquals(UserManager.INVALID_DNI, manager.create(new User("Ana", "G", "12345678A", "ana@correo.com",
                "Clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.INVALID_EMAIL, manager.create(new User("Ana", "G", "12345678Z", "ana@correo.es",
                "Clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.INVALID_PASSWORD, manager.create(new User("Ana", "G", "12345678Z",
                "ana@correo.com", "clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.INVALID_CARD, manager.create(new User("Ana", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234 5678 9012 3456")));
        assertEquals(0, manager.count());
        assertEquals(false, Files.exists(file));
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        manager.create(Fixtures.ana());
        assertEquals(UserManager.DUPLICATE_DNI, manager.create(new User("Otra", "G", "12345678Z",
                "otro@correo.com", "Clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.DUPLICATE_EMAIL, manager.create(new User("Otro", "G", "87654321X",
                "ANA@correo.com", "Clave1!", "6", "1234567890123456")));
        assertEquals(1, manager.count());
    }

    @Test
    void deletesWithCorrectCredentialsOnly() throws IOException {
        manager.create(Fixtures.ana());
        assertEquals(UserManager.INVALID_CREDENTIALS, manager.delete("ana@correo.com", "Otra1!"));
        assertEquals(UserManager.INVALID_CREDENTIALS, manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, manager.count());
        assertEquals(UserManager.USER_DELETED, manager.delete("ana@correo.com", "Clave1!"));
        assertEquals(0, manager.count());
        assertEquals(0, UserManager.initialize(new TextFileUserRepository(file), Park.HASHER).count());
    }

    @Test
    void updatesEveryEditableField() throws IOException {
        manager.create(Fixtures.ana());
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria"));
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "SURNAME", "Ruiz"));
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana.nueva@correo.com", "Nueva2#", "card", "9999888877776666"));
        User reloaded = UserManager.initialize(new TextFileUserRepository(file), Park.HASHER).findByEmail("ana.nueva@correo.com");
        assertNotNull(reloaded);
        assertEquals("Ana Maria", reloaded.getName());
        assertEquals("Ruiz", reloaded.getSurname());
        assertEquals("699999999", reloaded.getPhone());
        assertTrue(Park.HASHER.matches("Nueva2#", reloaded.getPassword()));
        assertEquals("9999888877776666", reloaded.getCard());
        assertEquals("12345678Z", reloaded.getDni());
    }

    @Test
    void updateRequiresCredentialsAndValidValues() {
        manager.create(Fixtures.ana());
        manager.create(Fixtures.pedro());
        assertEquals(UserManager.INVALID_CREDENTIALS, manager.update("ana@correo.com", "Mala1!", "NAME", "X"));
        assertEquals(UserManager.INVALID_CREDENTIALS, manager.update("x@correo.com", "Clave1!", "NAME", "X"));
        assertEquals(UserManager.INVALID_FIELD, manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertEquals(UserManager.EMPTY_FIELD, manager.update("ana@correo.com", "Clave1!", "NAME", " "));
        assertEquals(UserManager.INVALID_PASSWORD, manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave"));
        assertEquals(UserManager.INVALID_EMAIL, manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertEquals(UserManager.INVALID_CARD, manager.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertEquals(UserManager.DUPLICATE_EMAIL,
                manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com"));
        User ana = manager.findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertTrue(Park.HASHER.matches("Clave1!", ana.getPassword()));
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void restartKeepsUsersForUniqueness() throws IOException {
        manager.create(Fixtures.ana());
        UserManager restarted = UserManager.initialize(new TextFileUserRepository(file), Park.HASHER);
        assertEquals(UserManager.DUPLICATE_DNI, restarted.create(Fixtures.ana()));
        assertNull(restarted.findByEmail("pedro@correo.com"));
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = dir.resolve("blocked");
        Files.createFile(blocked);
        UserManager broken = UserManager.initialize(new TextFileUserRepository(blocked.resolve("users.txt")), Park.HASHER);
        assertEquals(UserManager.STORAGE_FAILURE, broken.create(Fixtures.ana()));
    }

    @Test
    void findsUsersByDni() {
        manager.create(Fixtures.ana());
        assertEquals("ana@correo.com", manager.findByDni("12345678Z").getEmail());
        assertNull(manager.findByDni("87654321X"));
    }

    @Test
    void passwordChangeStoresNewHashAndOnlyNewPasswordWorks() throws IOException {
        manager.create(Fixtures.ana());
        String oldHash = manager.findByEmail("ana@correo.com").getPassword();
        assertEquals(UserManager.INVALID_PASSWORD, manager.update("ana@correo.com", "Clave1!", "PASSWORD", "debil"));
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#"));
        assertFalse(oldHash.equals(manager.findByEmail("ana@correo.com").getPassword()));
        UserManager restarted = UserManager.initialize(new TextFileUserRepository(file), Park.HASHER);
        assertEquals(UserManager.INVALID_CREDENTIALS, restarted.delete("ana@correo.com", "Clave1!"));
        assertEquals(UserManager.USER_DELETED, restarted.delete("ana@correo.com", "Nueva2#"));
    }

    @Test
    void plaintextPasswordsAreMigratedOnLoad() throws IOException {
        Files.write(file, List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        UserManager migrated = UserManager.initialize(new TextFileUserRepository(file), Park.HASHER);
        String stored = Files.readAllLines(file).get(0).split(";")[4];
        assertTrue(Park.HASHER.isHash(stored));
        assertEquals(UserManager.USER_DELETED, migrated.delete("ana@correo.com", "Clave1!"));
    }

    @Test
    void hashedFilesAreNotRewrittenOnLoad() throws IOException {
        manager.create(Fixtures.ana());
        List<String> before = Files.readAllLines(file);
        UserManager.initialize(new TextFileUserRepository(file), Park.HASHER);
        assertEquals(before, Files.readAllLines(file));
    }
}
