package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.repository.UserRepository;

class UserServiceTest {

    private final UserRepository repository = new UserRepository();
    private final UserService service = new UserService(repository);

    private void create(String dni, String email, String password, String card) throws UserOperationException {
        service.createUser("Ana", "Garcia Lopez", dni, email, password, "612345678", card);
    }

    @Test
    void validUserIsStored() throws UserOperationException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        assertEquals("ana@correo.com", repository.findByDni("12345678Z").orElseThrow().getEmail());
    }

    private void assertRejected(String expectedReason, String dni, String email, String password, String card) {
        UserOperationException e = assertThrows(UserOperationException.class, () -> create(dni, email, password, card));
        assertTrue(e.getMessage().contains(expectedReason), e.getMessage());
    }

    @Test
    void invalidFieldsAreRejectedAndNothingIsStored() {
        assertRejected("DNI", "12345678A", "ana@correo.com", "Clave1!", "1234567890123456");
        assertRejected("email", "12345678Z", "ana@correo.es", "Clave1!", "1234567890123456");
        assertRejected("password", "12345678Z", "ana@correo.com", "clave1!", "1234567890123456");
        assertRejected("card", "12345678Z", "ana@correo.com", "Clave1!", "1234 5678 9012 3456");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void duplicateDniOrEmailIsRejected() throws UserOperationException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        assertRejected("DNI", "12345678Z", "otra@correo.com", "Clave1!", "1234567890123456");
        assertRejected("email", "87654321X", "ana@correo.com", "Clave1!", "1234567890123456");
        assertEquals(1, repository.findAll().size());
    }
}
