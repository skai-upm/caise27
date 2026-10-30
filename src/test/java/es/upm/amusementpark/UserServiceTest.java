package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserServiceTest {

    @TempDir
    Path dir;

    private Path file;
    private UserService service;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        service = new UserService(new UserRepository(file));
    }

    private String createAna() {
        return service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private String createPedro() {
        return service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserService restart() throws IOException {
        return new UserService(new UserRepository(file));
    }

    @Test
    void validCreateIsPersisted() throws IOException {
        assertTrue(createAna().startsWith("OK"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));
    }

    @Test
    void invalidFieldsAreRejected() {
        assertTrue(service.create("Ana", "G", "12345678A", "ana@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(service.create("Ana", "G", "12345678Z", "ana@correo.es", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(service.create("Ana", "G", "12345678Z", "ana@correo.com", "clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(service.create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234 5678 9012 3456").startsWith("ERROR"));
        assertTrue(service.create("", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(service.getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    void duplicateDniOrEmailRejected() {
        createAna();
        assertTrue(service.create("Otro", "X", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertTrue(service.create("Otro", "X", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void authenticatedDelete() throws IOException {
        createAna();
        assertTrue(service.delete("ana@correo.com", "Clave1!").startsWith("OK"));
        assertTrue(service.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @Test
    void deleteWithWrongCredentialsChangesNothing() {
        createAna();
        assertTrue(service.delete("ana@correo.com", "Wrong1!").startsWith("ERROR"));
        assertTrue(service.delete("nadie@correo.com", "Clave1!").startsWith("ERROR"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void updateEachEditableField() throws IOException {
        createAna();
        assertTrue(service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria").startsWith("OK"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia").startsWith("OK"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "PHONE", "699999999").startsWith("OK"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com").startsWith("OK"));
        assertTrue(service.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#").startsWith("OK"));
        assertTrue(service.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666").startsWith("OK"));
        User u = restart().getUsers().get(0);
        assertEquals("Ana Maria", u.getName());
        assertEquals("Garcia", u.getSurname());
        assertEquals("699999999", u.getPhone());
        assertEquals("ana.nueva@correo.com", u.getEmail());
        assertEquals("Nueva2#", u.getPassword());
        assertEquals("9999888877776666", u.getCard());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void updateWithWrongCredentialsChangesNothing() {
        createAna();
        assertTrue(service.update("ana@correo.com", "Wrong1!", "NAME", "X").startsWith("ERROR"));
        assertTrue(service.update("nadie@correo.com", "Clave1!", "NAME", "X").startsWith("ERROR"));
        assertEquals("Ana", service.getUsers().get(0).getName());
    }

    @Test
    void emailUpdateMustBeUnique() {
        createAna();
        createPedro();
        assertTrue(service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com").startsWith("ERROR"));
        assertEquals("ana@correo.com", service.getUsers().get(0).getEmail());
    }

    @Test
    void invalidNewValuesRejected() throws IOException {
        createAna();
        assertTrue(service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave").startsWith("ERROR"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "CARD", "123").startsWith("ERROR"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo").startsWith("ERROR"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "DNI", "87654321X").startsWith("ERROR"));
        assertTrue(service.update("ana@correo.com", "Clave1!", "COLOR", "red").startsWith("ERROR"));
        User u = restart().getUsers().get(0);
        assertEquals("Clave1!", u.getPassword());
        assertEquals("1234567890123456", u.getCard());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void restartRestoresUsersAndUniqueness() throws IOException {
        createAna();
        UserService restarted = restart();
        assertEquals(1, restarted.getUsers().size());
        assertTrue(restarted.create("Otro", "X", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456").startsWith("ERROR"));
    }
}
