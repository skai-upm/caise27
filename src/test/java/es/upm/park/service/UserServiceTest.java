package es.upm.park.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.model.User;
import es.upm.park.repository.UserRepository;

class UserServiceTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        service = new UserService(repo);
    }

    private User createAna() {
        return service.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com",
                "Clave1!", "612345678", "1234567890123456");
    }

    private void assertRejected(String message, Runnable action) {
        int before = repo.findAll().size();
        UserException e = assertThrows(UserException.class, action::run);
        assertEquals(message, e.getMessage());
        assertEquals(before, repo.findAll().size());
    }

    @Test
    void createsValidUserWithExactValues() {
        User user = createAna();
        assertSame(user, repo.findByDni("12345678Z").orElseThrow());
        assertEquals("Ana", user.getFirstName());
        assertEquals("Garcia Lopez", user.getSurnames());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals("Clave1!", user.getPassword());
        assertEquals("612345678", user.getTelephone());
        assertEquals("1234567890123456", user.getCardNumber());
    }

    @Test
    void rejectsInvalidFields() {
        assertRejected("invalid DNI", () -> service.create("Ana", "G", "12345678A", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertRejected("invalid email", () -> service.create("Ana", "G", "12345678Z", "ana@correo.es",
                "Clave1!", "6", "1234567890123456"));
        assertRejected("invalid password", () -> service.create("Ana", "G", "12345678Z", "ana@correo.com",
                "clave1!", "6", "1234567890123456"));
        assertRejected("invalid card number", () -> service.create("Ana", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "1234 5678 9012 3456"));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void rejectsDuplicateDni() {
        User ana = createAna();
        assertRejected("a user with DNI 12345678Z already exists", () -> service.create("Luis", "Perez",
                "12345678Z", "luis@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertSame(ana, repo.findAll().get(0));
        assertEquals("ana@correo.com", ana.getEmail());
    }

    @Test
    void rejectsDuplicateEmail() {
        createAna();
        assertRejected("a user with email ana@correo.com already exists", () -> service.create("Luis", "Perez",
                "00000000T", "ana@correo.com", "Clave1!", "600000000", "1111222233334444"));
        assertEquals(false, repo.findByDni("00000000T").isPresent());
    }

    @Test
    void deletesUserWithCorrectCredentials() {
        User ana = createAna();
        assertSame(ana, service.delete("ana@correo.com", "Clave1!"));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void wrongPasswordDoesNotDelete() {
        User ana = createAna();
        assertRejected("incorrect password", () -> service.delete("ana@correo.com", "Otra2#"));
        assertSame(ana, repo.findAll().get(0));
        assertEquals("Clave1!", ana.getPassword());
    }

    @Test
    void unknownEmailDoesNotDelete() {
        createAna();
        assertRejected("no user with email nadie@correo.com", () -> service.delete("nadie@correo.com", "Clave1!"));
    }

    @Test
    void updatesEachModifiableField() {
        createAna();
        assertEquals("Ana Maria", service.update("ana@correo.com", "Clave1!", "NAME", "Ana Maria").getFirstName());
        assertEquals("Ruiz", service.update("ana@correo.com", "Clave1!", "SURNAME", "Ruiz").getSurnames());
        assertEquals("699", service.update("ana@correo.com", "Clave1!", "PHONE", "699").getTelephone());
        assertEquals("9999888877776666",
                service.update("ana@correo.com", "Clave1!", "CARD", "9999888877776666").getCardNumber());
        assertEquals("Nueva2#", service.update("ana@correo.com", "Clave1!", "PASSWORD", "Nueva2#").getPassword());
        User u = service.update("ana@correo.com", "Nueva2#", "EMAIL", "ana.nueva@correo.com");
        assertEquals("12345678Z", u.getDni());
        assertSame(u, repo.findByEmail("ana.nueva@correo.com").orElseThrow());
        assertEquals(false, repo.findByEmail("ana@correo.com").isPresent());
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void ownEmailCanBeKept() {
        createAna();
        assertEquals("ana@correo.com", service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.com").getEmail());
    }

    @Test
    void rejectsNonModifiableFieldsAndBadCredentials() {
        User ana = createAna();
        assertRejected("field DNI cannot be modified", () -> service.update("ana@correo.com", "Clave1!", "DNI", "87654321X"));
        assertRejected("field AGE cannot be modified", () -> service.update("ana@correo.com", "Clave1!", "AGE", "30"));
        assertRejected("incorrect password", () -> service.update("ana@correo.com", "Otra2#", "NAME", "X"));
        assertRejected("no user with email nadie@correo.com", () -> service.update("nadie@correo.com", "Clave1!", "NAME", "X"));
        assertSame(ana, repo.findAll().get(0));
        assertEquals("Ana", ana.getFirstName());
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void rejectsInvalidOrDuplicateValues() {
        User ana = createAna();
        service.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        assertRejected("invalid email", () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "ana@correo.es"));
        assertRejected("a user with email pedro@correo.com already exists",
                () -> service.update("ana@correo.com", "Clave1!", "EMAIL", "pedro@correo.com"));
        assertRejected("invalid password", () -> service.update("ana@correo.com", "Clave1!", "PASSWORD", "clave1!"));
        assertRejected("invalid card number", () -> service.update("ana@correo.com", "Clave1!", "CARD", "123456789012345"));
        assertSame(ana, repo.findByDni("12345678Z").orElseThrow());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("1234567890123456", ana.getCardNumber());
    }
}
