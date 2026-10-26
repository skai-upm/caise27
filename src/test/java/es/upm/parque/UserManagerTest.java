package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserManagerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserRepository(file));
    }

    private void createAna() {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void createStoresAndPersists() {
        createAna();
        assertEquals(1, manager.getUsers().size());
        User reloaded = new UserManager(new UserRepository(file)).findByEmail("ana@correo.com");
        assertNotNull(reloaded);
        assertEquals("612345678", reloaded.getPhone());
    }

    @Test
    void dniLetterIsStoredUpperCase() {
        manager.create("Ana", "Garcia", "12345678z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertNotNull(manager.findByDni("12345678Z"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @ParameterizedTest
    @CsvSource({
            "'',Garcia,12345678Z,ana@correo.com,Clave1!,6,1234567890123456",
            "Ana,'',12345678Z,ana@correo.com,Clave1!,6,1234567890123456",
            "Ana,Garcia,'',ana@correo.com,Clave1!,6,1234567890123456",
            "Ana,Garcia,12345678Z,'',Clave1!,6,1234567890123456",
            "Ana,Garcia,12345678Z,ana@correo.com,'',6,1234567890123456",
            "Ana,Garcia,12345678Z,ana@correo.com,Clave1!,'',1234567890123456",
            "Ana,Garcia,12345678Z,ana@correo.com,Clave1!,6,''",
            "Ana,Garcia,12345678A,ana@correo.com,Clave1!,6,1234567890123456",
            "Ana,Garcia,12345678Z,ana@correo.es,Clave1!,6,1234567890123456",
            "Ana,Garcia,12345678Z,ana@correo.com,clave1!,6,1234567890123456",
            "Ana,Garcia,12345678Z,ana@correo.com,Clave1!,6,123456789012345"
    })
    void invalidDataIsNotStored(String n, String s, String d, String e, String p, String t, String c) {
        assertThrows(UserException.class, () -> manager.create(n, s, d, e, p, t, c));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    void duplicateDniRejected() {
        createAna();
        UserException ex = assertThrows(UserException.class, () ->
                manager.create("Otra", "X", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456"));
        assertTrue(ex.getMessage().contains("DNI"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void duplicateEmailRejectedCaseInsensitive() {
        createAna();
        UserException ex = assertThrows(UserException.class, () ->
                manager.create("Pedro", "X", "87654321X", "ANA@correo.com", "Clave1!", "6", "1234567890123456"));
        assertTrue(ex.getMessage().contains("email"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deleteWithCorrectCredentials() {
        createAna();
        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(new UserManager(new UserRepository(file)).getUsers().isEmpty());
    }

    @Test
    void deleteUnknownEmailOrWrongPasswordRejected() {
        createAna();
        assertThrows(UserException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertThrows(UserException.class, () -> manager.delete("ana@correo.com", "Mala1!"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void updateEachEditableField() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", "surname", "Lopez");
        manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666");

        User u = new UserManager(new UserRepository(file)).findByEmail("ana.nueva@correo.com");
        assertEquals("Ana Maria", u.getName());
        assertEquals("Lopez", u.getSurname());
        assertEquals("699999999", u.getPhone());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCard());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void updateToOwnEmailIsAllowed() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void updateFailuresChangeNothing() {
        createAna();
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        assertThrows(UserException.class, () -> manager.update("nadie@correo.com", "Clave1!", "NAME", "X"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Mala1!", "NAME", "X"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "COLOR", "rojo"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", "NAME", " "));
        User ana = new UserManager(new UserRepository(file)).findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void dniCannotBeModified() {
        createAna();
        UserException ex = assertThrows(UserException.class, () ->
                manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertTrue(ex.getMessage().contains("DNI cannot be modified"));
        assertNull(manager.findByDni("87654321X"));
    }

    @Test
    void saveFailureRestoresPersistedState() {
        createAna();
        assertTrue(file.toFile().setWritable(false));
        try {
            assertThrows(UserException.class, () ->
                    manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654"));
            assertNull(manager.findByEmail("pedro@correo.com"));
            assertNotNull(manager.findByEmail("ana@correo.com"));
        } finally {
            assertTrue(file.toFile().setWritable(true));
        }
    }
}
