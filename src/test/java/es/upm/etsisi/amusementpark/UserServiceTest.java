package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserServiceTest {

    @TempDir
    Path dir;

    private Path file;
    private UserService service;

    @BeforeEach
    void setUp() throws UserException {
        file = dir.resolve("users.txt");
        service = newService();
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private UserService newService() throws UserException {
        return new UserService(new UserRepository(file, new PrintStream(new ByteArrayOutputStream())));
    }

    private User only() {
        List<User> users = service.getUsers();
        assertEquals(1, users.size());
        return users.get(0);
    }

    @Test
    void createsAndPersistsUser() throws UserException {
        assertEquals("Ana", only().getFirstName());
        assertEquals(1, newService().getUsers().size());
    }

    @Test
    void rejectsDuplicateDni() throws IOException {
        String before = Files.readString(file);
        assertThrows(UserException.class, () -> service.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, service.getUsers().size());
        assertEquals(before, Files.readString(file));
    }

    @Test
    void rejectsDuplicateEmail() {
        assertThrows(UserException.class, () -> service.create("Pedro", "Lopez Martin", "87654321X",
                "ana@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void rejectsInvalidCreationData() {
        assertThrows(UserException.class, () -> service.create("P", "L", "87654321A", "p@correo.com",
                "Otra2#", "1", "9876543210987654"));
        assertThrows(UserException.class, () -> service.create("P", "L", "87654321X", "p@correo.es",
                "Otra2#", "1", "9876543210987654"));
        assertThrows(UserException.class, () -> service.create("P", "L", "87654321X", "p@correo.com",
                "otra2#", "1", "9876543210987654"));
        assertThrows(UserException.class, () -> service.create("P", "L", "87654321X", "p@correo.com",
                "Otra2#", "1", "98765432109876"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void deletesWithCorrectCredentials() throws UserException {
        service.delete("ana@correo.com", "Clave1!");
        assertEquals(0, service.getUsers().size());
        assertEquals(0, newService().getUsers().size());
    }

    @Test
    void deleteRejectsWrongPasswordOrUnknownEmail() throws IOException {
        String before = Files.readString(file);
        assertThrows(UserException.class, () -> service.delete("ana@correo.com", "Wrong1!"));
        assertThrows(UserException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, service.getUsers().size());
        assertEquals(before, Files.readString(file));
    }

    @Test
    void updatesEveryModifiableField() throws UserException {
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");

        User reloaded = newService().getUsers().get(0);
        assertEquals("Ana Maria", reloaded.getFirstName());
        assertEquals("Garcia", reloaded.getSurnames());
        assertEquals("699999999", reloaded.getPhone());
        assertEquals("9999888877776666", reloaded.getCard());
        assertEquals("Nueva2#", reloaded.getPassword());
        assertEquals("ana.nueva@correo.com", reloaded.getEmail());
        assertEquals("12345678Z", reloaded.getDni());
    }

    @Test
    void updateAllowsKeepingOwnEmail() throws UserException {
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertEquals("ana@correo.com", only().getEmail());
    }

    @Test
    void updateRejectionsLeaveUserUnchanged() throws UserException, IOException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        String before = Files.readString(file);

        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "AGE", "30"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@x.es"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Bad1!", "NAME", "X"));
        assertThrows(UserException.class, () -> service.update("nadie@correo.com", "Clave1!", "NAME", "X"));

        assertEquals(before, Files.readString(file));
        assertEquals("Ana", service.getUsers().get(0).getFirstName());
    }

    @Test
    void failedSaveLeavesMemoryUnchanged() throws UserException {
        Path unwritable = dir.resolve("missing-dir").resolve("users.txt");
        UserService broken = new UserService(new UserRepository(unwritable, new PrintStream(new ByteArrayOutputStream())));
        assertThrows(UserException.class, () -> broken.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456"));
        assertEquals(0, broken.getUsers().size());
    }

    @Test
    void getUsersReturnsCopies() {
        service.getUsers().get(0).setFirstName("Changed");
        assertEquals("Ana", only().getFirstName());
    }
}
