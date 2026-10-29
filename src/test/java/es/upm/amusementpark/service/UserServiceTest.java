package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.persistence.UserRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    }

    private UserService newService() throws IOException {
        return new UserService(new UserRepository(file, new PrintStream(OutputStream.nullOutputStream())));
    }

    private void createPedro() throws UserOperationException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void createStoresAndPersistsUser() throws Exception {
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertTrue(Files.readString(file).contains("12345678Z"));
        assertNotNull(newService().findByEmail("ana@correo.com"));
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(UserOperationException.class, () -> service.create("P", "L", "87654321A",
                "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(UserOperationException.class, () -> service.create("P", "L", "87654321X",
                "pedro@correo.es", "Otra2#", "6", "9876543210987654"));
        assertThrows(UserOperationException.class, () -> service.create("P", "L", "87654321X",
                "pedro@correo.com", "otra2#", "6", "9876543210987654"));
        assertThrows(UserOperationException.class, () -> service.create("P", "L", "87654321X",
                "pedro@correo.com", "Otra2#", "6", "98765432109876"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDni() {
        assertThrows(UserOperationException.class, () -> service.create("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Otra2#", "6", "9876543210987654"));
        assertNull(service.findByEmail("otra@correo.com"));
    }

    @Test
    void createRejectsDuplicateEmail() {
        assertThrows(UserOperationException.class, () -> service.create("Otra", "Persona", "87654321X",
                "ana@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void deleteRemovesUserAndPersists() throws Exception {
        createPedro();
        service.delete("pedro@correo.com", "Otra2#");
        assertNull(service.findByEmail("pedro@correo.com"));
        assertNull(newService().findByEmail("pedro@correo.com"));
        assertNotNull(newService().findByEmail("ana@correo.com"));
    }

    @Test
    void deleteRejectsUnknownEmail() {
        assertThrows(UserOperationException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void deleteRejectsWrongPassword() {
        assertThrows(UserOperationException.class, () -> service.delete("ana@correo.com", "Mala1!"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void updateEachSimpleField() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");

        User reloaded = newService().findByEmail("ana@correo.com");
        assertEquals("Ana Maria", reloaded.getFirstName());
        assertEquals("Garcia", reloaded.getSurnames());
        assertEquals("699999999", reloaded.getPhone());
        assertEquals("9999888877776666", reloaded.getCard());
        assertEquals("Nueva2#", reloaded.getPassword());
        assertEquals("12345678Z", reloaded.getDni());
    }

    @Test
    void updateEmailRekeysUser() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertNull(service.findByEmail("ana@correo.com"));
        assertNotNull(service.findByEmail("ana.nueva@correo.com"));
        assertNotNull(newService().findByEmail("ana.nueva@correo.com"));
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void updateRejectsWrongCredentials() {
        assertThrows(UserOperationException.class,
                () -> service.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertThrows(UserOperationException.class,
                () -> service.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertEquals("Ana", service.findByEmail("ana@correo.com").getFirstName());
    }

    @Test
    void updateRejectsInvalidValues() {
        assertThrows(UserOperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(UserOperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave"));
        assertThrows(UserOperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        User ana = service.findByEmail("ana@correo.com");
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() throws Exception {
        createPedro();
        assertThrows(UserOperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertEquals("Pedro", service.findByEmail("pedro@correo.com").getFirstName());
    }
}
