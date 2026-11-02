package es.upm.etsisi.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PassTest extends ParkTestSupport {

    @BeforeEach
    void setUp() {
        ana();
        pedro();
        tickets.create("T001", "ana@correo.com", TOMORROW_S);
        tickets.create("TODAY", "ana@correo.com", TODAY_S);
    }

    @Test
    void singleAndPackPurchase() {
        List<SpeedPass> one = passes.createSpeedPasses("SP01", "T001", "1");
        assertEquals(List.of("SP01:1"), one.stream().map(Pass::getId).toList());
        assertEquals(new BigDecimal("8.00"), one.get(0).getPrice());
        List<SpeedPass> pack = passes.createSpeedPasses("SP02", "T001", "10");
        assertEquals(10, pack.size());
        assertEquals("SP02:10", pack.get(9).getId());
        assertEquals(new BigDecimal("80.00"), pack.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertTrue(pack.stream().noneMatch(Pass::isConsumed));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "2", "5", "11", "abc", ""})
    void quantityMustBeOneOrTen(String quantity) {
        assertThrows(ParkException.class, () -> passes.createSpeedPasses("SP01", "T001", quantity));
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void idConflictCreatesNothing() {
        passes.createSpecialPass("SP09:4", "T001");
        assertThrows(ConflictException.class, () -> passes.createSpeedPasses("SP09", "T001", "10"));
        assertEquals(1, park.getPasses().size());
    }

    @Test
    void ticketMustExistBeIndividualAndNotPast() {
        groups.create("G01", "Familia");
        groups.add("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TOMORROW_S);
        assertThrows(NotFoundException.class, () -> passes.createSpeedPasses("SP01", "NOPE", "1"));
        assertThrows(NotFoundException.class, () -> passes.createSpeedPasses("SP01", "TG01", "1"));
        park.setToday("2027-04-20");
        assertThrows(RuleViolationException.class, () -> passes.createSpeedPasses("SP01", "T001", "1"));
        assertThrows(RuleViolationException.class, () -> passes.createSpecialPass("PE01", "T001"));
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void passesOnDiscountedConstituentUseItsPrice() {
        groupOf("G06", 6);
        GroupTicket tg = tickets.createGroupTicket("TG06", "G06", TOMORROW_S);
        String constituent = tg.getComposition().get(0).getId();
        assertEquals(new BigDecimal("6.40"), passes.createSpeedPasses("SP", constituent, "1").get(0).getPrice());
        assertEquals(new BigDecimal("32.00"), passes.createSpecialPass("PE", constituent).getPrice());
    }

    @Test
    void priceUsesStoredTicketPriceNotCurrentPrice() {
        park.setBasePrice("100");
        assertEquals(new BigDecimal("40.00"), passes.createSpecialPass("PE01", "T001").getPrice());
    }

    @Test
    void oneSpecialPassPerTicket() {
        passes.createSpecialPass("PE01", "T001");
        assertThrows(ConflictException.class, () -> passes.createSpecialPass("PE02", "T001"));
        assertThrows(ConflictException.class, () -> passes.createSpecialPass("PE01", "TODAY"), "pass ids are unique");
    }

    @Test
    void refundRules() {
        passes.createSpeedPasses("SP02", "T001", "10");
        passes.deleteSpeedPass("SP02:3");
        assertNull(park.findPass("SP02:3"));
        assertEquals(9, park.getPasses().size());
        passes.createSpeedPasses("ST", "TODAY", "1");
        assertThrows(RuleViolationException.class, () -> passes.deleteSpeedPass("ST:1"), "ticket dated today");
        passes.createSpecialPass("PE01", "T001");
        assertThrows(NotFoundException.class, () -> passes.deleteSpeedPass("PE01"), "wrong kind");
        assertThrows(NotFoundException.class, () -> passes.deleteSpecialPass("SP02:1"), "wrong kind");
        assertThrows(NotFoundException.class, () -> passes.deleteSpecialPass("NOPE"));
        passes.deleteSpecialPass("PE01");
        assertNull(park.findPass("PE01"));
        passes.createSpecialPass("PE02", "T001");
    }

    @Test
    void consumedPassCannotBeRefunded() {
        passes.createSpeedPasses("SP01", "T001", "1");
        park.findPass("SP01:1").setConsumed(true);
        assertThrows(RuleViolationException.class, () -> passes.deleteSpeedPass("SP01:1"));
        assertNotNull(park.findPass("SP01:1"));
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        passes.createSpeedPasses("SP01", "T001", "1");
        passes.createSpeedPasses("SP02", "T001", "10");
        passes.createSpecialPass("PE01", "T001");
        passes.createSpeedPasses("KEEP", "TODAY", "1");
        tickets.delete("T001");
        assertEquals(List.of("KEEP:1"), park.getPasses().stream().map(Pass::getId).toList());
    }

    @Test
    void groupTicketDeletionCascades() {
        groupOf("G03", 3);
        GroupTicket tg = tickets.createGroupTicket("TG03", "G03", TOMORROW_S);
        for (IndividualTicket c : tg.getComposition()) {
            passes.createSpeedPasses("S" + c.getUser().getDni(), c.getId(), "1");
        }
        passes.createSpecialPass("PE", tg.getComposition().get(1).getId());
        tickets.deleteGroupTicket("TG03");
        assertTrue(park.getPasses().isEmpty());
        assertNull(park.findTicket("TG03"));
        assertNull(park.findTicket(tg.getComposition().get(0).getId()));
    }

    @Test
    void failedCascadeLeavesEverything() {
        groupOf("G03", 3);
        GroupTicket tg = tickets.createGroupTicket("TG03", "G03", TOMORROW_S);
        passes.createSpeedPasses("SP", tg.getComposition().get(0).getId(), "10");
        java.io.File data = dir.toFile();
        assumeTrue(data.setWritable(false) && !data.canWrite());
        try {
            assertThrows(ParkException.class, () -> tickets.deleteGroupTicket("TG03"));
            assertThrows(ParkException.class, () -> passes.deleteSpeedPass("SP:1"));
            assertThrows(ParkException.class, () -> passes.createSpecialPass("PE", "T001"));
            assertThrows(ParkException.class, () -> passes.createSpeedPasses("X", "T001", "10"));
        } finally {
            data.setWritable(true);
        }
        assertNotNull(park.findTicket("TG03"));
        assertEquals(10, park.getPasses().size());
        assertEquals(10, park.passesOf(tg.getComposition().get(0)).size());
    }
}
