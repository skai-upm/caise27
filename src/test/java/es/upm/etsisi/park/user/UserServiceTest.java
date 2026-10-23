package es.upm.etsisi.park.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.persistence.UserRepository;

class UserServiceTest {

    @TempDir
    Path dir;

    private Path file;
    private UserService service;

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        service = newService();
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserService newService() throws Exception {
        return new UserService(new UserRepository(file, new PrintStream(new ByteArrayOutputStream())));
    }

    @Test
    void createdUsersArePersisted() throws Exception {
        assertEquals(2, newService().getUsers().size());
    }

    @Test
    void rejectsDuplicateDni() {
        UserException e = assertThrows(UserException.class, () -> service.create("Eva", "Ruiz", "12345678z",
                "eva@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertTrue(e.getMessage().contains("DNI"));
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        assertThrows(UserException.class, () -> service.create("Eva", "Ruiz", "00000000T",
                "ANA@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(UserException.class, () -> service.create("", "Ruiz", "00000000T",
                "eva@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(UserException.class, () -> service.create("Eva", "Ruiz", "00000000A",
                "eva@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(UserException.class, () -> service.create("Eva", "Ruiz", "00000000T",
                "eva@correo.es", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(UserException.class, () -> service.create("Eva", "Ruiz", "00000000T",
                "eva@correo.com", "clave1!", "600000000", "1111222233334444"));
        assertThrows(UserException.class, () -> service.create("Eva", "Ruiz", "00000000T",
                "eva@correo.com", "Clave1!", "600000000", "1111 2222 3333 4444"));
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void deletesWithCorrectPassword() throws Exception {
        service.delete("pedro@correo.com", "Otra2#");
        assertNull(service.findByEmail("pedro@correo.com"));
        assertNull(newService().findByEmail("pedro@correo.com"));
    }

    @Test
    void deleteRejectsWrongPasswordAndUnknownUser() {
        UserException wrong = assertThrows(UserException.class, () -> service.delete("ana@correo.com", "Wrong1!"));
        assertTrue(wrong.getMessage().contains("Incorrect password"));
        UserException unknown = assertThrows(UserException.class,
                () -> service.delete("nobody@correo.com", "Clave1!"));
        assertTrue(unknown.getMessage().contains("not found"));
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void updatesFieldsAndPersists() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        service.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        service.update("ana.nueva@correo.com", "Nueva2#", UserField.SURNAME, "Garcia");

        User ana = newService().findByEmail("ana.nueva@correo.com");
        assertNotNull(ana);
        assertEquals("699999999", ana.getPhone());
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
        assertTrue(ana.checkPassword("Nueva2#"));
    }

    @Test
    void updateRejectsInvalidOrTakenValues() {
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "123"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Bad1!", UserField.PHONE, "600000000"));
        User ana = service.findByEmail("ana@correo.com");
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("612345678", ana.getPhone());
    }

    @Test
    void updateKeepingOwnEmailIsAllowed() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void rollsBackWhenSaveFails() throws Exception {
        Files.delete(file);
        Files.createDirectory(file);
        Files.createFile(file.resolve("blocker"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        assertEquals("612345678", service.findByEmail("ana@correo.com").getPhone());
        assertThrows(UserException.class, () -> service.delete("ana@correo.com", "Clave1!"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }
}
