package es.upm.etsisi.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.business.exception.AccessDeniedException;
import es.upm.etsisi.park.business.exception.DuplicateException;
import es.upm.etsisi.park.business.exception.NotFoundException;
import es.upm.etsisi.park.business.exception.OperationNotAllowedException;
import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.Pass;
import es.upm.etsisi.park.business.model.SpecialPass;
import es.upm.etsisi.park.business.model.SpeedPass;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PassControllerTest extends Delivery3ScenarioBase {

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) park.findTicket(id).orElseThrow();
    }

    @Test
    void singleton() {
        assertSame(PassController.getInstance(), PassController.getInstance());
    }

    @Test
    void packageOfTenSpeedPasses() {
        List<SpeedPass> passes = park.buySpeedPasses("SP01", "T001", "10");
        assertEquals(10, passes.size());
        assertEquals("SP01:1", passes.get(0).getId());
        assertEquals("SP01:10", passes.get(9).getId());
        for (SpeedPass pass : passes) {
            assertEquals(new BigDecimal("8.00"), pass.getPrice());
            assertFalse(pass.isConsumed());
            assertSame(ticket("T001"), pass.getTicket());
        }
        assertEquals(10, ticket("T001").getSpeedPasses().size());
    }

    @Test
    void singleSpeedPassFollowsNamingRuleAndUsesDiscountedPrice() {
        List<SpeedPass> passes = park.buySpeedPasses("SP02", "TG01:12345678Z", "1");
        assertEquals("SP02:1", passes.get(0).getId());
        assertEquals(new BigDecimal("7.20"), passes.get(0).getPrice());
    }

    @Test
    void laterPriceChangesDoNotAffectPasses() {
        park.setCurrentPrice("100.00");
        assertEquals(new BigDecimal("8.00"), park.buySpeedPasses("SP01", "T001", "1").get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), park.buySpecialPass("PE01", "T001").getPrice());
    }

    @Test
    void speedPassPurchaseRejections() {
        assertThrows(ValidationException.class, () -> park.buySpeedPasses("SP06", "T001", "5"));
        assertThrows(ValidationException.class, () -> park.buySpeedPasses("SP06", "T001", "x"));
        assertThrows(ValidationException.class, () -> park.buySpeedPasses("SP07", "TG01", "1"));
        assertThrows(NotFoundException.class, () -> park.buySpeedPasses("SP08", "T404", "1"));
        park.setCurrentDate("2027-04-16");
        assertThrows(OperationNotAllowedException.class, () -> park.buySpeedPasses("SP09", "T001", "1"));
        assertTrue(park.findPass("SP06:1").isEmpty());
    }

    @Test
    void conflictingIdentifierCreatesNothing() {
        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(DuplicateException.class, () -> park.buySpeedPasses("SP01", "T001", "10"));
        assertTrue(park.findPass("SP01:2").isEmpty());
        assertEquals(1, ticket("T001").getSpeedPasses().size());
        park.buySpecialPass("X:1", "TG01:87654321X");
        assertThrows(DuplicateException.class, () -> park.buySpeedPasses("X", "T001", "1"));
    }

    @Test
    void oneSpecialPassPerTicket() {
        SpecialPass pass = park.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());
        assertThrows(OperationNotAllowedException.class, () -> park.buySpecialPass("PE02", "T001"));
        park.enterAttraction("A04", "T001", false);
        assertTrue(pass.isConsumed());
        assertThrows(OperationNotAllowedException.class, () -> park.buySpecialPass("PE02", "T001"));
        assertSame(pass, ticket("T001").getSpecialPass().orElseThrow());
        assertThrows(ValidationException.class, () -> park.buySpecialPass("PE03", "TG01"));
    }

    @Test
    void refundRules() {
        park.createTicket("T002", "pedro@correo.com", FUTURE);
        park.buySpeedPasses("SP02", "T002", "10");
        Pass refunded = park.refundSpeedPass("SP02:3");
        assertEquals(new BigDecimal("8.00"), refunded.getPrice());
        assertTrue(park.findPass("SP02:3").isEmpty());
        assertTrue(park.findPass("SP02:4").isPresent());
        assertEquals(9, ticket("T002").getSpeedPasses().size());
        assertThrows(NotFoundException.class, () -> park.refundSpecialPass("SP02:1"));
        assertThrows(NotFoundException.class, () -> park.refundSpeedPass("SP02:3"));

        park.buySpecialPass("PE02", "T002");
        park.refundSpecialPass("PE02");
        assertTrue(ticket("T002").getSpecialPass().isEmpty());
        park.buySpecialPass("PE03", "T002");

        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(OperationNotAllowedException.class, () -> park.refundSpeedPass("SP01:1"));
    }

    @Test
    void consumedPassCannotBeRefundedAndIsKept() {
        park.createTicket("T002", "pedro@correo.com", FUTURE);
        park.buySpeedPasses("SP02", "T002", "1");
        park.setCurrentDate(FUTURE);
        park.enterAttraction("A02", "T002", true);
        park.setCurrentDate(TODAY);
        assertThrows(OperationNotAllowedException.class, () -> park.refundSpeedPass("SP02:1"));
        assertTrue(park.findPass("SP02:1").orElseThrow().isConsumed());
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        park.createTicket("T002", "pedro@correo.com", FUTURE);
        park.buySpeedPasses("SP02", "T002", "10");
        park.buySpeedPasses("SP03", "T002", "1");
        park.buySpecialPass("PE02", "T002");
        park.deleteTicket("T002");
        assertTrue(park.findPass("SP02:1").isEmpty());
        assertTrue(park.findPass("SP03:1").isEmpty());
        assertTrue(park.findPass("PE02").isEmpty());
        assertTrue(park.findTicket("T002").isEmpty());
    }

    @Test
    void rejectedTicketDeletionKeepsPasses() {
        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(OperationNotAllowedException.class, () -> park.deleteTicket("T001"));
        assertTrue(park.findPass("SP01:1").isPresent());
    }

    @Test
    void groupTicketDeletionCascadesToComponentPasses() {
        park.createGroup("G02", "Visita Mayo");
        park.addToGroup("G02", "pedro@correo.com");
        park.addToGroup("G02", "laura@correo.com");
        park.createGroupTicket("TG02", "G02", FUTURE);
        park.buySpeedPasses("SP05", "TG02:87654321X", "1");
        park.buySpecialPass("PE05", "TG02:11111111H");
        park.deleteGroupTicket("TG02");
        assertTrue(park.findPass("SP05:1").isEmpty());
        assertTrue(park.findPass("PE05").isEmpty());
        assertTrue(park.findTicket("TG02:87654321X").isEmpty());
        assertTrue(park.findTicket("TG02").isEmpty());
        park.buySpeedPasses("SP06", "TG01:87654321X", "1");
        assertThrows(OperationNotAllowedException.class, () -> park.deleteGroupTicket("TG01"));
        assertTrue(park.findPass("SP06:1").isPresent());
    }

    @Test
    void failedSpecialEntryLeavesPassUntouched() {
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A04", "T001", false));
        park.buySpecialPass("PE01", "T001");
        assertThrows(AccessDeniedException.class, () -> park.enterAttraction("A04", "T001", true));
        assertFalse(park.findPass("PE01").orElseThrow().isConsumed());
    }
}
