package es.upm.park.service;

import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static es.upm.park.service.ParkFixture.TODAY;
import static es.upm.park.service.ParkFixture.price;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketServiceTest {

    @TempDir
    Path dir;

    private Park park;
    private GroupService groups;
    private TicketService tickets;

    @BeforeEach
    void setUp() throws OperationException {
        park = ParkFixture.openWithUsers(dir);
        groups = new GroupService(park);
        tickets = new TicketService(park);
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
    }

    @Test
    void createsIndividualTicketAtCurrentPrice() throws OperationException {
        IndividualTicket ticket = tickets.createTicket("T001", "ana@correo.com", TODAY);
        assertEquals(price("40.00"), ticket.getPrice());
        assertSame(ParkFixture.user(park, "ana@correo.com"), ticket.getUser());

        park.setCurrentPrice(price("45.50"));
        IndividualTicket later = tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));
        assertEquals(price("45.50"), later.getPrice());
        assertEquals(price("40.00"), ticket.getPrice());
    }

    @Test
    void rejectsPastDateUnknownUserAndDuplicateId() throws OperationException {
        assertThrows(OperationException.class,
                () -> tickets.createTicket("T009", "ana@correo.com", TODAY.minusDays(1)));
        assertThrows(OperationException.class,
                () -> tickets.createTicket("T009", "nobody@correo.com", TODAY));
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        assertThrows(OperationException.class, () -> tickets.createTicket("T001", "pedro@correo.com", TODAY));
        assertNull(park.findTicket("T009"));
    }

    @Test
    void createsGroupTicketWithDerivedIdentifiers() throws OperationException {
        GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", TODAY);

        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X", "TG01:11111111H"),
                ticket.getComposition().stream().map(IndividualTicket::getId).toList());
        assertEquals(price("108.00"), ticket.getPrice());
        assertSame(park.findGroup("G01"), ticket.getGroup());
        assertNotNull(park.findTicket("TG01:87654321X"));
    }

    @Test
    void sixMemberGroupCosts192() throws OperationException {
        ParkFixture.addUsers(park, 3);
        groups.create("G02", "Seis");
        for (String email : List.of("ana@correo.com", "pedro@correo.com", "laura@correo.com",
                "user0@correo.com", "user1@correo.com", "user2@correo.com")) {
            groups.addMember("G02", email);
        }
        GroupTicket ticket = tickets.createGroupTicket("TG02", "G02", TODAY);
        assertEquals(price("32.00"), ticket.getComposition().get(0).getPrice());
        assertEquals(price("192.00"), ticket.getPrice());
    }

    @Test
    void sharedIdentifierSpace() throws OperationException {
        tickets.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(OperationException.class, () -> tickets.createTicket("TG01", "marta@correo.com", TODAY));
        assertThrows(OperationException.class,
                () -> tickets.createTicket("TG01:12345678Z", "marta@correo.com", TODAY));
        assertThrows(OperationException.class, () -> tickets.createGroupTicket("TG01", "G01", TODAY));
    }

    @Test
    void derivedIdConflictCreatesNothing() throws OperationException {
        tickets.createTicket("TG05:87654321X", "marta@correo.com", TODAY);
        int before = park.tickets().size();

        assertThrows(OperationException.class, () -> tickets.createGroupTicket("TG05", "G01", TODAY));

        assertEquals(before, park.tickets().size());
        assertNull(park.findTicket("TG05"));
        assertNull(park.findTicket("TG05:12345678Z"));
    }

    @Test
    void rejectsEmptyGroupUnknownGroupAndPastDate() throws OperationException {
        groups.create("G03", "Vacio");
        assertThrows(OperationException.class, () -> tickets.createGroupTicket("TG03", "G03", TODAY));
        assertThrows(OperationException.class, () -> tickets.createGroupTicket("TG03", "G99", TODAY));
        assertThrows(OperationException.class,
                () -> tickets.createGroupTicket("TG03", "G01", TODAY.minusDays(1)));
    }

    @Test
    void compositionDoesNotFollowGroupChanges() throws OperationException {
        GroupTicket ticket = tickets.createGroupTicket("TG01", "G01", TODAY);
        park.setCurrentDate(TODAY.plusDays(1));
        groups.removeMember("G01", "laura@correo.com");
        assertEquals(3, ticket.getComposition().size());
    }

    @Test
    void deletesOnlyFutureStandaloneTickets() throws OperationException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(5));

        assertThrows(OperationException.class, () -> tickets.deleteTicket("T001"));
        tickets.deleteTicket("T002");
        assertNull(park.findTicket("T002"));
        assertThrows(OperationException.class, () -> tickets.deleteTicket("T404"));
    }

    @Test
    void cannotDeleteGroupComponentsOrGroupTicketsWithTicketDelete() throws OperationException {
        tickets.createGroupTicket("TG02", "G01", TODAY.plusDays(5));
        assertThrows(OperationException.class, () -> tickets.deleteTicket("TG02:12345678Z"));
        assertThrows(OperationException.class, () -> tickets.deleteTicket("TG02"));
        assertNotNull(park.findTicket("TG02:12345678Z"));
    }

    @Test
    void groupTicketDeletionRemovesComposition() throws OperationException {
        tickets.createGroupTicket("TG01", "G01", TODAY);
        tickets.createGroupTicket("TG02", "G01", TODAY.plusDays(5));

        assertThrows(OperationException.class, () -> tickets.deleteGroupTicket("TG01"));
        tickets.deleteGroupTicket("TG02");

        assertNull(park.findTicket("TG02"));
        assertNull(park.findTicket("TG02:12345678Z"));
        assertNotNull(park.findTicket("TG01:12345678Z"));
        assertThrows(OperationException.class, () -> tickets.deleteGroupTicket("TG01:12345678Z"));
    }

    @Test
    void activeTicketsIncludeGroupComponents() throws OperationException {
        tickets.createGroupTicket("TG01", "G01", TODAY);
        assertFalse(park.hasActiveTicket(ParkFixture.user(park, "marta@correo.com")));
        park.setCurrentDate(TODAY.minusDays(1));
        assertTrue(park.hasActiveTicket(ParkFixture.user(park, "laura@correo.com")));
    }
}
