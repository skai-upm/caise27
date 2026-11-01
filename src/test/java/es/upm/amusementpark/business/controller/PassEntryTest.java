package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.presentation.AppBootstrap;

public class PassEntryTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    public void setUp() throws IOException, OperationException {
        park = start();
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createUser("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave1!", "600000000",
                "1234567890123456");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.addToGroup("G01", "luis@correo.com");
        park.createTicket("T001", "ana@correo.com", "2026-10-01");
        park.createGroupTicket("TG01", "G01", "2026-10-01");
        park.createAttraction("GEN", "General", "10");
        park.createAttraction("IND", "Individual", "10", "INDIVIDUAL");
        park.createAttraction("GRP", "Group", "10", "GROUP");
        park.createAttraction("SPC", "Special", "10", "SPECIAL");
    }

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    private ParkController start() throws IOException {
        return AppBootstrap.start(new StoragePaths(dir), LocalDate.of(2026, 10, 1), new BCryptPasswordHasher(4));
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) park.getTickets().find(id);
    }

    private long unused(String ticketId) {
        return ticket(ticketId).getSpeedPasses().stream().filter(p -> !p.isConsumed()).count();
    }

    @Test
    public void normalEntryConsumesNothing() throws Exception {
        park.buySpeedPasses("T001", "1");
        assertTrue(park.enterAttraction("GEN", "T001").isEmpty());
        assertEquals(1, unused("T001"));
    }

    @Test
    public void individualSpeedEntryConsumesExactlyOne() throws Exception {
        park.buySpeedPasses("T001", "10");
        List<Pass> consumed = park.enterAttraction("GEN", "T001", true);
        assertEquals(1, consumed.size());
        assertEquals("T001-SP1", consumed.get(0).getId());
        assertTrue(consumed.get(0).isConsumed());
        assertEquals(9, unused("T001"));
        park.exitAttraction("GEN", "T001");
        assertEquals("T001-SP2", park.enterAttraction("IND", "T001", true).get(0).getId());
    }

    @Test
    public void speedEntryWithoutPassFails() {
        assertThrows(OperationException.class, () -> park.enterAttraction("GEN", "T001", true));
        assertEquals(0, park.getAttractions().find("GEN").getOccupancy());
    }

    @Test
    public void groupSpeedEntryConsumesOnePerMember() throws Exception {
        park.buySpeedPasses("TG01-1", "1");
        park.buySpeedPasses("TG01-2", "10");
        List<Pass> consumed = park.enterAttraction("GRP", "TG01", true);
        assertEquals(List.of("TG01-1-SP1", "TG01-2-SP1"), consumed.stream().map(Pass::getId).toList());
        assertEquals(0, unused("TG01-1"));
        assertEquals(9, unused("TG01-2"));
        assertEquals(2, park.getAttractions().find("GRP").getOccupancy());
    }

    @Test
    public void groupSpeedEntryIsAtomic() throws Exception {
        park.buySpeedPasses("TG01-2", "1");
        assertThrows(OperationException.class, () -> park.enterAttraction("GEN", "TG01", true));
        assertEquals(0, park.getAttractions().find("GEN").getOccupancy());
        assertEquals(1, unused("TG01-2"));
    }

    @Test
    public void failedEntryForOtherReasonsConsumesNothing() throws Exception {
        park.createAttraction("MINI", "Mini", "1");
        park.buySpeedPasses("TG01-1", "1");
        park.buySpeedPasses("TG01-2", "1");
        assertThrows(OperationException.class, () -> park.enterAttraction("MINI", "TG01", true));
        assertEquals(1, unused("TG01-1"));
        assertEquals(1, unused("TG01-2"));
        assertThrows(OperationException.class, () -> park.enterAttraction("IND", "TG01", true));
        assertEquals(1, unused("TG01-1"));
    }

    @Test
    public void specialAttractionConsumesSpecialPass() throws Exception {
        assertThrows(OperationException.class, () -> park.enterAttraction("SPC", "T001"));
        park.buySpecialPass("T001");
        List<Pass> consumed = park.enterAttraction("SPC", "T001");
        assertEquals(List.of("T001-SPECIAL"), consumed.stream().map(Pass::getId).toList());
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        park.exitAttraction("SPC", "T001");
        assertThrows(OperationException.class, () -> park.enterAttraction("SPC", "T001"));
        assertEquals(0, park.getAttractions().find("SPC").getOccupancy());
    }

    @Test
    public void speedRejectedAtSpecialAttraction() throws Exception {
        park.buySpecialPass("T001");
        park.buySpeedPasses("T001", "1");
        assertThrows(OperationException.class, () -> park.enterAttraction("SPC", "T001", true));
        assertFalse(ticket("T001").getSpecialPass().isConsumed());
        assertEquals(1, unused("T001"));
        assertEquals(0, park.getAttractions().find("SPC").getOccupancy());
    }

    @Test
    public void consumedStateIsPersisted() throws Exception {
        park.buySpeedPasses("T001", "1");
        park.buySpecialPass("T001");
        park.enterAttraction("GEN", "T001", true);
        park.enterAttraction("SPC", "T001");
        AppBootstrap.shutdown();
        park = start();
        IndividualTicket t = ticket("T001");
        assertTrue(t.getSpeedPasses().stream().allMatch(SpeedPass::isConsumed));
        assertTrue(t.getSpecialPass().isConsumed());
    }
}
