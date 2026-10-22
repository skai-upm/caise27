package es.upm.etsisi.park.service;

import es.upm.etsisi.park.TestPark;
import es.upm.etsisi.park.core.Park;
import es.upm.etsisi.park.model.Attraction;
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
    private AttractionService service;

    @BeforeEach
    void setUp() {
        park = TestPark.withUsers(dir);
        service = park.getAttractionService();
        service.create("A01", "Dragon Rojo", "20");
        service.create("A03", "Mini", "2");
        park.getGroupService().create("G01", "Familia Garcia");
        park.getGroupService().addMember("G01", "ana@correo.com");
        park.getGroupService().addMember("G01", "pedro@correo.com");
        park.getGroupService().addMember("G01", "laura@correo.com");
    }

    private Attraction a(String id) {
        return service.find(id);
    }

    @Test
    void rejectsInvalidAttractions() {
        assertThrows(UserOperationException.class, () -> service.create("A01", "Dup", "5"));
        assertThrows(UserOperationException.class, () -> service.create("A04", "Noria", "0"));
        assertThrows(UserOperationException.class, () -> service.create("A04", "Noria", "abc"));
        assertThrows(UserOperationException.class, () -> service.create("A04", "", "5"));
        assertThrows(UserOperationException.class, () -> service.find("A04"));
    }

    @Test
    void individualEntryAndExit() {
        park.getTicketService().createIndividual("T001", "ana@correo.com", "2027-04-15");

        service.enter("A01", "T001");
        assertEquals(1, a("A01").getOccupants().size());

        service.exit("A01", "ana@correo.com");
        assertEquals(20, a("A01").getAvailablePlaces());
        assertThrows(UserOperationException.class, () -> service.exit("A01", "ana@correo.com"));
    }

    @Test
    void groupEntryAdmitsEveryMember() {
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");

        assertEquals(3, service.enter("A01", "TG01").size());
        assertEquals(17, a("A01").getAvailablePlaces());

        service.exit("A01", "pedro@correo.com");
        assertEquals(2, a("A01").getOccupants().size());
    }

    @Test
    void groupEntryIsAllOrNothing() {
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");

        assertThrows(UserOperationException.class, () -> service.enter("A03", "TG01"));
        assertTrue(a("A03").getOccupants().isEmpty());
    }

    @Test
    void ticketMustBeForTheSystemDate() {
        park.getTicketService().createIndividual("T002", "ana@correo.com", "2027-04-20");

        assertThrows(UserOperationException.class, () -> service.enter("A01", "T002"));
        assertThrows(UserOperationException.class, () -> service.enter("A01", "T404"));
        assertThrows(UserOperationException.class, () -> service.enter("A99", "T002"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void presenceDoesNotDependOnLaterDateChanges() {
        park.getTicketService().createIndividual("T001", "ana@correo.com", "2027-04-15");
        service.enter("A01", "T001");

        park.getSystemService().setDate("2027-04-16");

        assertEquals(1, a("A01").getOccupants().size());
    }

    @Test
    void userCannotBeInsideTwice() {
        park.getTicketService().createIndividual("T001", "ana@correo.com", "2027-04-15");
        park.getTicketService().createGroup("TG01", "G01", "2027-04-15");
        service.enter("A01", "T001");

        assertThrows(UserOperationException.class, () -> service.enter("A03", "T001"));
        assertThrows(UserOperationException.class, () -> service.enter("A01", "TG01"));
        assertFalse(a("A01").getOccupants().size() > 1);
    }
}
