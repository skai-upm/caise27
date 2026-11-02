package es.upm.etsisi.amusementpark.business.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.persistence.FileUserRepository;

class UserManagerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    private static User ana() {
        return new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    private static User pedro() {
        return new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
                "Otra2#", "623456789", "9876543210987654");
    }

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = new UserManager(new FileUserRepository(file));
    }

    private UserManager restart() throws IOException {
        return new UserManager(new FileUserRepository(file));
    }

    private List<String> storedLines() throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    @Test
    void createsValidUniqueUser() throws Exception {
        manager.create(ana());
        assertEquals(1, manager.getUsers().size());
        assertEquals(1, storedLines().size());
    }

    @Test
    void rejectsInvalidData() {
        User badDni = new User("Ana", "G", "12345678A", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User badEmail = new User("Ana", "G", "12345678Z", "ana@correo.es", "Clave1!", "6", "1234567890123456");
        User badPassword = new User("Ana", "G", "12345678Z", "ana@correo.com", "clave1!", "6", "1234567890123456");
        User badCard = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "123456789012345");
        for (User u : List.of(badDni, badEmail, badPassword, badCard)) {
            assertThrows(ParkException.class, () -> manager.create(u));
        }
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void rejectsDuplicateDniAndEmail() throws Exception {
        manager.create(ana());
        User sameDni = new User("X", "Y", "12345678Z", "otra@correo.com", "Clave1!", "6", "1234567890123456");
        User sameEmail = new User("X", "Y", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertThrows(ParkException.class, () -> manager.create(sameDni));
        assertThrows(ParkException.class, () -> manager.create(sameEmail));
        assertEquals(1, manager.getUsers().size());
        assertEquals(1, storedLines().size());
    }

    @Test
    void usersSurviveRestart() throws Exception {
        manager.create(ana());
        User reloaded = restart().findByEmail("ana@correo.com");
        assertNotNull(reloaded);
        assertEquals("12345678Z", reloaded.getDni());
        assertEquals("612345678", reloaded.getPhone());
    }

    @Test
    void failedWriteLeavesStateUnchanged() throws Exception {
        FileUserRepository failing = new FileUserRepository(file) {
            @Override
            public void save(List<User> users) throws IOException {
                throw new IOException("disk full");
            }
        };
        UserManager broken = new UserManager(failing);
        assertThrows(ParkException.class, () -> broken.create(ana()));
        assertTrue(broken.getUsers().isEmpty());
    }

    @Test
    void deletesWithCorrectCredentials() throws Exception {
        manager.create(ana());
        manager.create(pedro());
        manager.remove(manager.authenticate("pedro@correo.com", "Otra2#"));
        assertNull(manager.findByEmail("pedro@correo.com"));
        assertNull(restart().findByEmail("pedro@correo.com"));
        assertEquals(1, storedLines().size());
    }

    @Test
    void deletionRequiresValidIdentification() throws Exception {
        manager.create(ana());
        List<String> before = storedLines();
        assertThrows(ParkException.class, () -> manager.authenticate("ana@correo.com", "Wrong1!"));
        assertThrows(ParkException.class, () -> manager.authenticate("nobody@correo.com", "Clave1!"));
        assertEquals(1, manager.getUsers().size());
        assertEquals(before, storedLines());
    }

    @Test
    void updatesEveryModifiableField() throws Exception {
        manager.create(ana());
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        manager.update("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana@correo.com", "Nueva2#", UserField.EMAIL, "ana.nueva@correo.com");

        User u = restart().findByEmail("ana.nueva@correo.com");
        assertNotNull(u);
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("Garcia", u.getSurnames());
        assertEquals("699999999", u.getPhone());
        assertEquals("9999888877776666", u.getCard());
        assertTrue(u.hasPassword("Nueva2#"));
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void rejectedUpdatesLeaveUserUnchanged() throws Exception {
        manager.create(ana());
        manager.create(pedro());
        List<String> before = storedLines();
        assertThrows(ParkException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(ParkException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(ParkException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(ParkException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "1234 5678 9012 3456"));
        assertThrows(ParkException.class,
                () -> manager.update("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        assertThrows(ParkException.class,
                () -> manager.update("nobody@correo.com", "Clave1!", UserField.NAME, "X"));
        assertEquals(before, storedLines());
        assertEquals("Ana", manager.findByEmail("ana@correo.com").getFirstName());
    }

    @Test
    void keepingOwnEmailIsAllowed() throws Exception {
        manager.create(ana());
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void updateKeepsTheSameUserObject() throws Exception {
        manager.create(ana());
        User before = manager.findByEmail("ana@correo.com");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertTrue(before == manager.findByEmail("ana.nueva@correo.com"));
        assertEquals("ana.nueva@correo.com", before.getEmail());
    }

    @Test
    void failedUpdateSaveLeavesUserUnchanged() throws Exception {
        manager.create(ana());
        FileUserRepository failing = new FileUserRepository(file) {
            @Override
            public void save(List<User> users) throws IOException {
                throw new IOException("disk full");
            }
        };
        UserManager broken = new UserManager(failing);
        assertThrows(ParkException.class,
                () -> broken.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria"));
        assertEquals("Ana", broken.findByEmail("ana@correo.com").getFirstName());
    }
}
