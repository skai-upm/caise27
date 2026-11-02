package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassEntryTest extends ParkTestSupport {

    private GroupTicket tg;

    @BeforeEach
    void setUp() {
        ana();
        pedro();
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        groups.add("G01", "pedro@correo.com");
        tickets.create("T001", "ana@correo.com", TODAY_S);
        tg = tickets.createGroupTicket("TG01", "G01", TODAY_S);
        attractions.create("A01", "General", "20", null);
        attractions.create("A03", "Rafting", "20", "GROUP");
        attractions.create("A04", "Terror", "20", "SPECIAL");
        attractions.create("A02", "Individual", "20", "INDIVIDUAL");
    }

    private long consumed() {
        return park.getPasses().stream().filter(Pass::isConsumed).count();
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        passes.createSpeedPasses("SP01", "T001", "10");
        EntryResult r = attractions.enter("A02", "T001", true);
        assertEquals(1, r.entered());
        assertEquals(List.of("SP01:1"), r.speedPasses().stream().map(Pass::getId).toList());
        assertEquals(1, consumed());
    }

    @Test
    void speedEntryWithoutPassRejected() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("A01", "T001", true));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        passes.createSpeedPasses("SA", tg.getComposition().get(0).getId(), "1");
        assertThrows(RuleViolationException.class, () -> attractions.enter("A03", "TG01", true));
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        assertEquals(0, consumed());
        passes.createSpeedPasses("SB", tg.getComposition().get(1).getId(), "1");
        EntryResult r = attractions.enter("A03", "TG01", true);
        assertEquals(2, r.entered());
        assertEquals(2, consumed());
    }

    @Test
    void capacityFailureLeavesPassesUntouched() {
        attractions.create("SMALL", "Small", "1", null);
        tg.getComposition().forEach(c -> passes.createSpeedPasses("S" + c.getUser().getDni(), c.getId(), "1"));
        assertThrows(RuleViolationException.class, () -> attractions.enter("SMALL", "TG01", true));
        assertEquals(0, consumed());
    }

    @Test
    void wrongKindOrDateLeavesPassesUntouched() {
        passes.createSpeedPasses("SP01", "T001", "1");
        assertThrows(RuleViolationException.class, () -> attractions.enter("A03", "T001", true));
        park.setToday(TOMORROW_S);
        assertThrows(RuleViolationException.class, () -> attractions.enter("A01", "T001", true));
        assertEquals(0, consumed());
    }

    @Test
    void specialEntryConsumesSpecialPass() {
        SpecialPass pe = passes.createSpecialPass("PE01", "T001");
        EntryResult r = attractions.enter("A04", "T001", false);
        assertEquals(pe, r.specialPass());
        assertTrue(pe.isConsumed());
        attractions.exit("A04", "ana@correo.com");
        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "T001", false), "already consumed");
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void specialWithoutPassOrWithSpeedRejected() {
        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "T001", false));
        SpecialPass pe = passes.createSpecialPass("PE01", "T001");
        passes.createSpeedPasses("SP01", "T001", "1");
        assertThrows(RuleViolationException.class, () -> attractions.enter("A04", "T001", true));
        assertFalse(pe.isConsumed());
        assertEquals(0, consumed());
    }

    @Test
    void consumptionIsPersisted() {
        passes.createSpeedPasses("SP01", "T001", "1");
        passes.createSpecialPass("PE01", "T001");
        attractions.enter("A01", "T001", true);
        restart();
        assertTrue(park.findPass("SP01:1").isConsumed());
        assertFalse(park.findPass("PE01").isConsumed());
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void failedSaveUndoesEntryAndConsumption() {
        passes.createSpeedPasses("SP01", "T001", "1");
        java.io.File data = dir.toFile();
        assumeTrue(data.setWritable(false) && !data.canWrite());
        try {
            assertThrows(ParkException.class, () -> attractions.enter("A01", "T001", true));
        } finally {
            data.setWritable(true);
        }
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertFalse(park.findPass("SP01:1").isConsumed());
    }
}
