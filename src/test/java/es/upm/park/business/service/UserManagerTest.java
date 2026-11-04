package es.upm.park.business.service;

import es.upm.park.business.model.User;
import es.upm.park.business.model.UserField;
import es.upm.park.persistence.UserFileStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserManagerTest {

    @TempDir
    Path dir;
    Path file;
    UserManager manager;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserFileStorage(file));
    }

    private void createAna() {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() {
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private UserManager restart() {
        return new UserManager(new UserFileStorage(file));
    }

    @Test
    void validCreationIsStoredAndPersisted() {
        createAna();
        assertEquals(1, manager.getUsers().size());
        assertEquals("Ana", restart().findByEmail("ana@correo.com").getName());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "''|Garcia|12345678Z|ana@correo.com|Clave1!|612345678|1234567890123456",
        "Ana|''|12345678Z|ana@correo.com|Clave1!|612345678|1234567890123456",
        "Ana|Garcia|12345678A|ana@correo.com|Clave1!|612345678|1234567890123456",
        "Ana|Garcia|12345678Z|ana@correo.es|Clave1!|612345678|1234567890123456",
        "Ana|Garcia|12345678Z|ana@correo.com|Clave12|612345678|1234567890123456",
        "Ana|Garcia|12345678Z|ana@correo.com|Clave1!|''|1234567890123456",
        "Ana|Garcia|12345678Z|ana@correo.com|Clave1!|612345678|123456789012345",
    })
    void invalidDataIsNotStored(String n, String s, String dni, String e, String p, String ph, String c) {
        assertThrows(IllegalArgumentException.class, () -> manager.create(n, s, dni, e, p, ph, c));
        assertEquals(0, manager.getUsers().size());
        assertEquals(false, Files.exists(file));
    }

    @Test
    void phoneIsStoredVerbatim() {
        manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "+34 612 345 678", "1234567890123456");
        assertEquals("+34 612 345 678", restart().findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void duplicateDniOrEmailRejectedAndFileUnchanged() throws IOException {
        createAna();
        List<String> before = Files.readAllLines(file);
        assertThrows(IllegalArgumentException.class, () ->
                manager.create("Otro", "X", "12345678Z", "otro@correo.com", "Clave1!", "1", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () ->
                manager.create("Otro", "X", "87654321X", "ana@correo.com", "Clave1!", "1", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () ->
                manager.create("Otro", "X", "87654321X", "ANA@correo.com", "Clave1!", "1", "1234567890123456"));
        assertEquals(1, manager.getUsers().size());
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void deleteWithCorrectCredentials() {
        createAna();
        manager.delete("ana@correo.com", "Clave1!");
        assertNull(manager.findByEmail("ana@correo.com"));
        assertEquals(0, restart().getUsers().size());
    }

    @Test
    void deleteRejectedForWrongPasswordOrUnknownEmail() {
        createAna();
        assertThrows(IllegalArgumentException.class, () -> manager.delete("ana@correo.com", "Mala1!"));
        assertThrows(IllegalArgumentException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, restart().getUsers().size());
    }

    @Test
    void updateAllEditableFields() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia Ruiz");
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699000111");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");

        User u = restart().findByEmail("ana.nueva@correo.com");
        assertNotNull(u);
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia Ruiz", u.getSurnames());
        assertEquals("699000111", u.getPhone());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCard());
        assertEquals("12345678Z", u.getDni());
        assertNull(restart().findByEmail("ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> manager.delete("ana.nueva@correo.com", "Clave1!"));
    }

    @Test
    void rejectedUpdatesLeaveDataUnchanged() throws IOException {
        createAna();
        createPedro();
        List<String> before = Files.readAllLines(file);
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.NAME, " "));
        User ana = manager.findByEmail("ana@correo.com");
        assertEquals("Ana", ana.getName());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void updateEmailToOwnEmailInDifferentCaseIsAllowed() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "Ana@correo.com");
        assertEquals("Ana@correo.com", manager.findByEmail("ana@correo.com").getEmail());
    }

    @Test
    void updateModifiesTheSameUserObject() {
        createAna();
        User ana = manager.findByEmail("ana@correo.com");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertSame(ana, manager.findByEmail("ana.nueva@correo.com"));
        assertEquals("ana.nueva@correo.com", ana.getEmail());
        assertSame(ana, manager.findByDni("12345678Z"));
    }

    @Test
    void deleteReturnsRemovedUser() {
        createAna();
        User ana = manager.findByEmail("ana@correo.com");
        assertSame(ana, manager.delete("ana@correo.com", "Clave1!"));
    }

    @Test
    void restartRecoversOnlyCurrentState() {
        createAna();
        createPedro();
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.delete("pedro@correo.com", "Otra2#");

        UserManager again = restart();
        assertEquals(1, again.getUsers().size());
        assertEquals("Ana Maria", again.findByEmail("ana@correo.com").getName());
        again.create("Pedro", "Nuevo", "11111111H", "pedro@correo.com", "Otra2#", "1", "1111222233334444");
        assertEquals(2, again.getUsers().size());
    }
}
