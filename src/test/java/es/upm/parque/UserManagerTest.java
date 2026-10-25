package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserManagerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void createStoresUser() throws IOException {
        assertEquals(1, manager.getUsers().size());
        assertTrue(Files.readString(file).contains("12345678Z;ana@correo.com"));
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> manager.create("Pedro", "Lopez", "87654321A",
                "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.es", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.com", "otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.com", "Otra2#", "623456789", "987654321098765"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("", "Lopez", "87654321X",
                "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.com", "Otra2#", " ", "9876543210987654"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniOrEmail() {
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otra", "Persona", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otra", "Persona", "87654321X",
                "ANA@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deleteRequiresExistingEmailAndCorrectPassword() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> manager.authenticate("nadie@correo.com", "Clave1!"));
        assertThrows(IllegalArgumentException.class, () -> manager.authenticate("ana@correo.com", "Mala1!"));
        assertEquals(1, manager.getUsers().size());

        manager.remove(manager.authenticate("ana@correo.com", "Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
        assertEquals("", Files.readString(file));
    }

    @Test
    void updatesEveryEditableField() {
        manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666");

        User u = manager.findByEmail("ana.nueva@correo.com");
        assertNotNull(u);
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia", u.getSurname());
        assertEquals("699999999", u.getPhone());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCard());
        assertEquals("12345678Z", u.getDni());
        assertNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void updateRejectsWrongCredentialsAndInvalidValues() {
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Mala1!", "NAME", "X"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("nadie@correo.com", "Clave1!", "NAME", "X"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "CARD", "123456789012345"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", "NAME", ""));
        User ana = manager.findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updatePersistsNewState() throws IOException {
        manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        assertTrue(Files.readString(file).contains(";699999999;"));
    }

    @Test
    void usersAreRestoredByANewManager() {
        UserManager restarted = new UserManager(new UserRepository(file));
        assertNotNull(restarted.findByDni("12345678Z"));
        restarted.remove(restarted.authenticate("ana@correo.com", "Clave1!"));
        assertTrue(new UserManager(new UserRepository(file)).getUsers().isEmpty());
    }
}
