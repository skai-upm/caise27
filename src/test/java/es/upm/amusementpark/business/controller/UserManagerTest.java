package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.FileUserRepository;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserManagerTest {

    private static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    @TempDir
    Path dir;

    private Path file;
    private UserManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = UserManager.initialize(new FileUserRepository(file), HASHER);
    }

    @AfterEach
    void tearDown() {
        UserManager.reset();
    }

    private void createAna() throws IOException {
        manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private void createPedro() throws IOException {
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
    }

    private List<String> fileLines() throws IOException {
        return Files.exists(file) ? Files.readAllLines(file) : List.of();
    }

    @Test
    void createsAndPersistsValidUser() throws IOException {
        createAna();

        assertNotNull(manager.findByEmail("ana@correo.com"));
        List<String> lines = fileLines();
        assertEquals(1, lines.size());
        String[] fields = lines.get(0).split(";");
        assertEquals("Ana;Garcia Lopez;12345678Z;ana@correo.com",
                String.join(";", fields[0], fields[1], fields[2], fields[3]));
        assertTrue(BCryptPasswordHasher.isHash(fields[4]));
        assertTrue(HASHER.matches("Clave1!", fields[4]));
        assertFalse(lines.get(0).contains("Clave1!"));
        assertEquals("612345678;1234567890123456", fields[5] + ";" + fields[6]);
    }

    @Test
    void rejectsInvalidDataWithoutStoring() {
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "Garcia", "12345678A",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.es", "Clave1!", "612345678", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Ana", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234 5678 9012 3456"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("", "Garcia", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(!Files.exists(file));
    }

    @Test
    void rejectsDuplicateDniOrEmail() throws IOException {
        createAna();

        assertThrows(IllegalArgumentException.class, () -> manager.create("Otra", "Persona", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertThrows(IllegalArgumentException.class, () -> manager.create("Otra", "Persona", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void deletesOnlyWithCorrectCredentials() throws IOException {
        createAna();

        assertThrows(IllegalArgumentException.class, () -> manager.delete("ana@correo.com", "Wrong1!"));
        assertThrows(IllegalArgumentException.class, () -> manager.delete("nadie@correo.com", "Clave1!"));
        assertEquals(1, manager.getUsers().size());

        manager.delete("ana@correo.com", "Clave1!");
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(fileLines().isEmpty());
    }

    @Test
    void appliesChainedUpdatesFromTheSpecification() throws IOException {
        createAna();

        manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.PHONE, "699999999");
        manager.update("ana.nueva@correo.com", "Nueva2#", UserField.SURNAME, "Garcia");

        String[] fields = fileLines().get(0).split(";");
        assertEquals("Ana Maria;Garcia;12345678Z;ana.nueva@correo.com",
                String.join(";", fields[0], fields[1], fields[2], fields[3]));
        assertTrue(HASHER.matches("Nueva2#", fields[4]));
        assertFalse(HASHER.matches("Clave1!", fields[4]));
        assertEquals("699999999;9999888877776666", fields[5] + ";" + fields[6]);
    }

    @Test
    void rejectsUpdateWithWrongCredentials() throws IOException {
        createAna();

        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Wrong1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertEquals("Ana", manager.findByEmail("ana@correo.com").getName());
    }

    @Test
    void rejectsEmailOfAnotherUser() throws IOException {
        createAna();
        createPedro();
        List<String> before = fileLines();

        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertEquals(before, fileLines());
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void acceptsUpdatingEmailToItsCurrentValue() throws IOException {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    void revalidatesNewValues() throws IOException {
        createAna();
        List<String> before = fileLines();

        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.CARD, "123456789012345"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("ana@correo.com", "Clave1!", UserField.NAME, " "));

        User ana = manager.findByEmail("ana@correo.com");
        assertTrue(HASHER.matches("Clave1!", ana.getPassword()));
        assertEquals("1234567890123456", ana.getCard());
        assertEquals(before, fileLines());
    }

    @Test
    void restartRestoresUsersAndUniqueness() throws IOException {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        createPedro();
        manager.delete("pedro@correo.com", "Otra2#");

        UserManager.reset();
        UserManager restarted = UserManager.initialize(new FileUserRepository(file), HASHER);

        assertEquals(1, restarted.getUsers().size());
        User ana = restarted.findByEmail("ana@correo.com");
        assertEquals("699999999", ana.getPhone());
        assertNull(restarted.findByEmail("pedro@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> restarted.create("Otra", "Persona", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1111222233334444"));
    }

    @Test
    void rollsBackWhenSavingFails() throws IOException {
        Path notADirectory = dir.resolve("blocked");
        Files.writeString(notADirectory, "");
        UserManager.reset();
        UserManager failing = UserManager.initialize(new FileUserRepository(notADirectory.resolve("users.txt")), HASHER);

        assertThrows(IOException.class, () -> failing.create("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(failing.getUsers().isEmpty());
    }
}
