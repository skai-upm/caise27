package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private UserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
        ParkController.getInstance().initialize(repository, LocalDate.of(2026, 10, 27));
        service = new UserService(repository);
    }

    private void createAna() throws UserOperationException {
        service.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    @Test
    void validUserIsCreated() throws UserOperationException {
        createAna();
        User ana = repository.findByEmail("ana@correo.com").orElseThrow();
        assertEquals("12345678Z", ana.getDni());
        assertEquals("612345678", ana.getPhone());
    }

    @Test
    void invalidDniIsRejected() {
        UserOperationException e = assertThrows(UserOperationException.class, () -> service.createUser(
                "Ana", "Garcia Lopez", "12345678A", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void invalidPasswordIsRejected() {
        UserOperationException e = assertThrows(UserOperationException.class, () -> service.createUser(
                "Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "clave1!", "612345678", "1234567890123456"));
        assertTrue(e.getMessage().contains("password"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void duplicateDniIsRejected() throws UserOperationException {
        createAna();
        UserOperationException e = assertThrows(UserOperationException.class, () -> service.createUser(
                "Otra", "Persona", "12345678Z", "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void duplicateEmailIsRejected() throws UserOperationException {
        createAna();
        UserOperationException e = assertThrows(UserOperationException.class, () -> service.createUser(
                "Pedro", "Lopez Martin", "87654321X", "ana@correo.com", "Otra2#", "623456789", "9876543210987654"));
        assertTrue(e.getMessage().contains("email"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void userIsDeletedWithCorrectPassword() throws UserOperationException {
        createAna();
        service.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(repository.findByEmail("ana@correo.com").isEmpty());
    }

    @Test
    void deletingUnknownUserIsRejected() {
        UserOperationException e = assertThrows(UserOperationException.class,
                () -> service.deleteUser("nadie@correo.com", "Clave1!"));
        assertTrue(e.getMessage().contains("No user"));
    }

    @Test
    void incorrectPasswordDoesNotDelete() throws UserOperationException {
        createAna();
        UserOperationException e = assertThrows(UserOperationException.class,
                () -> service.deleteUser("ana@correo.com", "Otra1!"));
        assertTrue(e.getMessage().contains("password"));
        assertTrue(repository.findByEmail("ana@correo.com").isPresent());
    }

    private User ana() {
        return repository.findByEmail("ana@correo.com").orElseThrow();
    }

    @Test
    void eachModifiableFieldCanBeUpdated() throws UserOperationException {
        createAna();
        service.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.updateUser("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        User ana = ana();
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("Nueva2#", ana.getPassword());
        service.updateUser("ana@correo.com", "Nueva2#", UserField.EMAIL, "ana.nueva@correo.com");
        assertTrue(repository.findByEmail("ana@correo.com").isEmpty());
        assertEquals("12345678Z", repository.findByEmail("ana.nueva@correo.com").orElseThrow().getDni());
    }

    @Test
    void updateRequiresCorrectPassword() throws UserOperationException {
        createAna();
        assertThrows(UserOperationException.class,
                () -> service.updateUser("ana@correo.com", "Otra1!", UserField.PHONE, "699999999"));
        assertEquals("612345678", ana().getPhone());
    }

    @Test
    void updateOfUnknownUserIsRejected() {
        assertThrows(UserOperationException.class,
                () -> service.updateUser("nadie@correo.com", "Clave1!", UserField.PHONE, "699999999"));
    }

    @Test
    void invalidNewValuesAreRejected() throws UserOperationException {
        createAna();
        assertThrows(UserOperationException.class,
                () -> service.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(UserOperationException.class,
                () -> service.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserOperationException.class,
                () -> service.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "123456789012345"));
        User ana = ana();
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCard());
    }

    @Test
    void emailOfAnotherUserIsRejected() throws UserOperationException {
        createAna();
        service.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        UserOperationException e = assertThrows(UserOperationException.class,
                () -> service.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertTrue(e.getMessage().contains("email"));
        assertEquals("12345678Z", ana().getDni());
    }

    @Test
    void keepingOwnEmailIsAllowed() throws UserOperationException {
        createAna();
        service.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertEquals(1, repository.findAll().size());
    }
}
