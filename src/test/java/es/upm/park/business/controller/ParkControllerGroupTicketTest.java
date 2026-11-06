package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.model.GroupTicket;
import es.upm.park.persistence.repository.UserRepository;
import es.upm.park.business.service.GroupService;
import es.upm.park.business.service.ParkException;
import es.upm.park.business.service.TicketService;

class ParkControllerGroupTicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();
    private GroupService groups;
    private TicketService tickets;

    @BeforeEach
    void setUp() {
        park.start(new UserRepository(), LocalDate.of(2027, 4, 1));
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.getUserService().create("Luis", "M", "00000000T", "luis@correo.com", "Clave1!", "6", "1111222233334444");
        groups = park.getGroupService();
        tickets = park.getTicketService();
        groups.create("G01", "Familia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
    }

    private void assertRejected(String message, Runnable action) {
        int before = tickets.count();
        ParkException e = assertThrows(ParkException.class, action::run);
        assertEquals(message, e.getMessage());
        assertEquals(before, tickets.count());
    }

    @Test
    void createsCompositionForEveryMember() {
        GroupTicket t = park.createGroupTicket("TG01", "G01", DAY);
        assertEquals(new BigDecimal("80.00"), t.getPrice());
        assertTrue(t.findConstituent("TG01:12345678Z").isPresent());
        assertTrue(t.findConstituent("TG01:87654321X").isPresent());
    }

    @Test
    void rejectsEmptyUnknownPastAndConflicts() {
        groups.create("G02", "Vacio");
        assertRejected("group G02 is empty", () -> park.createGroupTicket("TG02", "G02", DAY));
        assertRejected("no group G99", () -> park.createGroupTicket("TG02", "G99", DAY));
        assertRejected("ticket date 2027-03-31 is before the current date 2027-04-01",
                () -> park.createGroupTicket("TG02", "G01", LocalDate.of(2027, 3, 31)));
        tickets.createIndividual("TG01:87654321X", "ana@correo.com", DAY);
        assertRejected("ticket identifier TG01:87654321X already exists", () -> park.createGroupTicket("TG01", "G01", DAY));
        assertThrows(ParkException.class, () -> tickets.find("TG01"));
    }

    @Test
    void constituentIdsShareTheSpaceAndCannotBeDeletedAlone() {
        park.createGroupTicket("TG01", "G01", DAY);
        assertRejected("ticket TG01:12345678Z already exists",
                () -> tickets.createIndividual("TG01:12345678Z", "ana@correo.com", DAY));
        assertRejected("ticket TG01:12345678Z belongs to group ticket TG01 and cannot be deleted independently",
                () -> tickets.deleteIndividual("TG01:12345678Z"));
        assertRejected("ticket TG01 already exists", () -> park.createGroupTicket("TG01", "G01", DAY));
    }

    @Test
    void activeGroupTicketLocksGroupAndMembersUntilDeleted() {
        park.createGroupTicket("TG01", "G01", DAY);
        assertRejected("group G01 has an active group ticket", () -> groups.addUser("G01", "luis@correo.com"));
        assertRejected("group G01 has an active group ticket", () -> groups.removeUser("G01", "ana@correo.com"));
        assertRejected("group G01 has an active group ticket", () -> park.deleteGroup("G01"));
        assertRejected("user ana@correo.com has an active ticket", () -> park.deleteUser("ana@correo.com", "Clave1!"));
        tickets.deleteGroupTicket("TG01");
        assertRejected("no group ticket TG01", () -> tickets.deleteGroupTicket("TG01"));
        assertFalse(tickets.hasActiveGroupTicket(groups.find("G01")));
        groups.addUser("G01", "luis@correo.com");
        assertEquals(3, groups.find("G01").getMemberDnis().size());
    }

    @Test
    void todaysGroupTicketKeptAndHistoricalSurvivesGroupDeletion() {
        GroupTicket t = park.createGroupTicket("TG01", "G01", LocalDate.of(2027, 4, 1));
        assertRejected("ticket TG01 cannot be deleted: it is not dated after 2027-04-01",
                () -> tickets.deleteGroupTicket("TG01"));
        park.setCurrentDate("2027-04-02");
        groups.addUser("G01", "luis@correo.com");
        assertEquals(2, t.getComposition().size());
        park.deleteGroup("G01");
        assertNull(t.getGroup());
        assertEquals("TG01", tickets.find("TG01").getId());
        assertEquals(new BigDecimal("80.00"), tickets.find("TG01").getPrice());
    }

    @Test
    void memberOfLockedGroupCannotBeDeletedEvenWithoutOwnTicket() {
        park.createGroupTicket("TG01", "G01", DAY);
        groups.create("G03", "Otro");
        park.setCurrentDate("2027-04-20");
        groups.addUser("G01", "luis@correo.com");
        park.setCurrentDate("2027-04-01");
        assertRejected("user luis@correo.com belongs to group G01 which has an active group ticket",
                () -> park.deleteUser("luis@correo.com", "Clave1!"));
    }
}
