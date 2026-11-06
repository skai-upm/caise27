package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.InvalidInputException;
import es.upm.park.business.model.User;
import es.upm.park.business.security.BCryptPasswordHasher;
import es.upm.park.business.security.PasswordService;
import es.upm.park.business.validation.UserValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerTest {

    private final UserController controller = UserController.getInstance();
    private final PasswordService passwords =
            new PasswordService(new BCryptPasswordHasher(), UserValidator.passwordPolicy());
    private UserRegistry registry;

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        registry = new UserRegistry();
        controller.configure(registry, passwords);
        park.reset();
        park.setCurrentDate(java.time.LocalDate.of(2027, 4, 15));
        park.setUserDirectory(registry);
        controller.setParkController(park);
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private User ana() {
        return controller.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");
    }

    @Test
    void createsUserWithProtectedPassword() {
        User user = ana();
        assertSame(user, registry.findByEmail("ana@correo.com").orElseThrow());
        assertNotEquals("Clave1!", user.getPassword());
        assertTrue(passwords.verify("Clave1!", user.getPassword()));
        assertSame(UserController.getInstance(), controller);
    }

    @Test
    void duplicateDniOrEmailIsRejected() {
        ana();
        controller.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        assertThrows(BusinessRuleException.class, () -> controller.createUser("Otra", "Persona", "12345678Z",
                "otra@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(BusinessRuleException.class, () -> controller.createUser("Otra", "Persona", "00000000T",
                "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertThrows(BusinessRuleException.class, () -> controller.createUser("Otra", "Persona", "87654321X",
                "ana@correo.com", "Clave1!", "600000000", "1234567890123456"));
        assertEquals(2, registry.all().size());
        assertEquals("12345678Z", registry.findByEmail("ana@correo.com").orElseThrow().getDni());
    }

    @Test
    void invalidDataAddsNoUser() {
        String[][] invalid = {
            {"Ana", "Garcia", "12345678A", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"},
            {"Ana", "Garcia", "12345678Z", "ana@correo.es", "Clave1!", "612345678", "1234567890123456"},
            {"Ana", "Garcia", "12345678Z", "ana@correo.com", "clave1!", "612345678", "1234567890123456"},
            {"Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234 5678 9012 3456"},
            {"", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456"},
        };
        for (String[] d : invalid) {
            assertThrows(InvalidInputException.class,
                    () -> controller.createUser(d[0], d[1], d[2], d[3], d[4], d[5], d[6]), String.join(";", d));
        }
        assertTrue(registry.all().isEmpty());
    }

    @Test
    void deletesWithCorrectPasswordOnly() {
        ana();
        assertThrows(BusinessRuleException.class, () -> controller.deleteUser("ana@correo.com", "Otra1!"));
        assertTrue(registry.findByEmail("ana@correo.com").isPresent());
        assertThrows(es.upm.park.business.exception.NotFoundException.class,
                () -> controller.deleteUser("nadie@correo.com", "Clave1!"));
        controller.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(registry.findByEmail("ana@correo.com").isEmpty());
    }

    @Test
    void activeTicketOrLockedGroupPreventsDeletion() {
        ana();
        park.createIndividualTicket("T001", "ana@correo.com", park.getCurrentDate());
        assertThrows(BusinessRuleException.class, () -> controller.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(registry.findByEmail("ana@correo.com").isPresent());
        assertTrue(park.findTicket("T001").isPresent());
    }

    @Test
    void deletionLeavesGroupsAndKeepsHistoricalTickets() {
        ana();
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", park.getCurrentDate());
        park.setCurrentDate(park.getCurrentDate().plusDays(1));
        controller.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(registry.findByEmail("ana@correo.com").isEmpty());
        assertTrue(park.findGroup("G01").orElseThrow().getMemberEmails().isEmpty());
        assertTrue(park.findTicket("T001").isPresent(), "historical ticket kept");
    }

    @Test
    void unconfiguredControllerFails() {
        controller.configure(null, null);
        assertThrows(IllegalStateException.class, this::ana);
    }
}
