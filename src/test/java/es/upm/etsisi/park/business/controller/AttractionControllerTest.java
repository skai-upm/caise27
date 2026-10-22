package es.upm.etsisi.park.business.controller;

import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionServiceTest {

    @TempDir
    Path dir;

    private Park park;
    private AttractionController service;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        service = park.getAttractionController();
        service.create("A01", "Dragon Rojo", "20", null);
        service.create("A03", "Mini", "2", null);
        park.getGroupController().create("G01", "Familia Garcia");
        park.getGroupController().addMember("G01", "ana@correo.com");
        park.getGroupController().addMember("G01", "pedro@correo.com");
        park.getGroupController().addMember("G01", "laura@correo.com");
    }

    private Attraction<?> a(String id) {
        return service.find(id);
    }

    @Test
    void rejectsInvalidAttractions() {
        assertThrows(ParkException.class, () -> service.create("A01", "Dup", "5", null));
        assertThrows(ParkException.class, () -> service.create("A04", "Noria", "0", null));
        assertThrows(ParkException.class, () -> service.create("A04", "Noria", "abc", null));
        assertThrows(ParkException.class, () -> service.create("A04", "", "5", null));
        assertThrows(ParkException.class, () -> service.find("A04"));
    }

    @Test
    void individualEntryAndExit() {
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");

        service.enter("A01", "T001", null);
        assertEquals(1, a("A01").getOccupants().size());

        service.exit("A01", "ana@correo.com");
        assertEquals(20, a("A01").getAvailablePlaces());
        assertThrows(ParkException.class, () -> service.exit("A01", "ana@correo.com"));
    }

    @Test
    void groupEntryAdmitsEveryMember() {
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");

        assertEquals(3, service.enter("A01", "TG01", null).users().size());
        assertEquals(17, a("A01").getAvailablePlaces());

        service.exit("A01", "pedro@correo.com");
        assertEquals(2, a("A01").getOccupants().size());
    }

    @Test
    void groupEntryIsAllOrNothing() {
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");

        assertThrows(ParkException.class, () -> service.enter("A03", "TG01", null));
        assertTrue(a("A03").getOccupants().isEmpty());
    }

    @Test
    void ticketMustBeForTheSystemDate() {
        park.getTicketController().createIndividual("T002", "ana@correo.com", "2027-04-20");

        assertThrows(ParkException.class, () -> service.enter("A01", "T002", null));
        assertThrows(ParkException.class, () -> service.enter("A01", "T404", null));
        assertThrows(ParkException.class, () -> service.enter("A99", "T002", null));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void presenceDoesNotDependOnLaterDateChanges() {
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");
        service.enter("A01", "T001", null);

        park.getSystemController().setDate("2027-04-16");

        assertEquals(1, a("A01").getOccupants().size());
    }

    @Test
    void userCannotBeInsideTwice() {
        park.getTicketController().createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getTicketController().createGroup("TG01", "G01", "2027-04-15");
        service.enter("A01", "T001", null);

        assertThrows(ParkException.class, () -> service.enter("A03", "T001", null));
        assertThrows(ParkException.class, () -> service.enter("A01", "TG01", null));
        assertFalse(a("A01").getOccupants().size() > 1);
    }
}
