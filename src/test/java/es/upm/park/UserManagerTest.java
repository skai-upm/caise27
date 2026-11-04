package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        manager = new UserManager(new UserFileStore(file));
    }

    private Result createAna() {
        return manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    private Result createPedro() {
        return manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com",
                "Otra2#", "623456789", "9876543210987654");
    }

    private UserManager restart() throws IOException {
        return new UserManager(new UserFileStore(file));
    }

    private List<String> fileLines() throws IOException {
        return Files.exists(file) ? Files.readAllLines(file) : List.of();
    }

    // ---- create ----

    @Test
    void validCreationIsStoredAndPersisted() throws IOException {
        assertTrue(createAna().success());
        assertEquals(1, manager.getUsers().size());
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                fileLines());
        assertEquals("ana@correo.com", restart().getUsers().get(0).getEmail());
    }

    @Test
    void invalidFieldsAreNotStored() throws IOException {
        assertFalse(manager.create("Ana", "Garcia", "12345678A", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertFalse(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.es", "Clave1!", "6", "1234567890123456").success());
        assertFalse(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "clave1!", "6", "1234567890123456").success());
        assertFalse(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "123456789012345").success());
        assertFalse(manager.create("", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertFalse(manager.create("Ana", " ", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertFalse(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "", "1234567890123456").success());
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(fileLines().isEmpty());
    }

    @Test
    void phoneIsStoredAsSupplied() {
        manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "+34 612 345 678", "1234567890123456");
        assertEquals("+34 612 345 678", manager.getUsers().get(0).getPhone());
    }

    @Test
    void duplicateDniRejected() throws IOException {
        createAna();
        List<String> before = fileLines();
        assertFalse(manager.create("Otra", "Persona", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertEquals(before, fileLines());
    }

    @Test
    void duplicateEmailRejected() {
        createAna();
        assertFalse(manager.create("Otra", "Persona", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertFalse(manager.create("Otra", "Persona", "87654321X", "ANA@correo.com", "Clave1!", "6", "1234567890123456").success());
        assertEquals(1, manager.getUsers().size());
    }

    // ---- delete ----

    @Test
    void deleteWithCorrectCredentials() throws IOException {
        createAna();
        createPedro();
        assertTrue(manager.delete("ana@correo.com", "Clave1!").success());
        assertEquals(1, manager.getUsers().size());
        assertEquals("87654321X", restart().getUsers().get(0).getDni());
    }

    @Test
    void deleteWithWrongPasswordOrUnknownEmailFails() throws IOException {
        createAna();
        List<String> before = fileLines();
        assertFalse(manager.delete("ana@correo.com", "Mala1!").success());
        assertFalse(manager.delete("nadie@correo.com", "Clave1!").success());
        assertEquals(1, manager.getUsers().size());
        assertEquals(before, fileLines());
    }

    // ---- update ----

    @Test
    void updatesEveryEditableFieldAndPersists() throws IOException {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria").success());
        assertTrue(manager.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia Ruiz").success());
        assertTrue(manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999").success());
        assertTrue(manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com").success());
        assertTrue(manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#").success());
        assertTrue(manager.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666").success());

        User u = restart().getUsers().get(0);
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("Garcia Ruiz", u.getSurnames());
        assertEquals("699999999", u.getPhone());
        assertEquals("ana.nueva@correo.com", u.getEmail());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCardNumber());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void newCredentialsTakeEffect() {
        createAna();
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.NAME, "X").success());
        assertFalse(manager.delete("ana.nueva@correo.com", "Clave1!").success());
        assertTrue(manager.delete("ana.nueva@correo.com", "Nueva2#").success());
    }

    @Test
    void invalidUpdatesRejectedAndValueUnchanged() throws IOException {
        createAna();
        createPedro();
        List<String> before = fileLines();
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com").success());
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "PEDRO@correo.com").success());
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es").success());
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Clave12").success());
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.CARD, "1234 5678 9012 3456").success());
        assertFalse(manager.update("ana@correo.com", "Clave1!", UserField.NAME, " ").success());
        assertFalse(manager.update("ana@correo.com", "Mala1!", UserField.NAME, "X").success());
        assertFalse(manager.update("nadie@correo.com", "Clave1!", UserField.NAME, "X").success());
        assertEquals(before, fileLines());
        User ana = manager.getUsers().get(0);
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCardNumber());
    }

    @Test
    void updatingOwnEmailToSameValueIsAllowed() {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com").success());
    }

    // ---- restart recovery ----

    @Test
    void restartRecoversExactCurrentState() throws IOException {
        createAna();
        createPedro();
        manager.update("ana@correo.com", "Clave1!", UserField.PHONE, "600000000");
        manager.delete("pedro@correo.com", "Otra2#");
        List<User> recovered = restart().getUsers();
        assertEquals(1, recovered.size());
        assertEquals("12345678Z", recovered.get(0).getDni());
        assertEquals("600000000", recovered.get(0).getPhone());
    }

    // ---- identity and lookup ----

    @Test
    void updateKeepsTheSameUserInstance() {
        createAna();
        User before = manager.getUsers().get(0);
        manager.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        manager.update("ana.nueva@correo.com", "Clave1!", UserField.PHONE, "600000000");
        assertSame(before, manager.getUsers().get(0));
        assertEquals("ana.nueva@correo.com", before.getEmail());
        assertEquals("600000000", before.getPhone());
    }

    @Test
    void rejectedUpdateDoesNotTouchInstance() {
        createAna();
        User before = manager.getUsers().get(0);
        manager.update("ana@correo.com", "Clave1!", UserField.CARD, "123");
        assertSame(before, manager.getUsers().get(0));
        assertEquals("1234567890123456", before.getCardNumber());
    }

    @Test
    void findsUsersByEmailAndDni() {
        createAna();
        User ana = manager.getUsers().get(0);
        assertSame(ana, manager.findByEmail("ANA@correo.com").orElseThrow());
        assertSame(ana, manager.findByDni("12345678Z").orElseThrow());
        assertTrue(manager.findByEmail("nadie@correo.com").isEmpty());
        assertTrue(manager.findByDni("87654321X").isEmpty());
    }
}
