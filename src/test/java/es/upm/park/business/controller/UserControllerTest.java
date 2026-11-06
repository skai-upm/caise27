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
    void updatesEachFieldAfterAuthentication() {
        User user = ana();
        controller.updateUser("ana@correo.com", "Clave1!", es.upm.park.business.model.UserField.NAME, "Ana Maria");
        controller.updateUser("ana@correo.com", "Clave1!", es.upm.park.business.model.UserField.SURNAME, "Garcia");
        controller.updateUser("ana@correo.com", "Clave1!", es.upm.park.business.model.UserField.PHONE, "699999999");
        controller.updateUser("ana@correo.com", "Clave1!", es.upm.park.business.model.UserField.CARD,
                "9999888877776666");
        controller.updateUser("ana@correo.com", "Clave1!", es.upm.park.business.model.UserField.PASSWORD, "Nueva2#");
        assertEquals("Ana Maria", user.getFirstName());
        assertEquals("Garcia", user.getSurnames());
        assertEquals("699999999", user.getPhone());
        assertEquals("9999888877776666", user.getCard());
        assertTrue(passwords.verify("Nueva2#", user.getPassword()));
        assertThrows(BusinessRuleException.class, () -> controller.updateUser("ana@correo.com", "Clave1!",
                es.upm.park.business.model.UserField.NAME, "X"), "old password no longer valid");
        assertEquals("12345678Z", user.getDni());
    }

    @Test
    void invalidUpdatesChangeNothing() {
        User user = ana();
        controller.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        String stored = user.getPassword();
        assertThrows(BusinessRuleException.class, () -> controller.updateUser("ana@correo.com", "Mala1!",
                es.upm.park.business.model.UserField.NAME, "X"));
        assertThrows(InvalidInputException.class, () -> controller.updateUser("ana@correo.com", "Clave1!",
                es.upm.park.business.model.UserField.EMAIL, "ana@correo.es"));
        assertThrows(BusinessRuleException.class, () -> controller.updateUser("ana@correo.com", "Clave1!",
                es.upm.park.business.model.UserField.EMAIL, "pedro@correo.com"));
        assertThrows(InvalidInputException.class, () -> controller.updateUser("ana@correo.com", "Clave1!",
                es.upm.park.business.model.UserField.PASSWORD, "clave1!"));
        assertThrows(InvalidInputException.class, () -> controller.updateUser("ana@correo.com", "Clave1!",
                es.upm.park.business.model.UserField.CARD, "1234"));
        assertThrows(InvalidInputException.class, () -> controller.updateUser("ana@correo.com", "Clave1!",
                es.upm.park.business.model.UserField.NAME, " "));
        assertEquals("Ana", user.getFirstName());
        assertEquals("ana@correo.com", user.getEmail());
        assertEquals(stored, user.getPassword());
        assertEquals("1234567890123456", user.getCard());
        assertThrows(InvalidInputException.class, () -> es.upm.park.business.model.UserField.parse("DNI"));
        assertThrows(InvalidInputException.class, () -> es.upm.park.business.model.UserField.parse("AGE"));
    }

    @Test
    void emailChangeMovesParkRecords() {
        ana();
        controller.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createIndividualTicket("T001", "ana@correo.com", park.getCurrentDate());
        park.buySpeedPasses("SP01", "T001", 1);
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", park.getCurrentDate());
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.enterAttraction("A01", "T001");
        controller.updateUser("ana@correo.com", "Clave1!", es.upm.park.business.model.UserField.EMAIL,
                "ana.nueva@correo.com");
        String nueva = "ana.nueva@correo.com";
        assertTrue(registry.findByEmail("ana@correo.com").isEmpty());
        assertEquals("12345678Z", registry.findByEmail(nueva).orElseThrow().getDni());
        assertEquals(java.util.List.of("pedro@correo.com", nueva),
                java.util.List.copyOf(park.findGroup("G01").orElseThrow().getMemberEmails()));
        assertEquals(java.util.List.of("T001", "TG01:12345678Z"),
                park.ticketsOf(nueva).stream().map(t -> t.getId()).toList());
        assertTrue(park.ticketsOf("ana@correo.com").isEmpty());
        assertTrue(park.findAttraction("A01").orElseThrow().isInside(nueva));
        assertEquals(1, park.passesOf(nueva).size());
        InvoiceController invoices = InvoiceController.getInstance();
        invoices.setParkController(park);
        assertTrue(invoices.userInvoice(nueva).render().contains("T001"));
    }

    @Test
    void unconfiguredControllerFails() {
        controller.configure(null, null);
        assertThrows(IllegalStateException.class, this::ana);
    }
}
