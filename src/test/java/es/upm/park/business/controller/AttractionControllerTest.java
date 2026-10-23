package es.upm.park.business.controller;

import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.attraction.Attraction;
import es.upm.park.business.model.attraction.AttractionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static es.upm.park.business.controller.ParkFixture.TODAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionControllerTest {

    @TempDir
    Path dir;

    private Park park;
    private AttractionController attractions;
    private TicketController tickets;

    @BeforeEach
    void setUp() throws ParkException {
        park = ParkFixture.openWithUsers(dir);
        attractions = AttractionController.getInstance();
        tickets = TicketController.getInstance();
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        groups.addMember("G01", "laura@correo.com");
        attractions.create("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        attractions.create("A02", "Torre del Cielo", 12, AttractionType.GENERAL);
    }

    @Test
    void rejectsDuplicateAttraction() {
        assertThrows(ParkException.class, () -> attractions.create("A01", "Otra", 5, AttractionType.GENERAL));
        assertThrows(ParkException.class, () -> attractions.create("A03", "", 5, AttractionType.GENERAL));
    }

    @Test
    void groupEntryAndIndividualExit() throws ParkException {
        tickets.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(3, attractions.enter("A01", "TG01", false).entered().size());

        Attraction<?> a01 = park.findAttraction("A01");
        assertEquals(3, a01.getOccupancy());
        assertEquals(17, a01.getAvailablePlaces());

        attractions.exit("A01", "ana@correo.com");
        attractions.exit("A01", "PEDRO@correo.com");
        assertEquals(1, a01.getOccupancy());
        assertThrows(ParkException.class, () -> attractions.exit("A01", "ana@correo.com"));
    }

    @Test
    void individualEntry() throws ParkException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        attractions.enter("A02", "T001", false);
        assertTrue(park.findAttraction("A02").isInside(ParkFixture.user(park, "ana@correo.com")));
    }

    @Test
    void groupEntryIsAllOrNothing() throws ParkException {
        attractions.create("A03", "Mini", 2, AttractionType.GENERAL);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        assertThrows(ParkException.class, () -> attractions.enter("A03", "TG01", false));
        assertEquals(0, park.findAttraction("A03").getOccupancy());
    }

    @Test
    void ticketMustBeValidToday() throws ParkException {
        tickets.createTicket("T002", "marta@correo.com", TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T002", false));
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T404", false));
        assertThrows(ParkException.class, () -> attractions.enter("A99", "T002", false));
    }

    @Test
    void userCannotBeInsideTwice() throws ParkException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        tickets.createGroupTicket("TG01", "G01", TODAY);
        attractions.enter("A02", "T001", false);
        assertThrows(ParkException.class, () -> attractions.enter("A01", "TG01", false));
        assertEquals(0, park.findAttraction("A01").getOccupancy());
    }

    @Test
    void presenceSurvivesDateChange() throws ParkException {
        tickets.createTicket("T001", "ana@correo.com", TODAY);
        attractions.enter("A01", "T001", false);
        park.setCurrentDate(TODAY.plusDays(1));
        assertTrue(park.findAttraction("A01").isInside(ParkFixture.user(park, "ana@correo.com")));
    }
}
