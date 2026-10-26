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
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void createStoresAndPersistsUser() throws IOException {
        assertNotNull(manager.findByEmail("ana@correo.com"));
        UserManager reloaded = new UserManager(new UserRepository(file));
        assertEquals(1, reloaded.getUsers().size());
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(UserException.class, () -> manager.create("P", "L", "12345678A", "p@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(UserException.class, () -> manager.create("P", "L", "87654321X", "p@correo.es", "Otra2#", "6", "9876543210987654"));
        assertThrows(UserException.class, () -> manager.create("P", "L", "87654321X", "p@correo.com", "otra2#", "6", "9876543210987654"));
        assertThrows(UserException.class, () -> manager.create("P", "L", "87654321X", "p@correo.com", "Otra2#", "6", "98765432109876"));
        assertThrows(UserException.class, () -> manager.create("", "L", "87654321X", "p@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniOrEmail() {
        assertThrows(UserException.class, () -> manager.create("P", "L", "12345678Z", "otro@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(UserException.class, () -> manager.create("P", "L", "87654321X", "ana@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deleteRequiresMatchingPassword() throws IOException {
        assertThrows(UserException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertThrows(UserException.class, () -> manager.delete("ana@correo.com", "Mala1!"));
        assertEquals(1, manager.getUsers().size());

        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.readAllLines(file).isEmpty());
    }

    @Test
    void updateChangesEachField() throws IOException {
        manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666");

        assertEquals(java.util.List.of("Ana Maria;Garcia;12345678Z;ana.nueva@correo.com;Nueva2#;699999999;9999888877776666"),
                Files.readAllLines(file));
    }

    @Test
    void updateRejectsInvalidRequests() {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Mala1!", "NAME", "X"));
        assertThrows(UserException.class, () -> manager.update("nadie@correo.com", "Clave1!", "NAME", "X"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "NAME", ""));

        User ana = manager.findByDni("12345678Z");
        assertEquals("Ana", ana.getName());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertNull(manager.findByEmail("ana@correo"));
    }
}
