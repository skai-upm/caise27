package es.upm.amusementpark.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.cli.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupTicketTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private ParkController park;

    /** Users u0..u19 with e-mail uN@x.com and DNI DNI-N. */
    private static final UserDirectory USERS = new UserDirectory() {
        @Override
        public boolean exists(String email) {
            return email.startsWith("u") && email.endsWith("@x.com");
        }

        @Override
        public String dniOf(String email) {
            return exists(email) ? "DNI-" + email.substring(1, email.indexOf('@')) : null;
        }
    };

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(USERS);
    }

    private void group(String id, int members) {
        park.createGroup(id, "Group " + id);
        for (int i = 0; i < members; i++) {
            park.addUserToGroup(id, "u" + i + "@x.com");
        }
    }

    @ParameterizedTest
    @CsvSource({"1,40.00", "2,40.00", "3,36.00", "5,36.00", "6,32.00", "10,32.00", "11,20.00"})
    void discountBrackets(int members, String expected) {
        assertEquals(new BigDecimal(expected), GroupTicket.memberPrice(new BigDecimal("40.00"), members));
    }

    @Test
    void sixMembersAt40Cost192() {
        group("G01", 6);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertEquals(6, ticket.getComponents().size());
        for (IndividualTicket component : ticket.getComponents()) {
            assertEquals(new BigDecimal("32.00"), component.getPrice());
            assertSame(ticket, component.getContainingGroupTicket());
        }
    }

    @Test
    void componentsHaveDerivedIdentifiersAndAreRegistered() {
        group("G01", 2);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(List.of("TG01:DNI-0", "TG01:DNI-1"), ticket.getComponents().stream().map(Ticket::getId).toList());
        assertEquals("u1@x.com", ((IndividualTicket) park.findTicket("TG01:DNI-1")).getUserEmail());
        assertSame(park.findGroup("G01"), ticket.getGroup());
        assertThrows(ParkException.class, () -> park.createIndividualTicket("TG01:DNI-0", "u5@x.com", TODAY));
    }

    @Test
    void emptyMissingGroupAndPastDateRejected() {
        park.createGroup("G02", "Empty");
        group("G01", 1);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG02", "G02", TODAY));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG03", "G99", TODAY));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG04", "G01", TODAY.minusDays(1)));
        assertTrue(park.getTickets().isEmpty());
    }

    @Test
    void derivedIdentifierConflictCreatesNothing() {
        group("G01", 2);
        park.createIndividualTicket("TG01:DNI-1", "u1@x.com", TODAY.plusDays(5));
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", TODAY.plusDays(5)));
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:DNI-0"));
        assertEquals(1, park.getTickets().size());
    }

    @Test
    void groupTicketIdentifierConflictRejected() {
        group("G01", 1);
        park.createIndividualTicket("TG01", "u0@x.com", TODAY);
        assertThrows(ParkException.class, () -> park.createGroupTicket("TG01", "G01", TODAY));
        assertTrue(park.findTicket("TG01") instanceof IndividualTicket);
    }

    @Test
    void compositionFixedAndPriceStable() {
        group("G01", 2);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY.minusDays(0));
        park.setCurrentDate(TODAY.plusDays(1));
        park.addUserToGroup("G01", "u2@x.com");
        park.setCurrentPrice(new BigDecimal("99"));
        assertEquals(2, ticket.getComponents().size());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void activeGroupTicketLocksGroupUntilDeleted() {
        group("G01", 2);
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(3));
        assertTrue(park.isGroupLocked("G01"));
        assertThrows(ParkException.class, () -> park.addUserToGroup("G01", "u3@x.com"));
        assertThrows(ParkException.class, () -> park.deleteGroup("G01"));
        assertThrows(ParkException.class, () -> park.deleteTicket("TG01:DNI-0"));
        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:DNI-0"));
        assertFalse(park.isGroupLocked("G01"));
        park.addUserToGroup("G01", "u3@x.com");
    }

    @Test
    void todaysGroupTicketCannotBeDeleted() {
        group("G01", 1);
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(ParkException.class, () -> park.deleteGroupTicket("T999"));
        assertNotNull(park.findTicket("TG01"));
    }

    @Test
    void groupDeletionKeepsHistoricalTicket() {
        group("G01", 2);
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        park.deleteGroup("G01");
        assertNull(park.findGroup("G01"));
        assertNull(ticket.getGroup());
        assertEquals("G01", ticket.getGroupId());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertEquals(2, ticket.getComponents().size());
        assertSame(ticket, park.findTicket("TG01"));
    }

    @Test
    void userDeletionGuardCoversGroupTicketsAndLockedGroups() {
        group("G01", 2);
        park.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> park.ensureUserDeletable("u0@x.com"));
        park.setCurrentDate(TODAY.plusDays(1));
        park.ensureUserDeletable("u0@x.com");
        park.createGroup("G02", "Other");
        park.addUserToGroup("G02", "u5@x.com");
        park.addUserToGroup("G01", "u5@x.com");
        park.createGroupTicket("TG02", "G02", TODAY.plusDays(4));
        assertThrows(ParkException.class, () -> park.removeUserFromAllGroups("u5@x.com"));
        assertTrue(park.findGroup("G01").hasMember("u5@x.com"));
        park.removeUserFromAllGroups("u1@x.com");
        assertFalse(park.findGroup("G01").hasMember("u1@x.com"));
    }

    @Test
    void lockedGroupBlocksMemberWithoutOwnTicket() {
        group("G01", 1);
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(2));
        park.setGroupTicketDates(id -> List.of(TODAY.plusDays(2)));
        park.setCurrentDate(TODAY);
        assertThrows(ParkException.class, () -> park.ensureUserDeletable("u0@x.com"));
    }

    @Test
    void commandsShowTotalAndErrors() {
        group("G01", 6);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        TicketCommands.register(processor, park, out);
        processor.execute("GROUPTICKET CREATE;TG01;G01;2027-04-20");
        processor.execute("GROUPTICKET CREATE;TG02;G99;2027-04-20");
        processor.execute("TICKET DELETE;TG01:DNI-0");
        processor.execute("GROUPTICKET DELETE;TG01");
        processor.execute("GROUPTICKET DELETE");
        processor.execute("HELP");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("GROUP TICKET CREATED. TOTAL PRICE: 192.00"));
        assertTrue(text.contains("ERROR: Group G99 does not exist"));
        assertTrue(text.contains("belongs to group ticket TG01"));
        assertTrue(text.contains("GROUP TICKET DELETED"));
        assertTrue(text.contains("ERROR: Invalid syntax"));
        assertTrue(text.contains("GROUPTICKET CREATE;<identifier>;<group>;<date>"));
    }
}
