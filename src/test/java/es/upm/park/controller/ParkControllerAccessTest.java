package es.upm.park.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.model.Attraction;
import es.upm.park.model.User;
import es.upm.park.repository.UserRepository;
import es.upm.park.service.ParkException;

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
        assertRejected("no ticket TG01", () -> park.enterAttraction("A01", "TG01"));
    }
}
