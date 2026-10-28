package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;
import es.upm.amusementpark.persistence.repository.UserRepository;

class UserServiceTest {

    private final UserRepository repository = new UserRepository();
    private final InMemoryUserStorage storage = new InMemoryUserStorage();
    private final UserService service = UserService.getInstance().configure(repository, storage);

    private void create(String dni, String email, String password, String card) throws ParkException {
        service.createUser("Ana", "Garcia Lopez", dni, email, password, "612345678", card);
    }

    @Test
    void validUserIsStored() throws ParkException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        assertEquals("ana@correo.com", repository.findByDni("12345678Z").orElseThrow().getEmail());
    }

    private void assertRejected(String expectedReason, String dni, String email, String password, String card) {
        ParkException e = assertThrows(ParkException.class, () -> create(dni, email, password, card));
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
    void duplicateDniOrEmailIsRejected() throws ParkException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        assertRejected("DNI", "12345678Z", "otra@correo.com", "Clave1!", "1234567890123456");
        assertRejected("email", "87654321X", "ana@correo.com", "Clave1!", "1234567890123456");
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void createdUsersAreSaved() throws ParkException {
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
    void deleteWithCorrectPasswordRemovesAndSaves() throws ParkException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        create("87654321X", "pedro@correo.com", "Otra2#", "9876543210987654");
        service.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(repository.findByDni("12345678Z").isEmpty());
        assertEquals(1, storage.saved().size());
        assertEquals("87654321X", storage.saved().get(0).getDni());
    }

    @Test
    void deleteOfUnknownEmailOrWithWrongPasswordIsRejected() throws ParkException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        ParkException notFound = assertThrows(ParkException.class,
                () -> service.deleteUser("nadie@correo.com", "Clave1!"));
        assertTrue(notFound.getMessage().contains("not found"));
        ParkException wrong = assertThrows(ParkException.class,
                () -> service.deleteUser("ana@correo.com", "Otra1!"));
        assertTrue(wrong.getMessage().contains("Incorrect password"));
        assertEquals(1, repository.findAll().size());
        assertEquals(1, storage.saved().size());
    }

    private void createAnaAndPedro() throws ParkException {
        create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456");
        service.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void updateEachModifiableField() throws ParkException {
        createAnaAndPedro();
        service.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.updateUser("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia");
        service.updateUser("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        service.updateUser("ana.nueva@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        var ana = repository.findByDni("12345678Z").orElseThrow();
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCardNumber());
        assertEquals("ana.nueva@correo.com", ana.getEmail());
        assertEquals("Nueva2#", ana.getPassword());
        assertEquals("12345678Z", storage.saved().get(0).getDni());
        assertEquals("Nueva2#", storage.saved().get(0).getPassword());
        assertEquals("87654321X", storage.saved().get(1).getDni());
    }

    private void assertUpdateRejected(String reason, String email, String password, UserField field, String value) {
        ParkException e = assertThrows(ParkException.class,
                () -> service.updateUser(email, password, field, value));
        assertTrue(e.getMessage().contains(reason), e.getMessage());
    }

    @Test
    void invalidUpdatesAreRejectedAndNothingChanges() throws ParkException {
        createAnaAndPedro();
        assertUpdateRejected("Incorrect password", "ana@correo.com", "Mala1!", UserField.PHONE, "6");
        assertUpdateRejected("not found", "nadie@correo.com", "Clave1!", UserField.PHONE, "6");
        assertUpdateRejected("already exists", "ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com");
        assertUpdateRejected("Invalid email", "ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo");
        assertUpdateRejected("Invalid password", "ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!");
        assertUpdateRejected("Invalid card", "ana@correo.com", "Clave1!", UserField.CARD, "123456789012345");
        var ana = repository.findByDni("12345678Z").orElseThrow();
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCardNumber());
        assertEquals("612345678", ana.getPhone());
    }

    @Test
    void keepingOwnEmailIsAllowed() throws ParkException {
        createAnaAndPedro();
        service.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.com");
        assertEquals("ana@correo.com", repository.findByDni("12345678Z").orElseThrow().getEmail());
    }

    @Test
    void failedSaveLeavesUserUnchanged() throws ParkException {
        createAnaAndPedro();
        storage.setFailing(true);
        assertUpdateRejected("Could not save", "ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        assertEquals("612345678", repository.findByDni("12345678Z").orElseThrow().getPhone());
    }
}
