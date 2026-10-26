package es.upm.parque.business.controller;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.model.User;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserControllerTest {

    @TempDir
    Path dir;

    private final PasswordHasher hasher = new PasswordHasher(4);
    private Path file;
    private UserController manager;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = UserController.getInstance().configure(new UserRepository(file), hasher);
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void createStoresAndPersistsUser() throws IOException {
        assertNotNull(manager.findByEmail("ana@correo.com"));
        UserController reloaded = UserController.getInstance().configure(new UserRepository(file), hasher);
        assertEquals(1, reloaded.getUsers().size());
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(BusinessException.class, () -> manager.create("P", "L", "12345678A", "p@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(BusinessException.class, () -> manager.create("P", "L", "87654321X", "p@correo.es", "Otra2#", "6", "9876543210987654"));
        assertThrows(BusinessException.class, () -> manager.create("P", "L", "87654321X", "p@correo.com", "otra2#", "6", "9876543210987654"));
        assertThrows(BusinessException.class, () -> manager.create("P", "L", "87654321X", "p@correo.com", "Otra2#", "6", "98765432109876"));
        assertThrows(BusinessException.class, () -> manager.create("", "L", "87654321X", "p@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniOrEmail() {
        assertThrows(BusinessException.class, () -> manager.create("P", "L", "12345678Z", "otro@correo.com", "Otra2#", "6", "9876543210987654"));
        assertThrows(BusinessException.class, () -> manager.create("P", "L", "87654321X", "ana@correo.com", "Otra2#", "6", "9876543210987654"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deleteRequiresMatchingPassword() throws IOException {
        assertThrows(BusinessException.class, () -> manager.authenticate("nadie@correo.com", "Clave1!"));
        assertThrows(BusinessException.class, () -> manager.authenticate("ana@correo.com", "Mala1!"));
        assertEquals(1, manager.getUsers().size());

        manager.delete(manager.authenticate("ana@correo.com", "Clave1!"));
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

        String[] stored = Files.readAllLines(file).get(0).split(";");
        assertEquals(List.of("Ana Maria", "Garcia", "12345678Z", "ana.nueva@correo.com", "699999999", "9999888877776666"),
                List.of(stored[0], stored[1], stored[2], stored[3], stored[5], stored[6]));
        assertTrue(hasher.matches("Nueva2#", stored[4]));
        assertFalse(hasher.matches("Clave1!", stored[4]));
    }

    @Test
    void passwordsAreStoredAsHashes() throws IOException {
        String stored = Files.readAllLines(file).get(0).split(";")[4];
        assertTrue(hasher.isHash(stored));
        assertFalse(Files.readString(file).contains("Clave1!"));
        assertTrue(hasher.matches("Clave1!", manager.findByEmail("ana@correo.com").getPassword()));
    }

    @Test
    void plaintextPasswordsAreHashedOnLoad() throws IOException {
        Files.writeString(file, "Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n");
        UserController reloaded = UserController.getInstance().configure(new UserRepository(file), hasher);
        assertTrue(hasher.isHash(Files.readAllLines(file).get(0).split(";")[4]));
        assertEquals("87654321X", reloaded.authenticate("pedro@correo.com", "Otra2#").getDni());
    }

    @Test
    void updateRejectsInvalidRequests() {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Mala1!", "NAME", "X"));
        assertThrows(BusinessException.class, () -> manager.update("nadie@correo.com", "Clave1!", "NAME", "X"));
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(BusinessException.class, () -> manager.update("ana@correo.com", "Clave1!", "NAME", ""));

        User ana = manager.findByDni("12345678Z");
        assertEquals("Ana", ana.getName());
        assertEquals("ana@correo.com", ana.getEmail());
        assertTrue(hasher.matches("Clave1!", ana.getPassword()));
        assertNull(manager.findByEmail("ana@correo"));
    }
}
