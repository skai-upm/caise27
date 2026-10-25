package es.upm.etsisi.park.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.UserFileRepository;
import java.io.IOException;
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
    void setUp() {
        file = dir.resolve("users.txt");
        service = new UserService(new UserFileRepository(file));
    }

    private void createAna() {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void createsAndPersistsUser() throws IOException {
        createAna();
        assertTrue(service.findByEmail("ana@correo.com").isPresent());
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));
    }

    @Test
    void rejectsInvalidDataOnCreate() {
        UserException e = assertThrows(UserException.class, () -> service.create("Ana", "Garcia Lopez",
                "12345678A", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "123456789012345"));
        assertThrows(UserException.class, () -> service.create(" ", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(service.findAll().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void rejectsDuplicateDni() {
        createAna();
        UserException e = assertThrows(UserException.class, () -> service.create("Otra", "Persona",
                "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void rejectsDuplicateEmail() {
        createAna();
        UserException e = assertThrows(UserException.class, () -> service.create("Otra", "Persona",
                "87654321X", "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(e.getMessage().contains("email"));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void deletesUserWithCorrectPassword() throws IOException {
        createAna();
        createPedro();
        service.delete("pedro@correo.com", "Otra2#");
        assertFalse(service.findByEmail("pedro@correo.com").isPresent());
        assertFalse(String.join("\n", Files.readAllLines(file)).contains("pedro@correo.com"));
    }

    @Test
    void deleteFailsForUnknownUserOrWrongPassword() {
        createAna();
        UserException unknown = assertThrows(UserException.class,
                () -> service.delete("nadie@correo.com", "Clave1!"));
        assertTrue(unknown.getMessage().contains("does not exist"));
        assertThrows(UserException.class, () -> service.delete("ana@correo.com", "Mala1!"));
        assertTrue(service.findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void updatesEveryModifiableField() {
        createAna();
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");
        assertFalse(service.findByEmail("ana@correo.com").isPresent());
        User ana = service.findByEmail("ana.nueva@correo.com").orElseThrow();
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("Nueva2#", ana.getPassword());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updateIsPersisted() {
        createAna();
        service.update("ana@correo.com", "Clave1!", "phone", "699999999");
        UserService reloaded = new UserService(new UserFileRepository(file));
        assertEquals("699999999", reloaded.findByEmail("ana@correo.com").orElseThrow().getPhone());
    }

    @Test
    void dniCannotBeUpdated() {
        createAna();
        UserException e = assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertTrue(e.getMessage().contains("not modifiable"));
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").orElseThrow().getDni());
    }

    @Test
    void updateRequiresCorrectPassword() {
        createAna();
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Mala1!", "PHONE", "1"));
        assertEquals("612345678", service.findByEmail("ana@correo.com").orElseThrow().getPhone());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() {
        createAna();
        createPedro();
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertTrue(service.findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void updateToOwnEmailIsAllowed() {
        createAna();
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertTrue(service.findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void updateRejectsInvalidValues() {
        createAna();
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "NAME", " "));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", "PHONE", ""));
        User ana = service.findByEmail("ana@correo.com").orElseThrow();
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Ana", ana.getFirstName());
    }

    @Test
    void loadsExistingUsersAtStartup() {
        createAna();
        UserService reloaded = new UserService(new UserFileRepository(file));
        reloaded.delete("ana@correo.com", "Clave1!");
        assertTrue(reloaded.findAll().isEmpty());
    }
}
