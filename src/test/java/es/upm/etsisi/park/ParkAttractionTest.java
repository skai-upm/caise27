package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkAttractionTest extends ParkTestSupport {

    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() {
        ana = ana();
        pedro = pedro();
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", "ana@correo.com", TODAY_S);
        park.createTicket("T002", "pedro@correo.com", TODAY_S);
    }

    @Test
    void createsEmptyAttraction() {
        Attraction a = park.findAttraction("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "2.5", "", "9999999999"})
    void invalidCapacityRejected(String capacity) {
        assertThrows(ParkException.class, () -> park.createAttraction("A02", "Torre", capacity));
        assertEquals(null, park.findAttraction("A02"));
    }

    @Test
    void duplicateIdRejected() {
        assertThrows(ParkException.class, () -> park.createAttraction("A01", "Otra", "5"));
        assertEquals("Dragon Rojo", park.findAttraction("A01").getName());
    }

    @Test
    void individualEntry() {
        assertEquals(1, park.enter("A01", "T001"));
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void wrongDateRejected() {
        park.createTicket("T003", "ana@correo.com", TOMORROW_S);
        assertThrows(ParkException.class, () -> park.enter("A01", "T003"));
        park.setToday(TOMORROW_S);
        assertThrows(ParkException.class, () -> park.enter("A01", "T001"), "yesterday's ticket");
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void fullAttractionRejected() {
        park.createAttraction("A02", "Mini", "1");
        park.enter("A02", "T001");
        assertThrows(ParkException.class, () -> park.enter("A02", "T002"));
        assertEquals(List.of(ana), park.findAttraction("A02").getOccupants());
    }

    @Test
    void unknownTicketAttractionOrAlreadyInsideRejected() {
        assertThrows(ParkException.class, () -> park.enter("A99", "T001"));
        assertThrows(ParkException.class, () -> park.enter("A01", "NOPE"));
        park.enter("A01", "T001");
        assertThrows(ParkException.class, () -> park.enter("A01", "T001"));
        assertEquals(1, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void presenceSurvivesDateChange() {
        park.enter("A01", "T001");
        park.setToday(TOMORROW_S);
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void groupEntryIsIndivisible() {
        groupOf("G04", 4);
        park.createGroupTicket("TG04", "G04", TODAY_S);
        park.createAttraction("A03", "Small", "4");
        park.enter("A03", "T001");
        assertThrows(ParkException.class, () -> park.enter("A03", "TG04"));
        assertEquals(List.of(ana), park.findAttraction("A03").getOccupants());
        park.exit("A03", "ana@correo.com");
        assertEquals(4, park.enter("A03", "TG04"));
        assertEquals(4, park.findAttraction("A03").getOccupants().size());
    }

    @Test
    void groupMembersExitIndividually() {
        groupOf("G04", 4);
        park.createGroupTicket("TG04", "G04", TODAY_S);
        park.enter("A01", "TG04");
        Attraction a = park.findAttraction("A01");
        String leaving = a.getOccupants().get(1).getEmail();
        park.exit("A01", leaving);
        assertEquals(3, a.getOccupants().size());
        assertEquals(17, a.getAvailable());
        assertFalse(a.getOccupants().stream().anyMatch(u -> u.getEmail().equals(leaving)));
    }

    @Test
    void constituentTicketAdmitsItsMember() {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_S);
        assertEquals(1, park.enter("A01", "TG01:87654321X"));
        assertEquals(List.of(pedro), park.findAttraction("A01").getOccupants());
    }

    @Test
    void exitOfNonOccupantRejected() {
        assertThrows(ParkException.class, () -> park.exit("A01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> park.exit("A99", "ana@correo.com"));
    }

    @Test
    void exitUsesCurrentEmail() {
        park.enter("A01", "T001");
        park.updateUser("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        park.exit("A01", "ana.nueva@correo.com");
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }
}
