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
}
