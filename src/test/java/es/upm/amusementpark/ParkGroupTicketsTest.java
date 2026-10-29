package es.upm.amusementpark;

import es.upm.amusementpark.business.exception.ParkException;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

class ParkGroupTicketsTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    private final User ana = new User("ana@correo.com", "12345678Z");
    private final User pedro = new User("pedro@correo.com", "87654321X");

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(ana);
        park.registerUser(pedro);
        park.setCurrentDate("2027-04-01");
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createGroupTicketWithDerivedIdentifiers() throws IOException {
        String out = run("GROUPTICKET CREATE;TG01;G01;2027-04-15\n");
        assertTrue(out.contains("GROUP TICKET CREATED. TOTAL PRICE: 80.00 EUR"));
        GroupTicket ticket = (GroupTicket) park.findTicket("TG01");
        List<String> ids = ticket.getComposition().stream().map(Ticket::getIdentifier).toList();
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"), ids);
        assertSame(ana, ticket.getComposition().get(0).getUser());
        assertSame(park.findGroup("G01"), ticket.getGroup());
    }

    @Test
    void discountUsesCurrentPrice() throws IOException {
        for (int i = 0; i < 4; i++) {
            User user = new User("m" + i + "@correo.com", "M" + i);
            park.registerUser(user);
            park.addUserToGroup("G01", user.getEmail());
        }
        String out = run("SYSTEM PRICE;40\nGROUPTICKET CREATE;TG02;G01;2027-04-15\n");
        assertTrue(out.contains("TOTAL PRICE: 192.00 EUR"));
    }

    @Test
    void emptyGroupAndPastDateRejected() throws IOException {
        park.createGroup("G02", "Vacio");
        park.setCurrentDate("2027-04-15");
        String out = run("GROUPTICKET CREATE;TG01;G02;2027-04-15\n"
                + "GROUPTICKET CREATE;TG02;G01;2027-04-14\n"
                + "GROUPTICKET CREATE;TG03;G99;2027-04-15\n");
        assertTrue(out.contains("Error: group G02 has no members"));
        assertTrue(out.contains("Error: date 2027-04-14 is earlier"));
        assertTrue(out.contains("Error: group not found: G99"));
        assertTrue(park.getTickets().isEmpty());
    }

    @Test
    void identifierConflictCreatesNothing() {
        park.createIndividualTicket("TG01:12345678Z", "ana@correo.com", "2027-04-15");
        assertThrows(ParkException.class,
                () -> park.createGroupTicket("TG01", "G01", "2027-04-15"));
        assertNull(park.findTicket("TG01"));
        assertFalse(park.isTicketIdentifierUsed("TG01:87654321X"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void groupTicketIdClashWithIndividualTicket() {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(ParkException.class,
                () -> park.createIndividualTicket("TG01", "ana@correo.com", "2027-04-15"));
        assertThrows(ParkException.class,
                () -> park.createIndividualTicket("TG01:87654321X", "ana@correo.com", "2027-04-15"));
    }

    @Test
    void activeGroupTicketLocksGroup() throws IOException {
        User luis = new User("luis@correo.com", "11111111H");
        park.registerUser(luis);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        String out = run("GROUP ADD;G01;luis@correo.com\n"
                + "GROUP REMOVE;G01;ana@correo.com\n"
                + "GROUP DELETE;G01\n");
        assertEquals(2, out.split("cannot be modified while it has an active group ticket", -1).length - 1);
        assertTrue(out.contains("has a group ticket for the current date or later"));
        assertNotNull(park.findGroup("G01"));
        assertEquals(2, park.findGroup("G01").getMembers().size());
    }

    @Test
    void deletingFutureTicketUnlocksGroup() throws IOException {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        String out = run("GROUPTICKET DELETE;TG01\nGROUP REMOVE;G01;pedro@correo.com\n");
        assertTrue(out.contains("GROUP TICKET DELETED"));
        assertTrue(out.contains("USER REMOVED FROM GROUP"));
        assertNull(park.findTicket("TG01"));
        assertFalse(park.isTicketIdentifierUsed("TG01:12345678Z"));
    }

    @Test
    void todaysGroupTicketKeptAndHistoricalUnlocks() throws IOException {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-15");
        String out = run("GROUPTICKET DELETE;TG01\nGROUPTICKET DELETE;TG99\n");
        assertTrue(out.contains("must be kept"));
        assertTrue(out.contains("Error: group ticket not found: TG99"));
        assertTrue(park.hasActiveGroupTicket(park.findGroup("G01")));
        park.setCurrentDate("2027-04-16");
        assertFalse(park.hasActiveGroupTicket(park.findGroup("G01")));
    }

    @Test
    void compositionFixedAfterGroupChanges() {
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        User luis = new User("luis@correo.com", "11111111H");
        park.registerUser(luis);
        park.addUserToGroup("G01", "luis@correo.com");
        assertEquals(2, ticket.getComposition().size());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void composedTicketAndGroupTicketNotDeletableWithTicketDelete() throws IOException {
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        String out = run("TICKET DELETE;TG01:12345678Z\nTICKET DELETE;TG01\n");
        assertTrue(out.contains("belongs to group ticket TG01 and cannot be deleted directly"));
        assertTrue(out.contains("is a group ticket; use GROUPTICKET DELETE"));
        assertEquals(2, ((GroupTicket) park.findTicket("TG01")).getComposition().size());
    }

    @Test
    void deletingGroupKeepsHistoricalTicketsWithNullGroup() {
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
        assertSame(ticket, park.findTicket("TG01"));
        assertNull(ticket.getGroup());
        assertEquals(2, ticket.getComposition().size());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void userDeletionCheckCoversGroupTickets() {
        User luis = new User("luis@correo.com", "11111111H");
        park.registerUser(luis);
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.setCurrentDate("2027-04-16");
        park.addUserToGroup("G01", "luis@correo.com");
        park.setCurrentDate("2027-04-01");
        assertTrue(park.hasActiveTicket(ana));
        assertFalse(park.canDeleteUser(ana));
        assertFalse(park.hasActiveTicket(luis));
        assertFalse(park.canDeleteUser(luis));
        park.setCurrentDate("2027-04-16");
        assertTrue(park.canDeleteUser(ana));
        assertTrue(park.canDeleteUser(luis));
    }

    @Test
    void helpListsGroupTicketCommands() throws IOException {
        String out = run("HELP\n");
        assertTrue(out.contains("GROUPTICKET CREATE;<identifier>;<group>;<date>"));
        assertTrue(out.contains("GROUPTICKET DELETE;<identifier>"));
    }
}
