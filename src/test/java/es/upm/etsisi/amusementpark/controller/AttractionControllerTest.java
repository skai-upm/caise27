package es.upm.etsisi.amusementpark.controller;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.model.Attraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttractionControllerTest extends ParkFixture {

    private AttractionController attractions;
    private TicketController tickets;

    @BeforeEach
    void setUp() {
        attractions = new AttractionController(park);
        tickets = new TicketController(park);
        attractions.create("A01", "Dragon Rojo", "20");
        attractions.create("A02", "Torre del Cielo", "1");
        GroupController groups = new GroupController(park);
        groups.create("G01", "Familia");
        groups.addUser("G01", "ana@correo.com");
        groups.addUser("G01", "pedro@correo.com");
    }

    @Test
    void createRejections() {
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A01", "Otra", "5"));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A03", "Noria", "0"));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A03", "Noria", "abc"));
        assertTrue(park.attractions().findById("A03").isEmpty());
    }

    @Test
    void individualEntryAndExit() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        assertEquals(1, attractions.enter("A01", "T001"));
        Attraction a = attractions.find("A01");
        assertEquals(List.of(ana), a.getOccupants());
        attractions.exit("A01", "ana@correo.com");
        assertTrue(a.getOccupants().isEmpty());
        assertEquals(20, a.getAvailablePlaces());
    }

    @Test
    void groupEntryAndIndividualExit() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertEquals(2, attractions.enter("A01", "TG01"));
        Attraction a = attractions.find("A01");
        assertEquals(18, a.getAvailablePlaces());
        attractions.exit("A01", "pedro@correo.com");
        assertEquals(List.of(ana), a.getOccupants());
    }

    @Test
    void groupEntryIsAtomic() {
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A02", "TG01"));
        assertTrue(attractions.find("A02").getOccupants().isEmpty());
    }

    @Test
    void ticketMustBeValidToday() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.plusDays(1).toString());
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "T001"));
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A01", "T999"));
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A99", "T001"));
        assertTrue(attractions.find("A01").getOccupants().isEmpty());
    }

    @Test
    void presenceIndependentOfLaterDateChange() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        attractions.enter("A01", "T001");
        park.setDate(TODAY.plusDays(3).toString());
        assertEquals(List.of(ana), attractions.find("A01").getOccupants());
    }

    @Test
    void exitOfUserNotInside() {
        assertThrows(IllegalArgumentException.class, () -> attractions.exit("A01", "ana@correo.com"));
    }

    @Test
    void attractionsReloadEmpty() {
        tickets.createTicket("T001", "ana@correo.com", TODAY.toString());
        attractions.enter("A01", "T001");
        restart();
        Attraction a = new AttractionController(park).find("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
    }
}
