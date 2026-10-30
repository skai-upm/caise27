package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserServiceTest {

    @TempDir
    Path dir;

    private Path file;
    private UserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        Park.getInstance().open(dir);
        repository = Park.getInstance().getUsers();
        service = new UserService();
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private UserRepository reload() {
        UserRepository reloaded = new UserRepository(file);
        reloaded.load();
        return reloaded;
    }

    @Test
    void createsValidUser() {
        assertNotNull(reload().findByEmail("ana@correo.com"));
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> service.create("Pedro", "Lopez", "87654321A",
                "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> service.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.es", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> service.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.com", "otra2#", "623456789", "9876543210987654"));
        assertThrows(IllegalArgumentException.class, () -> service.create("Pedro", "Lopez", "87654321X",
                "pedro@correo.com", "Otra2#", "623456789", "987654321098765"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        assertThrows(IllegalArgumentException.class, () -> service.create("Otra", "Ana", "12345678Z",
                "otra@correo.com", "Clave1!", "1", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> service.create("Pedro", "Lopez", "87654321X",
                "ana@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, reload().findAll().size());
    }

    @Test
    void deletesWithCorrectCredentials() {
        service.delete("ana@correo.com", "Clave1!");
        assertNull(repository.findByEmail("ana@correo.com"));
        assertTrue(reload().findAll().isEmpty());
    }

    @Test
    void deleteRejectsUnknownEmailAndWrongPassword() {
        assertThrows(IllegalArgumentException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertThrows(IllegalArgumentException.class, () -> service.delete("ana@correo.com", "Mala1!"));
        assertNotNull(reload().findByEmail("ana@correo.com"));
    }

    @Test
    void updatesEveryModifiableFieldAndPersists() {
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", UserField.EMAIL, "ana.nueva@correo.com");

        User ana = reload().findByDni("12345678Z");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertTrue(ana.checkPassword("Nueva2#"));
        assertEquals("ana.nueva@correo.com", ana.getEmail());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void updateRejectsWrongCredentials() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertEquals("Ana", reload().findByDni("12345678Z").getFirstName());
    }

    @Test
    void updateRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234 5678 9012 3456"));
        User ana = reload().findByDni("12345678Z");
        assertEquals("ana@correo.com", ana.getEmail());
        assertTrue(ana.checkPassword("Clave1!"));
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertEquals("ana@correo.com", reload().findByDni("12345678Z").getEmail());
    }

    @Test
    void updateAllowsKeepingOwnEmail() {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertEquals("ana@correo.com", reload().findByDni("12345678Z").getEmail());
    }
}
