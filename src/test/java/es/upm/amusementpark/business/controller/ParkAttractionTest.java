package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.ParkException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.User;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkAttractionTest extends ParkTestSupport {

    private User ana;
    private User pedro;

    @BeforeEach
    void setUpPark() throws Exception {
        ana = createAna();
        pedro = createPedro();
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
    }

    @Test
    void createsEmptyAttraction() throws Exception {
        Attraction attraction = park.findAttraction("A01");
        assertEquals("Dragon Rojo", attraction.getName());
        assertEquals(20, attraction.getCapacity());
        assertTrue(attraction.getOccupants().isEmpty());
    }

    @Test
    void rejectsInvalidAttractions() {
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "Otra", "5"));
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "Cero", "0"));
        assertThrows(ParkException.class, () -> park.createAttraction("A03", "Texto", "veinte"));
        assertThrows(ParkException.class, () -> park.createAttraction("", "Sin id", "5"));
        assertThrows(ParkException.class, () -> park.findAttraction("A02"));
    }

    @Test
    void individualEntry() throws Exception {
        assertEquals(1, park.enterAttraction("A01", "T001"));
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void ticketMustBeValidToday() throws Exception {
        park.createTicket("T002", "pedro@correo.com", TOMORROW_TEXT);
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T002"));
        park.setCurrentDate(TOMORROW_TEXT);
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001"));
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T999"));
        assertThrows(ParkException.class, () -> park.enterAttraction("A99", "T002"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void groupEntryAdmitsEveryone() throws Exception {
        assertEquals(2, park.enterAttraction("A01", "TG01"));
        assertEquals(List.of(ana, pedro), park.findAttraction("A01").getOccupants());
    }

    @Test
    void groupEntryIsAllOrNothing() throws Exception {
        park.createAttraction("A02", "Torre del Cielo", "1");
        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG01"));
        assertTrue(park.findAttraction("A02").getOccupants().isEmpty());
        park.enterAttraction("A02", "T001");
        assertEquals(0, park.findAttraction("A02").getFreePlaces());
    }

    @Test
    void alreadyInsideIsRejected() throws Exception {
        park.enterAttraction("A01", "T001");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "TG01"));
        assertEquals(1, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void compositionTicketAdmitsOneUser() throws Exception {
        assertEquals(1, park.enterAttraction("A01", "TG01:87654321X"));
        assertEquals(List.of(pedro), park.findAttraction("A01").getOccupants());
    }

    @Test
    void presenceIndependentOfLaterDateChanges() throws Exception {
        park.enterAttraction("A01", "T001");
        park.setCurrentDate("2027-05-01");
        assertTrue(park.findAttraction("A01").contains(ana));
    }

    @Test
    void inactiveUserCannotEnter() throws Exception {
        park.createTicket("T003", "pedro@correo.com", TODAY_TEXT);
        park.setCurrentDate(TOMORROW_TEXT);
        park.removeFromGroup("G01", "pedro@correo.com");
        park.deleteUser("pedro@correo.com", "Otra2#");
        park.setCurrentDate(TODAY_TEXT);
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T003"));
    }

    @Test
    void individualExitAfterGroupEntry() throws Exception {
        park.enterAttraction("A01", "TG01");
        park.exitAttraction("A01", "ana@correo.com");
        assertEquals(List.of(pedro), park.findAttraction("A01").getOccupants());
        assertEquals(19, park.findAttraction("A01").getFreePlaces());
        assertThrows(ParkException.class, () -> park.exitAttraction("A01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.exitAttraction("A99", "pedro@correo.com"));
    }

    @Test
    void usersDoNotReferenceAttractions() {
        for (Field field : User.class.getDeclaredFields()) {
            assertFalse(field.getType().equals(Attraction.class), field.getName());
            assertFalse(field.getGenericType().getTypeName().contains("Attraction"), field.getName());
        }
    }
}
