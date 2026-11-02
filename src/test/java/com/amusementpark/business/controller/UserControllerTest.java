package com.amusementpark.business.controller;

import com.amusementpark.business.model.User;
import com.amusementpark.business.security.BCryptPasswordHasher;
import com.amusementpark.business.security.PasswordHasher;
import com.amusementpark.persistence.UserFileRepository;
import com.amusementpark.persistence.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserControllerTest {

    private static final PasswordHasher HASHER = new BCryptPasswordHasher(4);

    @TempDir
    Path dir;

    private Path file;
    private UserController manager;

    @BeforeEach
    void setUp() throws IOException {
        file = dir.resolve("users.txt");
        manager = restart();
    }

    private String createAna() {
        return manager.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private UserController restart() throws IOException {
        UserController.getInstance().configure(new UserFileRepository(file), HASHER);
        return UserController.getInstance();
    }

    private static User only(UserController m) {
        assertEquals(1, m.getUsers().size());
        return m.getUsers().get(0);
    }

    @Test
    void createsValidUserAndPersistsIt() throws IOException {
        assertTrue(createAna().startsWith("OK:"));
        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        String[] fields = lines.get(0).split(";", -1);
        assertEquals(List.of("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com"), List.of(fields).subList(0, 4));
        assertEquals(List.of("612345678", "1234567890123456"), List.of(fields).subList(5, 7));
        assertTrue(BCryptPasswordHasher.isBCryptHash(fields[4]));
        assertFalse(lines.get(0).contains("Clave1!"), "no plaintext password stored");
        assertTrue(HASHER.matches("Clave1!", fields[4]));
        assertEquals("ana@correo.com", only(restart()).getEmail());
    }

    @Test
    void rejectsInvalidDataWithoutStoring() {
        assertTrue(manager.create("Ana", "Garcia", "12345678A", "ana@correo.com", "Clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.es", "Clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234 5678 9012 3456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("Ana", " ", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(Files.notExists(file));
    }

    @Test
    void rejectsDuplicateDniOrEmail() throws IOException {
        createAna();
        List<String> before = Files.readAllLines(file);
        assertTrue(manager.create("Otro", "X", "12345678Z", "otro@correo.com", "Clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertTrue(manager.create("Otro", "X", "87654321X", "ana@correo.com", "Clave1!", "6", "1234567890123456")
                .startsWith("ERROR:"));
        assertEquals(1, manager.getUsers().size());
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void deletesOnlyWithMatchingCredentials() throws IOException {
        createAna();
        assertTrue(manager.delete("ana@correo.com", "Wrong1!").startsWith("ERROR:"));
        assertTrue(manager.delete("nadie@correo.com", "Clave1!").startsWith("ERROR:"));
        assertEquals(1, manager.getUsers().size());

        assertTrue(manager.delete("ana@correo.com", "Clave1!").startsWith("OK:"));
        assertTrue(manager.getUsers().isEmpty());
        assertTrue(restart().getUsers().isEmpty());
    }

    @Test
    void updatesFieldsInSequenceAndPersists() throws IOException {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria").startsWith("OK:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia").startsWith("OK:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "PHONE", "699999999").startsWith("OK:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com").startsWith("OK:"));
        assertTrue(manager.update("ana.nueva@correo.com", "Clave1!", "PASSWORD", "Nueva2#").startsWith("OK:"));
        assertTrue(manager.update("ana.nueva@correo.com", "Nueva2#", "CARD", "9999888877776666").startsWith("OK:"));

        User ana = only(restart());
        assertEquals("Ana Maria", ana.getName());
        assertEquals("Garcia", ana.getSurname());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana.nueva@correo.com", ana.getEmail());
        assertTrue(HASHER.matches("Nueva2#", ana.getPassword()));
        assertFalse(HASHER.matches("Clave1!", ana.getPassword()));
        assertTrue(restart().authenticate("ana.nueva@correo.com", "Nueva2#") != null);
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
    }

    @Test
    void rejectsUpdatesWithWrongCredentials() throws IOException {
        createAna();
        List<String> before = Files.readAllLines(file);
        assertTrue(manager.update("ana@correo.com", "Bad1!", "NAME", "X").startsWith("ERROR:"));
        assertTrue(manager.update("nadie@correo.com", "Clave1!", "NAME", "X").startsWith("ERROR:"));
        assertEquals("Ana", only(manager).getName());
        assertEquals(before, Files.readAllLines(file));
    }

    @Test
    void rejectsInvalidOrNonEditableUpdatesKeepingState() throws IOException {
        createAna();
        manager.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        List<String> before = Files.readAllLines(file);

        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com").startsWith("ERROR:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es").startsWith("ERROR:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!").startsWith("ERROR:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "CARD", "123").startsWith("ERROR:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "NAME", "").startsWith("ERROR:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "DNI", "87654321X").startsWith("ERROR:"));
        assertTrue(manager.update("ana@correo.com", "Clave1!", "AGE", "30").startsWith("ERROR:"));

        assertEquals(before, Files.readAllLines(file));
        User ana = manager.getUsers().get(0);
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana@correo.com", ana.getEmail());
        assertTrue(HASHER.matches("Clave1!", ana.getPassword()));
    }

    @Test
    void updatingEmailToOwnEmailIsAllowed() {
        createAna();
        assertTrue(manager.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com").startsWith("OK:"));
    }

    @Test
    void failedSaveLeavesStateUnchanged() throws IOException {
        UserController m = UserController.getInstance();
        m.configure(new UserRepository() {
            @Override
            public List<User> load() {
                return new java.util.ArrayList<>();
            }

            @Override
            public void save(List<User> users) throws IOException {
                throw new IOException("disk full");
            }
        }, HASHER);
        assertTrue(m.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456").startsWith("ERROR:"));
        assertTrue(m.getUsers().isEmpty());
    }
}
