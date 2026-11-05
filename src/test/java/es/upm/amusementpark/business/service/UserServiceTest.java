package es.upm.amusementpark.business.service;

import es.upm.amusementpark.business.exception.OperationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.amusementpark.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        service = new UserService(repo);
    }

    @Test
    void createsValidUser() {
        service.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertEquals(1, repo.count());
        assertEquals("Garcia Lopez", repo.findByDni("12345678Z").orElseThrow().getSurnames());
    }

    @Test
    void rejectsInvalidFields() {
        assertThrows(OperationException.class, () -> service.createUser("A", "B", "12345678A", "ana@correo.com", "Clave1!", "6", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.createUser("A", "B", "12345678Z", "ana@correo.es", "Clave1!", "6", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.createUser("A", "B", "12345678Z", "ana@correo.com", "Clave12", "6", "1234567890123456"));
        assertThrows(OperationException.class, () -> service.createUser("A", "B", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234 5678 9012 3456"));
        assertEquals(0, repo.count());
    }

    @Test
    void rejectsDuplicateDniAndEmail() {
        service.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertThrows(OperationException.class, () -> service.createUser("Luis", "Martin", "12345678Z", "luis@correo.com", "Clave1!", "6", "1111222233334444"));
        assertThrows(OperationException.class, () -> service.createUser("Luis", "Martin", "87654321X", "ana@correo.com", "Clave1!", "6", "1111222233334444"));
        assertEquals(1, repo.count());
        assertEquals("ana@correo.com", repo.findByDni("12345678Z").orElseThrow().getEmail());
    }
}
