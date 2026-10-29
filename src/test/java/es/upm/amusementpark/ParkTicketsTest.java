package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkTicketsTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    private final User ana = new User("ana@correo.com");
    private final User pedro = new User("pedro@correo.com");

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(ana);
        park.registerUser(pedro);
        park.setCurrentDate("2027-04-01");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createTicketAtDefaultPrice() throws IOException {
        String out = run("TICKET CREATE;T001;ana@correo.com;2027-04-15\n");
        assertTrue(out.contains("TICKET CREATED. PRICE: 40.00 EUR"));
        IndividualTicket ticket = (IndividualTicket) park.findTicket("T001");
        assertEquals(ana, ticket.getUser());
        assertEquals("2027-04-15", ticket.getDate().toString());
    }

    @Test
    void priceFollowsSystemPriceAndIsFixedAtCreation() throws IOException {
        String out = run("TICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "SYSTEM PRICE;45.50\n"
                + "TICKET CREATE;T002;ana@correo.com;2027-04-15\n"
                + "SYSTEM PRICE;50\n");
        assertTrue(out.contains("TICKET CREATED. PRICE: 45.50 EUR"));
        assertEquals(new BigDecimal("40.00"), park.findTicket("T001").getPrice());
        assertEquals(new BigDecimal("45.50"), park.findTicket("T002").getPrice());
    }

    @Test
    void todayAllowedPastRejected() throws IOException {
        park.setCurrentDate("2027-04-15");
        String out = run("TICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "TICKET CREATE;T002;ana@correo.com;2027-04-14\n");
        assertNotNull(park.findTicket("T001"));
        assertNull(park.findTicket("T002"));
        assertTrue(out.contains("Error: date 2027-04-14 is earlier than the current date 2027-04-15"));
    }

    @Test
    void unknownUserDuplicateIdAndBadInputRejected() throws IOException {
        String out = run("TICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "TICKET CREATE;T001;pedro@correo.com;2027-04-16\n"
                + "TICKET CREATE;T002;nadie@correo.com;2027-04-15\n"
                + "TICKET CREATE;T003;ana@correo.com;15-04-2027\n"
                + "TICKET CREATE;T004;ana@correo.com\n"
                + "TICKET CREATE; ;ana@correo.com;2027-04-15\n");
        assertTrue(out.contains("Error: ticket identifier already in use: T001"));
        assertTrue(out.contains("Error: user not found: nadie@correo.com"));
        assertTrue(out.contains("Error: invalid date"));
        assertTrue(out.contains("Error: invalid arguments for TICKET CREATE"));
        assertTrue(out.contains("Error: ticket identifier must not be empty"));
        assertEquals(1, park.getTickets().size());
        assertEquals(ana, ((IndividualTicket) park.findTicket("T001")).getUser());
    }

    @Test
    void commonIdentifierCheck() {
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        assertTrue(park.isTicketIdentifierUsed("T001"));
        assertFalse(park.isTicketIdentifierUsed("T002"));
    }

    @Test
    void deleteFutureTicket() throws IOException {
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        String out = run("TICKET DELETE;T001\n");
        assertTrue(out.contains("TICKET DELETED"));
        assertNull(park.findTicket("T001"));
    }

    @Test
    void todayAndPastTicketsAreKept() throws IOException {
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.createIndividualTicket("T002", "ana@correo.com", "2027-04-15");
        park.setCurrentDate("2027-04-15");
        run("TICKET DELETE;T001\n");
        park.setCurrentDate("2027-04-20");
        String out = run("TICKET DELETE;T002\nTICKET DELETE;T999\n");
        assertEquals(2, out.split("must be kept", -1).length - 1);
        assertTrue(out.contains("Error: ticket not found: T999"));
        assertNotNull(park.findTicket("T001"));
        assertNotNull(park.findTicket("T002"));
    }

    @Test
    void ticketsOfUserByTraversal() {
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.createIndividualTicket("T002", "ana@correo.com", "2027-04-16");
        park.createIndividualTicket("T003", "pedro@correo.com", "2027-04-15");
        List<String> ids = park.ticketsOf(ana).stream().map(Ticket::getIdentifier).toList();
        assertEquals(List.of("T001", "T002"), ids);
    }

    @Test
    void activeTicketDerivedFromCurrentDate() {
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.setCurrentDate("2027-04-15");
        assertTrue(park.hasActiveTicket(ana));
        assertFalse(park.hasActiveTicket(pedro));
        park.setCurrentDate("2027-04-16");
        assertFalse(park.hasActiveTicket(ana));
    }

    @Test
    void wrongDeleteArgumentsRejected() {
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket(""));
    }

    @Test
    void helpListsTicketCommands() throws IOException {
        String out = run("HELP\n");
        assertTrue(out.contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(out.contains("TICKET DELETE;<identifier>"));
    }
}
