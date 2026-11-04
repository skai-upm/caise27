package es.upm.etsisi.amusementpark.business.service;

import es.upm.etsisi.amusementpark.TestSupport;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.model.UserField;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        manager = new UserManager(new UserFileRepository(file), TestSupport.HASHER);
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private UserManager restart() throws IOException {
        return new UserManager(new UserFileRepository(file), TestSupport.HASHER);
    }

    private User ana(UserManager m) {
        return m.findByEmail("ana@correo.com").orElseThrow();
    }

    @Test
    void createStoresUserAndSurvivesRestart() throws Exception {
        UserManager restarted = restart();
        assertEquals(1, restarted.getUsers().size());
        assertEquals("Garcia Lopez", ana(restarted).getSurname());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "''|Lopez|87654321X|p@correo.com|Clave1!|600|1234567890123456",
        "Pedro|' '|87654321X|p@correo.com|Clave1!|600|1234567890123456",
        "Pedro|Lopez|87654321A|p@correo.com|Clave1!|600|1234567890123456",
        "Pedro|Lopez|87654321X|p@correo.es|Clave1!|600|1234567890123456",
        "Pedro|Lopez|87654321X|p@correo.com|clave1!|600|1234567890123456",
        "Pedro|Lopez|87654321X|p@correo.com|Clave1!|''|1234567890123456",
        "Pedro|Lopez|87654321X|p@correo.com|Clave1!|600|1234 5678 9012 3456",
        "Pedro|Lopez|12345678Z|otro@correo.com|Clave1!|600|1234567890123456",
        "Pedro|Lopez|87654321X|ana@correo.com|Clave1!|600|1234567890123456",
        "Pedro|Lopez|87654321X|ANA@correo.com|Clave1!|600|1234567890123456"
    })
    void rejectedCreationStoresNothing(String n, String s, String dni, String e, String p, String ph, String c)
            throws Exception {
        byte[] before = Files.readAllBytes(file);
        assertThrows(UserException.class, () -> manager.create(n, s, dni, e, p, ph, c));
        assertEquals(1, manager.getUsers().size());
        assertEquals(new String(before), new String(Files.readAllBytes(file)));
    }

    @Test
    void phoneIsStoredUnchanged() throws Exception {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "+34 612 345 678", "9876543210987654");
        assertEquals("+34 612 345 678", restart().findByEmail("pedro@correo.com").orElseThrow().getPhone());
    }

    @Test
    void deleteRequiresCorrectPassword() throws Exception {
        assertThrows(UserException.class, () -> manager.delete("ana@correo.com", "Wrong1!"));
        assertThrows(UserException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, restart().getUsers().size());

        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "NAME, Ana Maria",
        "SURNAME, Garcia Perez",
        "PHONE, 699999999",
        "PASSWORD, Nueva2#",
        "CARD, 9999888877776666"
    })
    void updatesEachFieldAndPersists(UserField field, String value) throws Exception {
        manager.update("ana@correo.com", "Clave1!", field, value);
        User u = ana(restart());
        String stored = switch (field) {
            case NAME -> u.getName();
            case SURNAME -> u.getSurname();
            case PHONE -> u.getPhone();
            case PASSWORD -> u.getPassword();
            case CARD -> u.getCard();
            case EMAIL -> u.getEmail();
        };
        if (field == UserField.PASSWORD) {
            assertTrue(TestSupport.HASHER.matches(value, stored), "stored value must be a hash of the new password");
        } else {
            assertEquals(value, stored);
        }
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void credentialChangeSequence() throws Exception {
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");

        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Nueva2#", UserField.NAME, "X"));
        assertThrows(UserException.class, () -> manager.update("ana.nueva@correo.com", "Clave1!", UserField.NAME, "X"));

        User u = restart().findByEmail("ana.nueva@correo.com").orElseThrow();
        assertTrue(TestSupport.HASHER.matches("Nueva2#", u.getPassword()));
        assertEquals("9999888877776666", u.getCard());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void updateToOwnEmailIsAllowed() throws Exception {
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertEquals("ana@correo.com", ana(manager).getEmail());
    }

    @Test
    void rejectedUpdatesKeepExistingValues() throws Exception {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        byte[] before = Files.readAllBytes(file);

        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        assertThrows(UserException.class, () -> manager.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "123"));
        assertThrows(UserException.class, () -> manager.update("ana@correo.com", "Clave1!", UserField.NAME, " "));

        User u = ana(manager);
        assertEquals("ana@correo.com", u.getEmail());
        assertTrue(TestSupport.HASHER.matches("Clave1!", u.getPassword()));
        assertEquals("1234567890123456", u.getCard());
        assertEquals("Ana", u.getName());
        assertEquals(new String(before), new String(Files.readAllBytes(file)));
    }

    @Test
    void restartRecoversCurrentState() throws Exception {
        manager.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.delete("pedro@correo.com", "Otra2#");

        UserManager restarted = restart();
        assertEquals(1, restarted.getUsers().size());
        assertEquals("Ana Maria", ana(restarted).getName());
        assertThrows(UserException.class, () -> restarted.create("X", "Y", "12345678Z", "x@correo.com", "Clave1!", "1", "1234567890123456"));
    }

    @Test
    void failedSaveRollsBack() throws Exception {
        Path notADirectory = dir.resolve("plain-file");
        Files.writeString(notADirectory, "x");
        UserManager m = new UserManager(new UserFileRepository(notADirectory.resolve("users.txt")), TestSupport.HASHER);
        assertThrows(UserException.class, () -> m.create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456"));
        assertTrue(m.getUsers().isEmpty());
    }
}
