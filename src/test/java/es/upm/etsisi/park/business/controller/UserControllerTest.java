package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.persistence.file.FileUserRepository;
import es.upm.etsisi.park.persistence.file.UserFileStore;
import es.upm.etsisi.park.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    @TempDir
    Path dir;

    private UserRepository repository;
    private UserController service;

    @BeforeEach
    void setUp() {
        TestPark.configure(dir);
        repository = Park.getInstance().getRepositories().users();
        service = Park.getInstance().getUserController();
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
    }

    private User ana() {
        return repository.findByDni("12345678Z").orElseThrow();
    }

    private UserRepository reloaded() {
        return new FileUserRepository(new UserFileStore(dir.resolve("users.txt")));
    }

    @Test
    void createsAndPersistsValidUser() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");

        assertEquals(2, reloaded().findAll().size());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(ParkException.class, () -> service.create(
                "Pedro", "Lopez Martin", "12345678A", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(ParkException.class, () -> service.create(
                "", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(ParkException.class, () -> service.create(
                "Pedro", "Lopez Martin", "87654321X", "pedro@correo.es", "Otra2#", "623456789", "9876543210987654"));
        assertThrows(ParkException.class, () -> service.create(
                "Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "otra2#", "623456789", "9876543210987654"));
        assertThrows(ParkException.class, () -> service.create(
                "Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "98765432"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        assertThrows(ParkException.class, () -> service.create(
                "Ana", "Bis", "12345678z", "other@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(ParkException.class, () -> service.create(
                "Pedro", "Lopez Martin", "87654321X", "ANA@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void deletesWithCorrectPassword() {
        service.delete("ana@correo.com", "Clave1!");

        assertTrue(reloaded().findAll().isEmpty());
    }

    @Test
    void doesNotDeleteWithWrongPasswordOrUnknownUser() {
        assertThrows(ParkException.class, () -> service.delete("ana@correo.com", "Wrong1!"));
        assertThrows(ParkException.class, () -> service.delete("nobody@correo.com", "Clave1!"));
        assertEquals(1, reloaded().findAll().size());
    }

    @Test
    void updatesEveryAllowedField() {
        service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria");
        service.update("ana@correo.com", "Clave1!", "SURNAME", "Garcia");
        service.update("ana@correo.com", "Clave1!", "PHONE", "699999999");
        service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666");
        service.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");

        User stored = reloaded().findByDni("12345678Z").orElseThrow();
        assertEquals("Ana Maria", stored.getFirstName());
        assertEquals("Garcia", stored.getSurnames());
        assertEquals("699999999", stored.getPhone());
        assertEquals("9999888877776666", stored.getCard());
        assertTrue(TestPark.HASHER.matches("Nueva2#", stored.getPasswordHash()));
        assertEquals("ana.nueva@correo.com", stored.getEmail());
    }

    @Test
    void rejectsInvalidUpdates() {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");

        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "CARD", "1234"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Wrong1!", "PHONE", "699999999"));
        assertThrows(ParkException.class,
                () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));

        assertEquals("ana@correo.com", ana().getEmail());
        assertTrue(TestPark.HASHER.matches("Clave1!", ana().getPasswordHash()));
        assertEquals("1234567890123456", ana().getCard());
        assertEquals("612345678", ana().getPhone());
        assertEquals("12345678Z", ana().getDni());
    }

    @Test
    void allowsKeepingOwnEmail() {
        service.update("ana@correo.com", "Clave1!", "EMAIL", "ANA@correo.com");

        assertEquals("ANA@correo.com", ana().getEmail());
    }
}
