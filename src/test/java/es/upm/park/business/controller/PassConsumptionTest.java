package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.model.Pass;
import es.upm.park.business.service.AccessResult;
import es.upm.park.business.service.ParkException;
import es.upm.park.persistence.repository.ParkRepositories;

class PassConsumptionTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();
    private ParkRepositories repos;

    @BeforeEach
    void setUp() {
        repos = ParkRepositories.inMemory();
        park.start(repos, DAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.getAttractionService().create("A01", "Dragon Rojo", "20", "GENERAL");
        park.getAttractionService().create("A03", "Rafting", "24", "GROUP");
        park.getAttractionService().create("A04", "Terror", "15", "SPECIAL");
        park.getAttractionService().create("A05", "Mini", "1", "INDIVIDUAL");
        park.getTicketService().createIndividual("T001", "ana@correo.com", DAY);
        park.getTicketService().createIndividual("T002", "pedro@correo.com", DAY);
        park.getGroupService().create("G01", "F");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", DAY);
    }

    private Pass pass(String id) {
        return repos.passes().findById(id).orElseThrow();
    }

    private void assertRejected(String message, String attraction, String ticket, boolean speed) {
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction(attraction, ticket, speed));
        assertEquals(message, e.getMessage());
        assertTrue(park.getAttractionService().occupantEmails(attraction).isEmpty());
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() {
        park.getPassService().buySpeedPasses("SP01", "T001", "10");
        AccessResult result = park.enterAttraction("A01", "T001", true);
        assertEquals(1, result.consumed().size());
        assertTrue(result.consumed().get(0).isConsumed());
        assertEquals(1, repos.passes().findAll().stream().filter(Pass::isConsumed).count());
        assertEquals(List.of("ana@correo.com"), park.getAttractionService().occupantEmails("A01"));
    }

    @Test
    void speedEntryWithoutAvailablePassIsRejected() {
        assertRejected("ticket T001 has no unconsumed SpeedPass", "A01", "T001", true);
        park.getPassService().buySpeedPasses("SP01", "T001", "1");
        pass("SP01:1").consume();
        assertRejected("ticket T001 has no unconsumed SpeedPass", "A01", "T001", true);
    }

    @Test
    void failedEntryKeepsPassesUnconsumed() {
        park.enterAttraction("A05", "T002", false);
        park.getPassService().buySpeedPasses("SP01", "T001", "1");
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction("A05", "T001", true));
        assertEquals("attraction A05 does not have capacity for 1 users", e.getMessage());
        assertFalse(pass("SP01:1").isConsumed());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        park.getPassService().buySpeedPasses("SPA", "TG01:12345678Z", "1");
        assertRejected("ticket TG01:87654321X has no unconsumed SpeedPass", "A01", "TG01", true);
        assertFalse(pass("SPA:1").isConsumed());
        park.getPassService().buySpeedPasses("SPB", "TG01:87654321X", "1");
        AccessResult result = park.enterAttraction("A03", "TG01", true);
        assertEquals(2, result.users().size());
        assertTrue(pass("SPA:1").isConsumed());
        assertTrue(pass("SPB:1").isConsumed());
    }

    @Test
    void specialPassConsumedOnlyOnSuccessfulEntry() {
        park.getPassService().buySpecialPass("PE01", "T001");
        park.getPassService().buySpeedPasses("SP01", "T001", "1");
        assertRejected("attraction A04 (SPECIAL) does not accept SpeedPasses", "A04", "T001", true);
        assertFalse(pass("PE01").isConsumed());
        assertFalse(pass("SP01:1").isConsumed());
        AccessResult result = park.enterAttraction("A04", "T001", false);
        assertEquals(List.of(pass("PE01")), result.consumed());
        assertTrue(pass("PE01").isConsumed());
        park.getAttractionService().exit("A04", "ana@correo.com");
        assertRejected("ticket T001 needs an unconsumed special pass for attraction A04", "A04", "T001", false);
    }

    @Test
    void normalEntryConsumesNothing() {
        park.getPassService().buySpeedPasses("SP01", "T001", "1");
        assertTrue(park.enterAttraction("A01", "T001", false).consumed().isEmpty());
        assertFalse(pass("SP01:1").isConsumed());
    }

    @Test
    void constituentTicketUsesItsOwnSpecialPass() {
        park.getPassService().buySpecialPass("PE01", "TG01:12345678Z");
        assertRejected("attraction A04 (SPECIAL) does not accept ticket TG01", "A04", "TG01", false);
        AccessResult result = park.enterAttraction("A04", "TG01:12345678Z", false);
        assertEquals("ana@correo.com", result.users().get(0).getEmail());
        assertEquals(1, result.users().size());
        assertTrue(pass("PE01").isConsumed());
        assertRejected("no ticket TG09:12345678Z", "A01", "TG09:12345678Z", false);
    }
}
