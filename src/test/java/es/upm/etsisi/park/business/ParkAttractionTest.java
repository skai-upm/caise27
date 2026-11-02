package es.upm.etsisi.park.business;

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
        attractions.create("A01", "Dragon Rojo", "20", null);
        tickets.create("T001", "ana@correo.com", TODAY_S);
        tickets.create("T002", "pedro@correo.com", TODAY_S);
    }

    @Test
    void createsEmptyAttraction() {
        Attraction<?> a = park.findAttraction("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "2.5", "", "9999999999"})
    void invalidCapacityRejected(String capacity) {
        assertThrows(ParkException.class, () -> attractions.create("A02", "Torre", capacity, null));
        assertEquals(null, park.findAttraction("A02"));
    }

    @Test
    void duplicateIdRejected() {
        assertThrows(ParkException.class, () -> attractions.create("A01", "Otra", "5", null));
        assertEquals("Dragon Rojo", park.findAttraction("A01").getName());
    }

    @Test
    void individualEntry() {
        assertEquals(1, attractions.enter("A01", "T001", false).entered());
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void wrongDateRejected() {
        tickets.create("T003", "ana@correo.com", TOMORROW_S);
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T003", false).entered());
        park.setToday(TOMORROW_S);
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T001", false).entered(), "yesterday's ticket");
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void fullAttractionRejected() {
        attractions.create("A02", "Mini", "1", null);
        attractions.enter("A02", "T001", false).entered();
        assertThrows(ParkException.class, () -> attractions.enter("A02", "T002", false).entered());
        assertEquals(List.of(ana), park.findAttraction("A02").getOccupants());
    }

    @Test
    void unknownTicketAttractionOrAlreadyInsideRejected() {
        assertThrows(ParkException.class, () -> attractions.enter("A99", "T001", false).entered());
        assertThrows(ParkException.class, () -> attractions.enter("A01", "NOPE", false).entered());
        attractions.enter("A01", "T001", false).entered();
        assertThrows(ParkException.class, () -> attractions.enter("A01", "T001", false).entered());
        assertEquals(1, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void presenceSurvivesDateChange() {
        attractions.enter("A01", "T001", false).entered();
        park.setToday(TOMORROW_S);
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void groupEntryIsIndivisible() {
        groupOf("G04", 4);
        tickets.createGroupTicket("TG04", "G04", TODAY_S);
        attractions.create("A03", "Small", "4", null);
        attractions.enter("A03", "T001", false).entered();
        assertThrows(ParkException.class, () -> attractions.enter("A03", "TG04", false).entered());
        assertEquals(List.of(ana), park.findAttraction("A03").getOccupants());
        attractions.exit("A03", "ana@correo.com");
        assertEquals(4, attractions.enter("A03", "TG04", false).entered());
        assertEquals(4, park.findAttraction("A03").getOccupants().size());
    }

    @Test
    void groupMembersExitIndividually() {
        groupOf("G04", 4);
        tickets.createGroupTicket("TG04", "G04", TODAY_S);
        attractions.enter("A01", "TG04", false).entered();
        Attraction<?> a = park.findAttraction("A01");
        String leaving = a.getOccupants().get(1).getEmail();
        attractions.exit("A01", leaving);
        assertEquals(3, a.getOccupants().size());
        assertEquals(17, a.getAvailable());
        assertFalse(a.getOccupants().stream().anyMatch(u -> u.getEmail().equals(leaving)));
    }

    @Test
    void constituentTicketAdmitsItsMember() {
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY_S);
        assertEquals(1, attractions.enter("A01", "TG01:87654321X", false).entered());
        assertEquals(List.of(pedro), park.findAttraction("A01").getOccupants());
    }

    @Test
    void exitOfNonOccupantRejected() {
        assertThrows(ParkException.class, () -> attractions.exit("A01", "ana@correo.com"));
        assertThrows(ParkException.class, () -> attractions.exit("A99", "ana@correo.com"));
    }

    @Test
    void exitUsesCurrentEmail() {
        attractions.enter("A01", "T001", false).entered();
        users.update("ana@correo.com", "Clave1!", "EMAIL", "ana.nueva@correo.com");
        attractions.exit("A01", "ana.nueva@correo.com");
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }
}
