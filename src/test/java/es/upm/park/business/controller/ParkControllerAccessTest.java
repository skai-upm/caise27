package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.User;
import es.upm.park.persistence.repository.UserRepository;
import es.upm.park.business.service.ParkException;

class ParkControllerAccessTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();
    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() {
        park.start(new UserRepository(), DAY);
        ana = park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        pedro = park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6",
                "9876543210987654");
        park.getAttractionService().create("A01", "Dragon Rojo", "20");
        park.getAttractionService().create("A03", "Mini", "1");
        park.getTicketService().createIndividual("T001", "ana@correo.com", DAY);
        park.getTicketService().createIndividual("T002", "ana@correo.com", DAY.plusDays(1));
        park.getTicketService().createIndividual("T003", "pedro@correo.com", DAY);
        park.getTicketService().createIndividual("T004", "ana@correo.com", DAY);
    }

    private void assertRejected(String message, Runnable action) {
        ParkException e = assertThrows(ParkException.class, action::run);
        assertEquals(message, e.getMessage());
    }

    private Attraction attraction(String id) {
        return park.getAttractionService().find(id);
    }

    @Test
    void validTicketAdmitsItsUser() {
        assertEquals(List.of(ana), park.enterAttraction("A01", "T001"));
        assertTrue(attraction("A01").contains(ana));
        park.setCurrentDate("2027-04-20");
        assertEquals(List.of("ana@correo.com"), park.getAttractionService().occupantEmails("A01"));
    }

    @Test
    void ticketMustBeValidToday() {
        assertRejected("ticket T002 is not valid on 2027-04-15", () -> park.enterAttraction("A01", "T002"));
        park.setCurrentDate("2027-04-16");
        assertRejected("ticket T001 is not valid on 2027-04-16", () -> park.enterAttraction("A01", "T001"));
        assertTrue(attraction("A01").getOccupants().isEmpty());
    }

    @Test
    void capacityAndPresenceLimitEntry() {
        park.enterAttraction("A03", "T003");
        assertRejected("attraction A03 does not have capacity for 1 users", () -> park.enterAttraction("A03", "T001"));
        assertEquals(List.of(pedro), attraction("A03").getOccupants());
        park.enterAttraction("A01", "T001");
        assertRejected("user ana@correo.com is already inside attraction A01", () -> park.enterAttraction("A01", "T004"));
        assertEquals(1, attraction("A01").getOccupants().size());
    }

    @Test
    void unknownAttractionOrTicket() {
        assertRejected("no attraction A99", () -> park.enterAttraction("A99", "T001"));
        assertRejected("no ticket T999", () -> park.enterAttraction("A01", "T999"));
    }

    @Test
    void commonAbstractionGivesHolders() {
        assertEquals(List.of(ana), park.getTicketService().find("T001").holders());
        park.getGroupService().create("G01", "F");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        assertEquals(List.of(ana, pedro), park.createGroupTicket("TG01", "G01", DAY).holders());
        assertRejected("no ticket TG01:12345678Z", () -> park.enterAttraction("A01", "TG01:12345678Z"));
    }

    private void groupTicket(String id, LocalDate date) {
        if (park.getGroupService().groupsOf(ana).isEmpty()) {
            park.getGroupService().create("G01", "F");
            park.getGroupService().addUser("G01", "ana@correo.com");
            park.getGroupService().addUser("G01", "pedro@correo.com");
        }
        park.createGroupTicket(id, "G01", date);
    }

    @Test
    void wholeGroupEntersTogether() {
        groupTicket("TG01", DAY);
        assertEquals(List.of(ana, pedro), park.enterAttraction("A01", "TG01"));
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), park.getAttractionService().occupantEmails("A01"));
    }

    @Test
    void groupEntryIsIndivisible() {
        park.getAttractionService().create("A04", "Duo", "2");
        park.getUserService().create("Luis", "M", "00000000T", "luis@correo.com", "Clave1!", "6", "1111222233334444");
        park.getTicketService().createIndividual("T005", "luis@correo.com", DAY);
        park.enterAttraction("A04", "T005");
        groupTicket("TG01", DAY);
        assertRejected("attraction A04 does not have capacity for 2 users", () -> park.enterAttraction("A04", "TG01"));
        assertEquals(List.of("luis@correo.com"), park.getAttractionService().occupantEmails("A04"));
        park.enterAttraction("A01", "T001");
        assertRejected("user ana@correo.com is already inside attraction A01", () -> park.enterAttraction("A01", "TG01"));
        assertEquals(List.of("ana@correo.com"), park.getAttractionService().occupantEmails("A01"));
    }

    @Test
    void groupTicketForAnotherDateRejected() {
        groupTicket("TG02", DAY.plusDays(1));
        assertRejected("ticket TG02 is not valid on 2027-04-15", () -> park.enterAttraction("A01", "TG02"));
    }

    @Test
    void exitFreesPlacesAndGroupMembersLeaveIndividually() {
        park.enterAttraction("A03", "T001");
        assertEquals(ana, park.getAttractionService().exit("A03", "ana@correo.com"));
        park.enterAttraction("A03", "T003");
        groupTicket("TG01", DAY);
        park.enterAttraction("A01", "TG01");
        park.getAttractionService().exit("A01", "ana@correo.com");
        assertEquals(List.of("pedro@correo.com"), park.getAttractionService().occupantEmails("A01"));
        assertRejected("user ana@correo.com is not inside attraction A01",
                () -> park.getAttractionService().exit("A01", "ana@correo.com"));
        assertRejected("no attraction A99", () -> park.getAttractionService().exit("A99", "ana@correo.com"));
    }

    @Test
    void statusReflectsAttractionState() {
        park.enterAttraction("A01", "T001");
        park.getAttractionService().create("A05", "Familiar", "4");
        groupTicket("TG01", DAY);
        park.getAttractionService().exit("A01", "ana@correo.com");
        park.enterAttraction("A05", "T001");
        park.getTicketService().createIndividual("T006", "pedro@correo.com", DAY);
        String nl = System.lineSeparator();
        assertEquals("Attraction A05 - Familiar" + nl + "Capacity: 4" + nl + "Inside: 1" + nl + "Available: 3" + nl
                + "Users: ana@correo.com", park.getAttractionService().status("A05"));
        assertEquals("Attraction A01 - Dragon Rojo" + nl + "Capacity: 20" + nl + "Inside: 0" + nl + "Available: 20"
                + nl + "Users: none", park.getAttractionService().status("A01"));
        park.enterAttraction("A01", "TG01");
        assertTrue(park.getAttractionService().status("A01").contains("Inside: 2" + nl + "Available: 18" + nl
                + "Users: ana@correo.com, pedro@correo.com"));
    }
}
