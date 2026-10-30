package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.model.User;
import es.upm.etsisi.amusementpark.repository.UserFileStore;
import es.upm.etsisi.amusementpark.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    @TempDir
    Path dir;

    private Path file;
    private UserRepository repository;
    private UserController controller;

    @BeforeEach
    void setUp() {
        file = dir.resolve("users.txt");
        repository = new UserRepository(new UserFileStore(file));
        controller = new UserController(repository);
        controller.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        controller.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private User reloaded(String email) {
        return new UserRepository(new UserFileStore(file)).findByEmail(email).orElseThrow();
    }

    @Test
    void createStoresUser() {
        assertEquals(2, repository.findAll().size());
        assertEquals("Ana", reloaded("ana@correo.com").getFirstName());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12345678A|new@correo.com|Clave1!|1234567890123456|Invalid DNI",
            "11111111H|new@correo.es|Clave1!|1234567890123456|Invalid email",
            "11111111H|new@correo.com|clave1!|1234567890123456|Invalid password",
            "11111111H|new@correo.com|Clave1!|123456789012345|Invalid card number",
            "12345678Z|new@correo.com|Clave1!|1234567890123456|DNI already registered",
            "11111111H|ana@correo.com|Clave1!|1234567890123456|Email already registered"})
    void createRejectsInvalidOrDuplicateData(String dni, String email, String password, String card, String message) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> controller.create("Luis", "Perez", dni, email, password, "600000000", card));
        assertEquals(message, e.getMessage());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void deleteWithCorrectPassword() {
        controller.delete("pedro@correo.com", "Otra2#");
        assertTrue(repository.findByEmail("pedro@correo.com").isEmpty());
        assertTrue(new UserRepository(new UserFileStore(file)).findByEmail("pedro@correo.com").isEmpty());
    }

    @Test
    void deleteWithWrongPasswordKeepsUser() {
        assertThrows(IllegalArgumentException.class, () -> controller.delete("pedro@correo.com", "Wrong1!"));
        assertTrue(repository.findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void deleteUnknownUser() {
        assertThrows(IllegalArgumentException.class, () -> controller.delete("nobody@correo.com", "Clave1!"));
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void updateEachFieldAndPersist() {
        controller.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        controller.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        controller.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        controller.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        controller.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        controller.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");

        User u = reloaded("ana.nueva@correo.com");
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("Garcia", u.getSurnames());
        assertEquals("699999999", u.getPhone());
        assertEquals("9999888877776666", u.getCardNumber());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() {
        assertDoesNotThrow(() -> controller.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "EMAIL|pedro@correo.com|Email already registered",
            "EMAIL|ana@correo|Invalid email",
            "PASSWORD|clave1!|Invalid password",
            "CARD|1234 5678 9012 3456|Invalid card number",
            "DNI|87654321X|Field cannot be modified: DNI",
            "AGE|30|Field cannot be modified: AGE"})
    void updateRejectsInvalidChanges(String field, String value, String message) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> controller.update("ana@correo.com", "Clave1!", field, value));
        assertEquals(message, e.getMessage());
        User u = reloaded("ana@correo.com");
        assertEquals("12345678Z", u.getDni());
        assertEquals("Clave1!", u.getPassword());
        assertEquals("1234567890123456", u.getCardNumber());
    }

    @Test
    void updateRequiresCredentials() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.update("ana@correo.com", "Wrong1!", "PHONE", "600000000"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.update("nobody@correo.com", "Clave1!", "PHONE", "600000000"));
        assertEquals("612345678", reloaded("ana@correo.com").getPhone());
    }
}
