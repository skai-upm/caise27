package es.upm.park.service;

import es.upm.park.model.User;
import es.upm.park.persistence.UserFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    @TempDir
    Path dir;

    private Path file;
    private UserService service;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        service = new UserService(new UserFileRepository(file));
    }

    private void createAna() throws OperationException {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() throws OperationException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserService reload() {
        return new UserService(new UserFileRepository(file));
    }

    @Test
    void createsAndPersistsUser() throws Exception {
        createAna();
        assertEquals(1, service.getUsers().size());
        assertNotNull(reload().findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(OperationException.class, () -> service.create("Ana", "Garcia", "12345678A",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.create("Ana", "Garcia", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "123"));
        assertThrows(OperationException.class, () -> service.create("", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", " ", "1234567890123456"));
        assertTrue(service.getUsers().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void rejectsDuplicateDni() throws Exception {
        createAna();
        OperationException e = assertThrows(OperationException.class, () -> service.create("Otra",
                "Persona", "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws Exception {
        createAna();
        assertThrows(OperationException.class, () -> service.create("Otra", "Persona", "87654321X",
                "ANA@correo.com", "Clave1!", "600000000", "1234567890123456"));
    }

    @Test
    void removesAuthenticatedUser() throws Exception {
        createAna();
        createPedro();
        service.remove(service.authenticate("pedro@correo.com", "Otra2#"));
        assertNull(service.findByEmail("pedro@correo.com"));
        assertNull(reload().findByEmail("pedro@correo.com"));
        assertNotNull(reload().findByEmail("ana@correo.com"));
    }

    @Test
    void wrongPasswordFailsAuthentication() throws Exception {
        createAna();
        assertThrows(OperationException.class, () -> service.authenticate("ana@correo.com", "Wrong1!"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void unknownUserFailsAuthentication() {
        assertThrows(OperationException.class, () -> service.authenticate("nobody@correo.com", "Clave1!"));
    }

    @Test
    void updatesEveryModifiableField() throws Exception {
        createAna();
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");

        User stored = reload().findByEmail("ana.nueva@correo.com");
        assertNotNull(stored);
        assertEquals("Ana Maria", stored.getFirstName());
        assertEquals("Garcia", stored.getSurnames());
        assertEquals("699999999", stored.getPhone());
        assertEquals("9999888877776666", stored.getCard());
        assertEquals("Nueva2#", stored.getPassword());
        assertEquals("12345678Z", stored.getDni());
    }

    @Test
    void updatingEmailToOwnEmailIsAllowed() throws Exception {
        createAna();
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsEmailInUseByAnotherUser() throws Exception {
        createAna();
        createPedro();
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsInvalidNewValues() throws Exception {
        createAna();
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "weak"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "NAME", ""));
        User ana = service.findByEmail("ana@correo.com");
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Clave1!", ana.getPassword());
    }

    @Test
    void rejectsWrongCredentialsOnUpdate() throws Exception {
        createAna();
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Wrong1!", "PHONE", "699999999"));
        assertEquals("612345678", service.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void dniCannotBeUpdated() throws Exception {
        createAna();
        OperationException e = assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertTrue(e.getMessage().contains("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void loadsExistingUsersOnStartup() throws IOException {
        Files.writeString(file, "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        assertNotNull(reload().findByDni("12345678Z"));
    }
}
