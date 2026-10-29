package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        service = new UserService(new UserRepository(file));
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    private UserService restart() {
        return new UserService(new UserRepository(file));
    }

    @Test
    void createStoresAndPersists() throws Exception {
        assertEquals(2, service.getUsers().size());
        assertEquals(2, Files.readAllLines(file).size());
        assertEquals(2, restart().getUsers().size());
    }

    @Test
    void createRejectsInvalidData() {
        assertThrows(OperationException.class,
                () -> service.create("Eva", "Ruiz", "11111111A", "eva@correo.com", "Clave1!", "6", "1111222233334444"));
        assertThrows(OperationException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "eva@correo.es", "Clave1!", "6", "1111222233334444"));
        assertThrows(OperationException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "eva@correo.com", "clave1!", "6", "1111222233334444"));
        assertThrows(OperationException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "eva@correo.com", "Clave1!", "6", "111122223333444"));
        assertEquals(2, service.getUsers().size());
    }

    @Test
    void createRejectsDuplicateDniAndEmail() {
        assertThrows(OperationException.class,
                () -> service.create("Eva", "Ruiz", "12345678Z", "eva@correo.com", "Clave1!", "6", "1111222233334444"));
        assertThrows(OperationException.class,
                () -> service.create("Eva", "Ruiz", "11111111H", "ana@correo.com", "Clave1!", "6", "1111222233334444"));
        assertEquals(2, restart().getUsers().size());
    }

    @Test
    void duplicateDniDetectedAfterRestart() {
        UserService again = restart();
        assertThrows(OperationException.class,
                () -> again.create("Eva", "Ruiz", "12345678Z", "eva@correo.com", "Clave1!", "6", "1111222233334444"));
    }

    @Test
    void removeAuthenticatedUserIsPersisted() throws Exception {
        service.remove(service.authenticate("pedro@correo.com", "Otra2#"));
        assertNull(service.findByEmail("pedro@correo.com"));
        assertNull(restart().findByEmail("pedro@correo.com"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file));
    }

    @Test
    void authenticateRejectsWrongPasswordAndUnknownEmail() {
        assertThrows(OperationException.class, () -> service.authenticate("pedro@correo.com", "Otra2?"));
        assertThrows(OperationException.class, () -> service.authenticate("nadie@correo.com", "Otra2#"));
        assertEquals(2, restart().getUsers().size());
    }

    @Test
    void updateEachModifiableFieldIsPersisted() throws Exception {
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#");

        assertNull(service.findByEmail("ana@correo.com"));
        User ana = restart().findByEmail("ana.nueva@correo.com");
        assertNotNull(ana);
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("Nueva2#", ana.getPassword());
    }

    @Test
    void updateRejectsBadCredentials() {
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Wrong1!", "PHONE", "600000000"));
        assertThrows(OperationException.class,
                () -> service.update("nadie@correo.com", "Clave1!", "PHONE", "600000000"));
        assertEquals("612345678", restart().findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void updateRejectsDniUnknownFieldAndInvalidValues() {
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "AGE", "30"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "CARD", "123456789012345"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es"));
        assertThrows(OperationException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));

        User ana = restart().findByEmail("ana@correo.com");
        assertEquals("12345678Z", ana.getDni());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void updateEmailToOwnEmailIsAllowed() throws Exception {
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }
}
