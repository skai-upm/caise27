package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.persistence.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserControllerTest {

    @TempDir
    Path dir;
    private Path file;
    private UserController manager;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        manager = new UserController(new UserRepository(file));
    }

    private void createAna() {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void createsAndPersistsUser() throws Exception {
        createAna();
        assertEquals(1, manager.getUsers().size());
        assertTrue(Files.readString(file).contains("Ana;Garcia Lopez;12345678Z;ana@correo.com"));
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "G", "12345678A", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "G", "12345678Z", "ana@correo.es",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "G", "12345678Z", "ana@correo.com",
                "clave1!", "6", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "123456789012345"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "G", "12345678Z", "ana@correo.com",
                "Clave1!", " ", "1234567890123456"));
        assertEquals(0, manager.getUsers().size());
        assertTrue(Files.notExists(file));
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        createAna();
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otro", "X", "12345678Z",
                "otro@correo.com", "Clave1!", "6", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otro", "X", "87654321X",
                "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otro", "X", "87654321X",
                "ANA@correo.com", "Clave1!", "6", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deletesWithCorrectCredentialsOnly() throws Exception {
        createAna();
        assertThrows(IllegalArgumentException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertThrows(IllegalArgumentException.class, () -> manager.delete("ana@correo.com", "Mala1!"));
        assertEquals(1, manager.getUsers().size());
        manager.delete("ana@correo.com", "Clave1!");
        assertEquals(0, manager.getUsers().size());
        assertEquals("", Files.readString(file));
    }

    @Test
    void updatesEveryEditableField() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        User u = manager.findByDni("12345678Z");
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia", u.getSurname());
        assertEquals("699999999", u.getPhone());
        assertEquals("ana.nueva@correo.com", u.getEmail());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCard());
    }

    @Test
    void updateChecksCredentialsAndRevalidates() {
        createAna();
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        User ana = manager.findByDni("12345678Z");
        assertEquals("Ana", ana.getName());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
    }

    @Test
    void usersSurviveRestart() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        UserController restarted = new UserController(new UserRepository(file));
        User u = restarted.findByEmail("ana@correo.com");
        assertNotNull(u);
        assertEquals("699999999", u.getPhone());
        restarted.delete("ana@correo.com", "Clave1!");
        assertNull(new UserController(new UserRepository(file)).findByEmail("ana@correo.com"));
    }
}
