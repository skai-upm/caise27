package es.upm.parque.business.controller;

import es.upm.parque.business.model.User;
import es.upm.parque.business.security.PasswordHasher;
import es.upm.parque.persistence.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static UserController configured(Path file) throws Exception {
        UserController controller = UserController.getInstance();
        controller.configure(new UserRepository(file), new PasswordHasher(4));
        return controller;
    }

    @Test
    void singleInstance() {
        assertSame(UserController.getInstance(), UserController.getInstance());
    }

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        manager = configured(file);
        assertEquals("OK USER CREATED", manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456"));
    }

    @Test
    void createPersistsUser() throws Exception {
        assertTrue(Files.readString(file).contains("12345678Z;ana@correo.com"));
        assertEquals(1, configured(file).getUsers().size());
    }

    @Test
    void passwordsAreStoredAsHashes() throws Exception {
        String content = Files.readString(file);
        assertFalse(content.contains("Clave1!"));
        String hash = content.trim().split(";")[4];
        assertTrue(PasswordHasher.isHash(hash), hash);
        assertNotNull(configured(file).authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void plaintextPasswordsAreMigratedOnLoad() throws Exception {
        Path legacy = dir.resolve("legacy.txt");
        Files.writeString(legacy, "Ana;Garcia;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        UserController c = configured(legacy);
        assertFalse(Files.readString(legacy).contains("Clave1!"));
        assertNotNull(c.authenticate("ana@correo.com", "Clave1!"));
    }

    @Test
    void createRejectsInvalidData() throws Exception {
        assertTrue(manager.create("P", "L", "12345678A", "p@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(manager.create("P", "L", "87654321X", "p@correo.es", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(manager.create("P", "L", "87654321X", "p@correo.com", "clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(manager.create("P", "L", "87654321X", "p@correo.com", "Clave1!", "6", "123").startsWith("ERROR"));
        assertTrue(manager.create("", "L", "87654321X", "p@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void createRejectsDuplicates() throws Exception {
        assertTrue(manager.create("P", "L", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(manager.create("P", "L", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deleteRequiresCorrectCredentials() throws Exception {
        assertTrue(manager.delete("ana@correo.com", "Wrong1!").startsWith("ERROR"));
        assertTrue(manager.delete("nadie@correo.com", "Clave1!").startsWith("ERROR"));
        assertEquals(1, manager.getUsers().size());
        assertEquals("OK USER DELETED", manager.delete("ana@correo.com", "Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
        assertEquals("", Files.readString(file));
    }

    @Test
    void updateEachField() throws Exception {
        assertEquals("OK USER UPDATED", manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria"));
        assertEquals("OK USER UPDATED", manager.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia"));
        assertEquals("OK USER UPDATED", manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999"));
        assertEquals("OK USER UPDATED", manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com"));
        assertEquals("OK USER UPDATED", manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#"));
        assertEquals("OK USER UPDATED", manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666"));
        User u = configured(file).findByEmail("ana.nueva@correo.com");
        assertNotNull(u);
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia", u.getSurname());
        assertEquals("699999999", u.getPhone());
        assertNotNull(u.getPassword());
        assertNotNull(configured(file).authenticate("ana.nueva@correo.com", "Nueva2#"));
        assertNull(configured(file).authenticate("ana.nueva@correo.com", "Clave1!"));
        assertEquals("9999888877776666", u.getCard());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void updateRejections() throws Exception {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        assertTrue(manager.update("ana@correo.com", "Bad1!x", "NAME", "X").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!").startsWith("ERROR"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "CARD", "1234").startsWith("ERROR"));
        User ana = manager.findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertNotNull(manager.authenticate("ana@correo.com", "Clave1!"));
        assertEquals("1234567890123456", ana.getCard());
        assertNull(manager.findByEmail("ana@correo.es"));
    }
}
