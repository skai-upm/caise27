package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.persistence.InMemoryUserStorage;
import es.upm.amusementpark.repository.UserRepository;

class UserServiceTest {

    private final UserRepository repository = new UserRepository();
    private final InMemoryUserStorage storage = new InMemoryUserStorage();
    private final UserService service = new UserService(repository, storage);

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

    @Test
    void createdUsersAreSaved() throws UserOperationException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        assertEquals(repository.findAll(), storage.saved());
    }

    @Test
    void failedSaveLeavesRepositoryUnchanged() {
        storage.setFailing(true);
        assertRejected("Could not save", "12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void deleteWithCorrectPasswordRemovesAndSaves() throws UserOperationException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        create("87654321X", "pedro@correo.com", "Otra2#", "9876543210987654");
        service.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(repository.findByDni("12345678Z").isEmpty());
        assertEquals(1, storage.saved().size());
        assertEquals("87654321X", storage.saved().get(0).getDni());
    }

    @Test
    void deleteOfUnknownEmailOrWithWrongPasswordIsRejected() throws UserOperationException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        UserOperationException notFound = assertThrows(UserOperationException.class,
                () -> service.deleteUser("nadie@correo.com", "Clave1!"));
        assertTrue(notFound.getMessage().contains("not found"));
        UserOperationException wrong = assertThrows(UserOperationException.class,
                () -> service.deleteUser("ana@correo.com", "Otra1!"));
        assertTrue(wrong.getMessage().contains("Incorrect password"));
        assertEquals(1, repository.findAll().size());
        assertEquals(1, storage.saved().size());
    }
}
