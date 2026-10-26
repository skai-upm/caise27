package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserField;
import es.upm.parque.persistence.UserStorage;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserManagerTest {

    @TempDir
    Path dir;

    private UserStorage storage;
    private UserManager manager;

    @BeforeEach
    void setUp() {
        storage = new UserStorage(dir.resolve("users.txt"));
        manager = UserManager.getInstance().configure(storage);
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
    }

    @Test
    void createPersistsUser() {
        assertEquals(1, storage.load().size());
    }

    @Test
    void invalidDataIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> manager.create("Luis", "Ruiz", "12345678A",
            "luis@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Luis", "Ruiz", "87654321X",
            "luis@correo.es", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Luis", "Ruiz", "87654321X",
            "luis@correo.com", "clave1!", "600000000", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Luis", "Ruiz", "87654321X",
            "luis@correo.com", "Clave1!", "600000000", "123456789012345"));
        assertThrows(IllegalArgumentException.class, () -> manager.create(" ", "Ruiz", "87654321X",
            "luis@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void duplicateDniOrEmailIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otro", "X", "12345678Z",
            "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otro", "X", "87654321X",
            "ANA@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deleteRequiresMatchingCredentials() {
        assertThrows(IllegalArgumentException.class, () -> manager.delete("ana@correo.com", "Wrong1!"));
        assertThrows(IllegalArgumentException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(storage.load().isEmpty());
    }

    @Test
    void updateChangesFieldsAndPersists() {
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.SURNAME, "Garcia");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.PHONE, "699999999");

        User reloaded = UserManager.getInstance().configure(storage).getUsers().iterator().next();
        assertEquals("Ana Maria", reloaded.getName());
        assertEquals("Garcia", reloaded.getSurname());
        assertEquals("12345678Z", reloaded.getDni());
        assertEquals("ana.nueva@correo.com", reloaded.getEmail());
        assertEquals("Nueva2#", reloaded.getPassword());
        assertEquals("699999999", reloaded.getPhone());
        assertEquals("9999888877776666", reloaded.getCard());
    }

    @Test
    void updateRevalidatesAndChecksCredentials() {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        assertThrows(IllegalArgumentException.class,
            () -> manager.update("ana@correo.com", "Bad1!", UserField.NAME, "Otra"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Clave12"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "1234 5678 9012 3456"));
        User ana = UserManager.getInstance().configure(storage).getUsers().iterator().next();
        assertEquals("Ana", ana.getName());
        assertEquals("ana@correo.com", ana.getEmail());
    }
}
