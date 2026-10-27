package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private UserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
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
}
