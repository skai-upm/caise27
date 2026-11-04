package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserServiceTest {

    @TempDir
    Path dir;
    Path file;
    UserService service;

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        service = new UserService(new UserRepository(file));
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private List<String> fileLines() throws Exception {
        return Files.readAllLines(file);
    }

    @Test
    void validCreationIsStored() throws Exception {
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                fileLines());
    }

    @Test
    void duplicateDniOrEmailRejected() throws Exception {
        List<String> before = fileLines();
        assertThrows(IllegalArgumentException.class, () -> service.create("Otro", "X", "12345678Z",
                "otro@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(IllegalArgumentException.class, () -> service.create("Otro", "X", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(1, service.getUsers().size());
        assertEquals(before, fileLines());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Pe|X|12345678A|pe@correo.com|Clave1!|600|1234567890123456",
            "Pe|X|87654321X|pe@correo.es|Clave1!|600|1234567890123456",
            "Pe|X|87654321X|pe@correo.com|clave1!|600|1234567890123456",
            "Pe|X|87654321X|pe@correo.com|Clave1!|600|1234 5678 9012 3456",
            "' '|X|87654321X|pe@correo.com|Clave1!|600|1234567890123456",
            "Pe|X|87654321X|pe@correo.com|Clave1!|''|1234567890123456"})
    void invalidCreationNotStored(String n, String s, String dni, String e, String p, String ph, String c)
            throws Exception {
        List<String> before = fileLines();
        assertThrows(IllegalArgumentException.class, () -> service.create(n, s, dni, e, p, ph, c));
        assertEquals(1, service.getUsers().size());
        assertEquals(before, fileLines());
    }

    @Test
    void sourceUpdateChain() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        service.update("ana.nueva@correo.com", "Nueva2#", UserField.CARD, "9999888877776666");
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana.nueva@correo.com;Nueva2#;612345678;9999888877776666"),
                fileLines());
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Nueva2#", UserField.PHONE, "699"));
        assertThrows(IllegalArgumentException.class,
                () -> service.delete("ana.nueva@correo.com", "Clave1!"));
    }

    @Test
    void surnameAndPhoneEditable() throws Exception {
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia Ruiz");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        User u = service.findByEmail("ana@correo.com");
        assertEquals("Garcia Ruiz", u.getSurname());
        assertEquals("699999999", u.getPhone());
        assertEquals("12345678Z", u.getDni());
    }

    @Test
    void failedAuthenticationChangesNothing() throws Exception {
        List<String> before = fileLines();
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Mala1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("nadie@correo.com", "Clave1!", UserField.NAME, "X"));
        assertThrows(IllegalArgumentException.class, () -> service.delete("ana@correo.com", "Mala1!"));
        assertThrows(IllegalArgumentException.class, () -> service.delete("nadie@correo.com", "Clave1!"));
        assertEquals("Ana", service.findByEmail("ana@correo.com").getName());
        assertEquals(before, fileLines());
    }

    @Test
    void invalidNewValuesRejected() throws Exception {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        List<String> before = fileLines();
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        assertThrows(IllegalArgumentException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.NAME, " "));
        assertEquals(before, fileLines());
        assertEquals("1234567890123456", service.findByEmail("ana@correo.com").getCard());
    }

    @Test
    void keepingOwnEmailAccepted() {
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void deleteRemovesLine() throws Exception {
        service.delete("ana@correo.com", "Clave1!");
        assertNull(service.findByEmail("ana@correo.com"));
        assertEquals(List.of(), fileLines());
    }

    @Test
    void restartRecoversExactState() throws Exception {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        service.update("pedro@correo.com", "Otra2#", UserField.PHONE, "700000000");
        service.delete("ana@correo.com", "Clave1!");

        UserService restarted = new UserService(new UserRepository(file));
        assertEquals(1, restarted.getUsers().size());
        User p = restarted.findByEmail("pedro@correo.com");
        assertEquals("700000000", p.getPhone());
        assertNull(restarted.findByEmail("ana@correo.com"));
        // uniqueness still enforced against recovered users
        assertThrows(IllegalArgumentException.class, () -> restarted.create("X", "Y", "87654321X",
                "x@correo.com", "Clave1!", "1", "1234567890123456"));
        // the deleted user's identifiers are free again
        restarted.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void updateKeepsSameUserInstance() {
        User before = service.findByEmail("ana@correo.com");
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        service.update("ana.nueva@correo.com", "Clave1!", UserField.PHONE, "699999999");
        assertSame(before, service.findByEmail("ana.nueva@correo.com"));
        assertEquals("699999999", before.getPhone());
    }

    @Test
    void deleteReturnsRemovedUser() {
        User ana = service.findByEmail("ana@correo.com");
        assertSame(ana, service.delete("ana@correo.com", "Clave1!"));
    }
}
