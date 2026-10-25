package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserManagerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    /**
     * A valid user number n (n &lt; 1000), with email u&lt;n&gt;@correo.com and password Clave1!.
     */
    static User numbered(int n) {
        int number = 10_000_000 + n;
        String dni = number + String.valueOf("TRWAGMYFPDXBNJZSQVHLCKE".charAt(number % 23));
        return new User("U" + n, "Apellido", dni, "u" + n + "@correo.com", "Clave1!", "600000000",
                "1234567890123456");
    }

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
    }

    @Test
    void createsAndPersistsValidUser() throws IOException {
        assertEquals(UserManager.USER_CREATED, manager.create(ana()));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));
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
        manager.create(ana());
        assertEquals(UserManager.DUPLICATE_DNI, manager.create(new User("Otra", "G", "12345678Z",
                "otro@correo.com", "Clave1!", "6", "1234567890123456")));
        assertEquals(UserManager.DUPLICATE_EMAIL, manager.create(new User("Otro", "G", "87654321X",
                "ANA@correo.com", "Clave1!", "6", "1234567890123456")));
        assertEquals(1, manager.count());
    }

    @Test
    void deletesWithCorrectCredentialsOnly() throws IOException {
        manager.create(ana());
        assertEquals(UserManager.INVALID_CREDENTIALS, manager.delete("ana@correo.com", "Otra1!"));
        assertEquals(UserManager.INVALID_CREDENTIALS, manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, manager.count());
        assertEquals(UserManager.USER_DELETED, manager.delete("ana@correo.com", "Clave1!"));
        assertEquals(0, manager.count());
        assertEquals(0, new UserManager(new UserRepository(file)).count());
    }

    @Test
    void updatesEveryEditableField() throws IOException {
        manager.create(ana());
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria"));
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "SURNAME", "Ruiz"));
        assertEquals(UserManager.USER_UPDATED, manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#"));
        assertEquals(UserManager.USER_UPDATED,
                manager.update("ana.nueva@correo.com", "Nueva2#", "card", "9999888877776666"));
        User reloaded = new UserManager(new UserRepository(file)).findByEmail("ana.nueva@correo.com");
        assertNotNull(reloaded);
        assertEquals("Ana Maria", reloaded.getName());
        assertEquals("Ruiz", reloaded.getSurname());
        assertEquals("699999999", reloaded.getPhone());
        assertEquals("Nueva2#", reloaded.getPassword());
        assertEquals("9999888877776666", reloaded.getCard());
        assertEquals("12345678Z", reloaded.getDni());
    }

    @Test
    void updateRequiresCredentialsAndValidValues() {
        manager.create(ana());
        manager.create(pedro());
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
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void restartKeepsUsersForUniqueness() throws IOException {
        manager.create(ana());
        UserManager restarted = new UserManager(new UserRepository(file));
        assertEquals(UserManager.DUPLICATE_DNI, restarted.create(ana()));
        assertNull(restarted.findByEmail("pedro@correo.com"));
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = dir.resolve("blocked");
        Files.createFile(blocked);
        UserManager broken = new UserManager(new UserRepository(blocked.resolve("users.txt")));
        assertEquals(UserManager.STORAGE_FAILURE, broken.create(ana()));
    }

    @Test
    void findsUsersByDni() {
        manager.create(ana());
        assertEquals("ana@correo.com", manager.findByDni("12345678Z").getEmail());
        assertNull(manager.findByDni("87654321X"));
    }
}
