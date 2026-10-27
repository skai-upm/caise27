package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 1);
    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private TicketService tickets;
    private UserService users;
    private User ana;

    @BeforeEach
    void setUp() throws UserOperationException {
        UserRepository userRepository = new UserRepository();
        park.initialize(userRepository, TODAY);
        users = new UserService(userRepository);
        tickets = new TicketService();
        ana = users.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void ticketUsesCurrentPriceAndKeepsIt() throws TicketOperationException {
        IndividualTicket first = tickets.createTicket("T001", "ana@correo.com", DAY);
        assertEquals(new BigDecimal("40.00"), first.getPrice());
        assertSame(ana, first.getUser());
        park.setCurrentPrice(new BigDecimal("45.50"));
        IndividualTicket second = tickets.createTicket("T002", "ana@correo.com", DAY);
        assertEquals(new BigDecimal("45.50"), second.getPrice());
        assertEquals(new BigDecimal("40.00"), first.getPrice());
    }

    @Test
    void ticketForTodayIsAllowedButNotForPast() throws TicketOperationException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        assertThrows(TicketOperationException.class, () -> tickets.createTicket("T002", "ana@correo.com",
                TODAY.minusDays(1)));
        assertTrue(park.getTickets().findById("T002").isEmpty());
    }

    @Test
    void unknownUserAndDuplicateIdentifierAreRejected() throws TicketOperationException {
        assertThrows(TicketOperationException.class, () -> tickets.createTicket("T001", "nadie@correo.com", DAY));
        tickets.createTicket("T001", "ana@correo.com", DAY);
        assertThrows(TicketOperationException.class, () -> tickets.createTicket("T001", "ana@correo.com", DAY));
        assertEquals(1, park.getTickets().findAll().size());
    }

    @Test
    void onlyFutureTicketsCanBeDeleted() throws TicketOperationException {
        tickets.createTicket("T001", "ana@correo.com", DAY);
        tickets.createTicket("T002", "ana@correo.com", DAY);
        tickets.deleteTicket("T001");
        assertTrue(park.getTickets().findById("T001").isEmpty());
        park.setCurrentDate(DAY);
        assertThrows(TicketOperationException.class, () -> tickets.deleteTicket("T002"));
        park.setCurrentDate(DAY.plusDays(5));
        assertThrows(TicketOperationException.class, () -> tickets.deleteTicket("T002"));
        assertTrue(park.getTickets().exists("T002"));
        assertThrows(TicketOperationException.class, () -> tickets.deleteTicket("T999"));
    }

    @Test
    void activeTicketBlocksUserDeletion() throws TicketOperationException {
        tickets.createTicket("T001", "ana@correo.com", DAY);
        park.setCurrentDate(DAY);
        assertThrows(UserOperationException.class, () -> users.deleteUser("ana@correo.com", "Clave1!"));
        assertTrue(park.getUsers().findByEmail("ana@correo.com").isPresent());
    }

    @Test
    void deletingFutureTicketFirstAllowsUserDeletion() throws Exception {
        tickets.createTicket("T001", "ana@correo.com", DAY);
        tickets.deleteTicket("T001");
        users.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.getUsers().findByEmail("ana@correo.com").isEmpty());
    }

    @Test
    void historicalTicketDoesNotBlockAndKeepsUserData() throws Exception {
        tickets.createTicket("T001", "ana@correo.com", DAY);
        park.setCurrentDate(DAY.plusDays(5));
        users.deleteUser("ana@correo.com", "Clave1!");
        IndividualTicket historical = (IndividualTicket) park.getTickets().findById("T001").orElseThrow();
        assertEquals("12345678Z", historical.getUser().getDni());
    }
}
