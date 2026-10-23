package es.upm.park.service;

import es.upm.park.model.Attraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static es.upm.park.service.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionServiceTest {

    @TempDir
    Path dir;

    private Park park;
    private AttractionService attractions;
    private TicketService tickets;

    @BeforeEach
    void setUp() throws OperationException {
        park = ParkFixture.openWithUsers(dir);
        attractions = new AttractionService(park);
        tickets = new TicketService(park);
        GroupService groups = new GroupService(park);
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        attractions.create("A01", "Dragon Rojo", 20);
        attractions.create("A02", "Torre del Cielo", 12);
    }

    @Test
    void rejectsDuplicateAttraction() {
        assertThrows(OperationException.class, () -> attractions.create("A01", "Otra", 5));
        assertThrows(OperationException.class, () -> attractions.create("A03", "", 5));
    }

    @Test
    void groupEntryAndIndividualExit() throws OperationException {
        tickets.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(3, attractions.enter("A01", "TG01").size());

        Attraction a01 = park.findAttraction("A01");
        assertEquals(3, a01.getOccupancy());
        assertEquals(17, a01.getAvailablePlaces());

        attractions.exit("A01", "ana@correo.com");
        attractions.exit("A01", "PEDRO@correo.com");
        assertEquals(1, a01.getOccupancy());
        assertThrows(OperationException.class, () -> attractions.exit("A01", "ana@correo.com"));
    }

    @Test
    void individualEntry() throws OperationException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        attractions.enter("A02", "T001");
        assertTrue(park.findAttraction("A02").isInside(ParkFixture.user(park, "ana@correo.com")));
    }

    @Test
    void groupEntryIsAllOrNothing() throws OperationException {
        attractions.create("A03", "Mini", 2);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(OperationException.class, () -> attractions.enter("A03", "TG01"));
        assertEquals(0, park.findAttraction("A03").getOccupancy());
    }

    @Test
    void ticketMustBeValidToday() throws OperationException {
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(1));
        assertThrows(OperationException.class, () -> attractions.enter("A01", "T002"));
        assertThrows(OperationException.class, () -> attractions.enter("A01", "T404"));
        assertThrows(OperationException.class, () -> attractions.enter("A99", "T002"));
    }

    @Test
    void userCannotBeInsideTwice() throws OperationException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        attractions.enter("A02", "T001");
        assertThrows(OperationException.class, () -> attractions.enter("A01", "TG01"));
        assertEquals(0, park.findAttraction("A01").getOccupancy());
    }

    @Test
    void presenceSurvivesDateChange() throws OperationException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        attractions.enter("A01", "T001");
        park.setCurrentDate(TODAY.plusDays(1));
        assertTrue(park.findAttraction("A01").isInside(ParkFixture.user(park, "ana@correo.com")));
    }
}
