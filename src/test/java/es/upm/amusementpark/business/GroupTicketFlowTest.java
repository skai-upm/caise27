package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.persistence.file.InMemoryUserStorage;
import es.upm.amusementpark.business.exception.ParkException;

class GroupTicketFlowTest {

    private final ParkController park = ParkController.getInstance();
    private final LocalDate today = LocalDate.of(2027, 4, 15);

    @BeforeEach
    void setUp() throws Exception {
        park.initialize(new InMemoryUserStorage());
        park.setCurrentDate(today);
        park.users().createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users().createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.users().createUser("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "6", "1111222233334444");
        park.groups().createGroup("G01", "Familia");
        park.groups().addMember("G01", "ana@correo.com");
        park.groups().addMember("G01", "pedro@correo.com");
    }

    private void assertRejected(String reason, Executable op) {
        ParkException e = assertThrows(ParkException.class, op);
        assertTrue(e.getMessage().contains(reason), e.getMessage());
    }

    @Test
    void purchaseCreatesCompositionForCurrentMembers() throws ParkException {
        GroupTicket ticket = park.createGroupTicket("TG01", "G01", today);
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
        assertEquals("TG01:12345678Z", ticket.individualTickets().get(0).getId());
        assertEquals("TG01:87654321X", ticket.individualTickets().get(1).getId());
    }

    @Test
    void rejectedPurchases() throws ParkException {
        park.groups().createGroup("G02", "Vacio");
        assertRejected("no members", () -> park.createGroupTicket("TG01", "G02", today));
        assertRejected("Group not found", () -> park.createGroupTicket("TG01", "G99", today));
        assertRejected("past dates", () -> park.createGroupTicket("TG01", "G01", today.minusDays(1)));
        assertTrue(park.tickets().findAll().isEmpty());
    }

    @Test
    void derivedIdentifierConflictCreatesNothing() throws ParkException {
        park.tickets().createIndividualTicket("TG01:12345678Z", "ana@correo.com", today);
        assertRejected("TG01:12345678Z", () -> park.createGroupTicket("TG01", "G01", today));
        assertEquals(1, park.tickets().findAll().size());
        assertTrue(park.tickets().findById("TG01").isEmpty());
        assertRejected("already exists", () -> park.tickets().createIndividualTicket("TG01:12345678Z", "pedro@correo.com", today));
    }

    @Test
    void groupTicketIdIsSharedWithIndividualTickets() throws ParkException {
        park.createGroupTicket("TG01", "G01", today);
        assertRejected("already exists", () -> park.tickets().createIndividualTicket("TG01", "luis@correo.com", today));
        assertRejected("already exists", () -> park.tickets().createIndividualTicket("TG01:87654321X", "luis@correo.com", today));
    }

    @Test
    void activeGroupTicketLocksUntilDeleted() throws ParkException {
        park.createGroupTicket("TG02", "G01", today.plusDays(5));
        assertRejected("locked", () -> park.groups().addMember("G01", "luis@correo.com"));
        assertRejected("locked", () -> park.groups().removeMember("G01", "ana@correo.com"));
        assertRejected("active group ticket", () -> park.deleteGroup("G01"));
        assertRejected("active tickets", () -> park.deleteUser("pedro@correo.com", "Otra2#"));
        assertRejected("belongs to a group ticket", () -> park.tickets().deleteIndividualTicket("TG02:12345678Z"));
        assertRejected("group ticket", () -> park.tickets().deleteIndividualTicket("TG02"));
        park.tickets().deleteGroupTicket("TG02");
        assertTrue(park.tickets().findAll().isEmpty());
        park.groups().addMember("G01", "luis@correo.com");
    }

    @Test
    void ticketForTodayCannotBeDeletedAndUnlocksWhenHistorical() throws ParkException {
        park.createGroupTicket("TG01", "G01", today);
        assertRejected("after the current date", () -> park.tickets().deleteGroupTicket("TG01"));
        assertRejected("active tickets", () -> park.deleteUser("pedro@correo.com", "Otra2#"));
        park.setCurrentDate(today.plusDays(1));
        park.groups().removeMember("G01", "pedro@correo.com");
        GroupTicket ticket = (GroupTicket) park.tickets().findById("TG01").orElseThrow();
        assertEquals(2, ticket.individualTickets().size());
        park.deleteUser("pedro@correo.com", "Otra2#");
        park.deleteGroup("G01");
        assertNull(ticket.getGroup());
        assertEquals(new BigDecimal("80.00"), ticket.getPrice());
    }

    @Test
    void deletingAnIndividualTicketIdAsGroupTicketFails() throws ParkException {
        park.tickets().createIndividualTicket("T001", "ana@correo.com", today.plusDays(1));
        assertRejected("Group ticket not found", () -> park.tickets().deleteGroupTicket("T001"));
    }

    @Test
    void groupTicketEntersAttractionAtomically() throws ParkException {
        park.createGroupTicket("TG01", "G01", today);
        park.attractions().createAttraction("A01", "Dragon Rojo", 20);
        park.attractions().createAttraction("A02", "Mini", 2);
        park.tickets().createIndividualTicket("T001", "luis@correo.com", today);
        assertEquals(2, park.enterAttraction("A01", "TG01").size());
        assertEquals(java.util.List.of("ana@correo.com", "pedro@correo.com"), park.attractions().listOccupantEmails("A01"));
        park.enterAttraction("A02", "T001");
        assertRejected("capacity", () -> park.enterAttraction("A02", "TG01"));
        assertEquals(java.util.List.of("luis@correo.com"), park.attractions().listOccupantEmails("A02"));
        park.attractions().exit("A01", "ana@correo.com");
        assertEquals(java.util.List.of("pedro@correo.com"), park.attractions().listOccupantEmails("A01"));
        assertEquals(19, park.attractions().status("A01").available());
    }
}
