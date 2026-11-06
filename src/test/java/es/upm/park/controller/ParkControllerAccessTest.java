package es.upm.park.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkControllerAccessTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final String ANA = "ana@correo.com";
    private static final String PEDRO = "pedro@correo.com";
    private static final String LUIS = "luis@correo.com";

    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(TestUsers.of(ANA, PEDRO, LUIS));
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.createIndividualTicket("T001", ANA, TODAY);
    }

    private Set<String> occupants(String attraction) {
        return park.findAttraction(attraction).orElseThrow().getOccupantEmails();
    }

    @Test
    void validTicketAdmitsItsUser() {
        assertEquals(List.of(ANA), park.enterAttraction("A01", "T001"));
        assertEquals(Set.of(ANA), occupants("A01"));
    }

    @Test
    void ticketForAnotherDayIsRejected() {
        park.createIndividualTicket("T002", PEDRO, TODAY.plusDays(1));
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T002"));
        assertTrue(occupants("A01").isEmpty());
    }

    @Test
    void fullAttractionRejectsEntry() {
        park.createAttraction("A03", "Mini", 1);
        park.createIndividualTicket("T003", PEDRO, TODAY);
        park.enterAttraction("A03", "T001");
        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "T003"));
        assertEquals(Set.of(ANA), occupants("A03"));
    }

    @Test
    void unknownIdsAndRepeatedEntryAreRejected() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A99", "T001"));
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T999"));
        park.enterAttraction("A01", "T001");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001"));
        assertEquals(1, occupants("A01").size());
    }

    @Test
    void presenceSurvivesDateChange() {
        park.enterAttraction("A01", "T001");
        park.setCurrentDate(TODAY.plusDays(1));
        assertEquals(Set.of(ANA), occupants("A01"));
    }

    private void groupTicket(String id, LocalDate date, String... members) {
        park.createGroup("G-" + id, "Grupo");
        for (String member : members) {
            park.addUserToGroup("G-" + id, member);
        }
        park.createGroupTicket(id, "G-" + id, date);
    }

    @Test
    void wholeGroupEnters() {
        groupTicket("TG01", TODAY, PEDRO, LUIS);
        assertEquals(List.of(PEDRO, LUIS), park.enterAttraction("A01", "TG01"));
        assertEquals(Set.of(PEDRO, LUIS), occupants("A01"));
    }

    @Test
    void insufficientCapacityAdmitsNobody() {
        park.createAttraction("A02", "Torre", 2);
        park.enterAttraction("A02", "T001");
        groupTicket("TG01", TODAY, PEDRO, LUIS);
        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG01"));
        assertEquals(Set.of(ANA), occupants("A02"));
    }

    @Test
    void groupTicketForAnotherDayAdmitsNobody() {
        groupTicket("TG02", TODAY.plusDays(1), PEDRO, LUIS);
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "TG02"));
        assertTrue(occupants("A01").isEmpty());
    }

    @Test
    void memberAlreadyInsideAdmitsNobody() {
        park.enterAttraction("A01", "T001");
        groupTicket("TG03", TODAY, ANA, PEDRO);
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "TG03"));
        assertEquals(Set.of(ANA), occupants("A01"));
    }

    @Test
    void exitReleasesPlaceAndGroupMembersLeaveIndividually() {
        park.createAttraction("A02", "Torre", 2);
        groupTicket("TG01", TODAY, PEDRO, LUIS);
        park.enterAttraction("A02", "TG01");
        park.exitAttraction("A02", PEDRO);
        assertEquals(Set.of(LUIS), occupants("A02"));
        park.enterAttraction("A02", "T001");
        assertEquals(Set.of(LUIS, ANA), occupants("A02"));
        assertThrows(ParkException.class, () -> park.exitAttraction("A02", PEDRO));
        assertThrows(ParkException.class, () -> park.exitAttraction("A99", ANA));
        assertEquals(2, occupants("A02").size());
    }
}
