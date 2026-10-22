package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.persistence.InMemoryUserRepository;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private InMemoryUserRepository repository;
    private UserService service;

    @BeforeEach
    void setUp() throws IOException {
        repository = new InMemoryUserRepository();
        service = new UserService(repository);
    }

    private void createAna() throws UserException {
        service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    private void createPedro() throws UserException {
        service.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void createsAndPersistsValidUser() throws UserException {
        createAna();
        User ana = service.findByEmail("ana@correo.com");
        assertNotNull(ana);
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals(1, repository.getSaved().size());
        assertEquals("12345678Z", repository.getSaved().get(0).getDni());
    }

    @Test
    void normalizesDniLetterToUpperCase() throws UserException {
        service.create("Ana", "Garcia", "12345678z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertEquals("12345678Z", service.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void rejectsInvalidData() {
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia", "12345678A", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia", "12345678Z", "ana@correo.es",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia", "12345678Z", "ana@correo.com",
                "clave1!", "6", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "123456789012345"));
        assertThrows(UserException.class, () -> service.create("", "Garcia", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", " ", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(UserException.class, () -> service.create("Ana", "Garcia", "12345678Z", "ana@correo.com",
                "Clave1!", "", "1234567890123456"));
        assertTrue(service.getUsers().isEmpty());
        assertEquals(0, repository.getSaveCount());
    }

    @Test
    void rejectsDuplicateDni() throws UserException {
        createAna();
        UserException e = assertThrows(UserException.class, () -> service.create("Otra", "Persona", "12345678z",
                "otra@correo.com", "Clave1!", "6", "1234567890123456"));
        assertTrue(e.getMessage().contains("DNI"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws UserException {
        createAna();
        UserException e = assertThrows(UserException.class, () -> service.create("Otra", "Persona", "87654321X",
                "ANA@correo.com", "Clave1!", "6", "1234567890123456"));
        assertTrue(e.getMessage().contains("email"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void deletesWithCorrectPassword() throws UserException {
        createAna();
        createPedro();
        service.delete("pedro@correo.com", "Otra2#");
        assertNull(service.findByEmail("pedro@correo.com"));
        assertEquals(1, repository.getSaved().size());
        assertEquals("ana@correo.com", repository.getSaved().get(0).getEmail());
    }

    @Test
    void deleteFindsEmailIgnoringCase() throws UserException {
        createAna();
        service.delete("ANA@Correo.com", "Clave1!");
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void deleteRejectsWrongPassword() throws UserException {
        createPedro();
        assertThrows(UserException.class, () -> service.delete("pedro@correo.com", "Wrong1!"));
        assertNotNull(service.findByEmail("pedro@correo.com"));
        assertEquals(1, repository.getSaved().size());
    }

    @Test
    void deleteRejectsUnknownEmail() {
        UserException e = assertThrows(UserException.class, () -> service.delete("nobody@correo.com", "Clave1!"));
        assertTrue(e.getMessage().contains("nobody@correo.com"));
    }

    @Test
    void updatesEachModifiableField() throws UserException {
        createAna();
        service.update("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        service.update("ana@correo.com", "Clave1!", UserField.SURNAME, "Garcia Ruiz");
        service.update("ana@correo.com", "Clave1!", UserField.PHONE, "699999999");
        service.update("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        service.update("ana@correo.com", "Nueva2#", UserField.EMAIL, "ana.nueva@correo.com");

        assertNull(service.findByEmail("ana@correo.com"));
        User ana = service.findByEmail("ana.nueva@correo.com");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia Ruiz", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("Nueva2#", ana.getPassword());
        assertEquals("12345678Z", ana.getDni());
        assertEquals("ana.nueva@correo.com", repository.getSaved().get(0).getEmail());
    }

    @Test
    void updateKeepsUserOrder() throws UserException {
        createAna();
        createPedro();
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertEquals("ana.nueva@correo.com", service.getUsers().get(0).getEmail());
        assertEquals("pedro@correo.com", service.getUsers().get(1).getEmail());
    }

    @Test
    void updateRejectsInvalidValues() throws UserException {
        createAna();
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", UserField.CARD, "1234"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.PASSWORD, "clave1!"));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", UserField.NAME, " "));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", UserField.SURNAME, ""));
        assertThrows(UserException.class, () -> service.update("ana@correo.com", "Clave1!", UserField.PHONE, ""));
        User ana = service.findByEmail("ana@correo.com");
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals(1, repository.getSaveCount());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() throws UserException {
        createAna();
        createPedro();
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "PEDRO@correo.com"));
        assertNotNull(service.findByEmail("ana@correo.com"));
    }

    @Test
    void updateAllowsChangingCaseOfOwnEmail() throws UserException {
        createAna();
        service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "Ana@correo.com");
        assertEquals("Ana@correo.com", service.findByEmail("ana@correo.com").getEmail());
    }

    @Test
    void updateRejectsWrongCredentials() throws UserException {
        createAna();
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Wrong1!", UserField.PHONE, "699999999"));
        assertThrows(UserException.class,
                () -> service.update("nobody@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        assertEquals("612345678", service.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void failedSaveLeavesStateUnchanged() throws UserException {
        createAna();
        repository.setFailOnSave(true);
        assertThrows(UserException.class, this::createPedro);
        assertNull(service.findByEmail("pedro@correo.com"));
        assertEquals(1, service.getUsers().size());
        assertThrows(UserException.class,
                () -> service.update("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com"));
        assertNotNull(service.findByEmail("ana@correo.com"));
        assertThrows(UserException.class, () -> service.delete("ana@correo.com", "Clave1!"));
        assertEquals(1, service.getUsers().size());
    }

    @Test
    void loadsUsersFromRepository() throws IOException, UserException {
        createAna();
        createPedro();
        UserService reloaded = new UserService(repository);
        assertEquals(2, reloaded.getUsers().size());
        assertNotNull(reloaded.findByEmail("pedro@correo.com"));
        assertNull(reloaded.findByEmail("nobody@correo.com"));
    }
}
