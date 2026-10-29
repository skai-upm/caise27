package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassTest extends ParkTestSupport {

    private PassController passes;
    private IndividualTicket t001;

    @BeforeEach
    void ticket() {
        passes = PassController.getInstance();
        user(1);
        t001 = park.createTicket("T001", email(1), "2027-04-15");
    }

    @Test
    void speedPassPackageOfTen() {
        List<SpeedPass> bought = passes.buySpeedPasses("SP02", "T001", "10");
        assertEquals(10, bought.size());
        assertEquals("SP02:1", bought.get(0).getId());
        assertEquals("SP02:10", bought.get(9).getId());
        assertEquals(new BigDecimal("8.00"), bought.get(0).getPrice());
        assertEquals(new BigDecimal("80.00"), PassController.total(bought));
        assertEquals(10, t001.getSpeedPasses().size());
        assertFalse(bought.get(0).isConsumed());
    }

    @Test
    void singleSpeedPassUsesSameIdRule() {
        assertEquals("SP01:1", passes.buySpeedPasses("SP01", "T001", "1").get(0).getId());
    }

    @Test
    void speedPassRejections() {
        assertThrows(ParkException.class, () -> passes.buySpeedPasses("SP03", "T001", "5"));
        assertThrows(ParkException.class, () -> passes.buySpeedPasses("SP03", "T404", "1"));
        user(2);
        park.createGroup("G01", "G");
        park.addToGroup("G01", email(2));
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertThrows(ParkException.class, () -> passes.buySpeedPasses("SP03", "TG01", "1"));
        park.setCurrentDate(LocalDate.of(2027, 4, 16));
        assertThrows(ParkException.class, () -> passes.buySpeedPasses("SP03", "T001", "1"));
        assertNull(park.findPass("SP03:1"));
    }

    @Test
    void speedPassConflictCreatesNothing() {
        passes.buySpeedPasses("SP02", "T001", "1");
        passes.buySpeedPasses("X", "T001", "1");
        // SP02:1 exists, so a 10-pack SP02 must not be created at all.
        assertThrows(ParkException.class, () -> passes.buySpeedPasses("SP02", "T001", "10"));
        assertNull(park.findPass("SP02:2"));
        assertEquals(2, t001.getSpeedPasses().size());
    }

    @Test
    void passesOnDiscountedCompositionTicket() {
        for (int i = 2; i <= 4; i++) {
            user(i);
        }
        park.createGroup("G01", "G");
        for (int i = 2; i <= 4; i++) {
            park.addToGroup("G01", email(i));
        }
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        String memberId = tg.getComposition().get(0).getId();
        assertEquals(new BigDecimal("7.20"), passes.buySpeedPasses("SPG", memberId, "1").get(0).getPrice());
        assertEquals(new BigDecimal("36.00"), passes.buySpecialPass("PEG", memberId).getPrice());
    }

    @Test
    void pricesUnaffectedBySystemPrice() {
        park.setPrice(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("8.00"), passes.buySpeedPasses("SP01", "T001", "1").get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), passes.buySpecialPass("PE01", "T001").getPrice());
    }

    @Test
    void atMostOneSpecialPass() {
        SpecialPass pe = passes.buySpecialPass("PE01", "T001");
        assertEquals(pe, t001.getSpecialPass().orElseThrow());
        assertThrows(ParkException.class, () -> passes.buySpecialPass("PE02", "T001"));
        pe.consume();
        assertThrows(ParkException.class, () -> passes.buySpecialPass("PE02", "T001"));
        assertThrows(ParkException.class, () -> passes.buySpecialPass("PE01", "T001"), "duplicate id");
    }

    @Test
    void passIdsAreUniqueAcrossTypes() {
        passes.buySpeedPasses("SP01", "T001", "1");
        assertThrows(ParkException.class, () -> passes.buySpecialPass("SP01:1", "T001"));
    }

    @Test
    void refunds() {
        passes.buySpeedPasses("SP02", "T001", "10");
        passes.buySpecialPass("PE01", "T001");
        passes.refundSpeedPass("SP02:3");
        assertNull(park.findPass("SP02:3"));
        assertEquals(9, t001.getSpeedPasses().size());
        assertThrows(ParkException.class, () -> passes.refundSpeedPass("PE01"), "wrong type");
        assertThrows(ParkException.class, () -> passes.refundSpecialPass("SP02:1"), "wrong type");
        passes.refundSpecialPass("PE01");
        assertTrue(t001.getSpecialPass().isEmpty());
        assertNotNull(passes.buySpecialPass("PE02", "T001"), "a refunded Special Pass may be bought again");
    }

    @Test
    void refundRejectedWhenConsumedOrNotFuture() {
        passes.buySpeedPasses("SP01", "T001", "1");
        passes.buySpeedPasses("SP02", "T001", "1");
        park.findPass("SP01:1").consume();
        assertThrows(ParkException.class, () -> passes.refundSpeedPass("SP01:1"));
        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        assertThrows(ParkException.class, () -> passes.refundSpeedPass("SP02:1"));
        assertNotNull(park.findPass("SP02:1"));
        assertThrows(ParkException.class, () -> passes.refundSpeedPass("NOPE"));
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        passes.buySpeedPasses("SP01", "T001", "1");
        passes.buySpeedPasses("SP02", "T001", "10");
        passes.buySpecialPass("PE01", "T001");
        park.deleteTicket("T001");
        assertNull(park.findTicket("T001"));
        assertTrue(park.allPasses().isEmpty());
    }

    @Test
    void groupTicketDeletionCascadesToPasses() {
        user(2);
        park.createGroup("G01", "G");
        park.addToGroup("G01", email(2));
        GroupTicket tg = park.createGroupTicket("TG01", "G01", "2027-04-15");
        String memberId = tg.getComposition().get(0).getId();
        passes.buySpeedPasses("SPG", memberId, "10");
        passes.buySpecialPass("PEG", memberId);
        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket(memberId));
        assertNull(park.findPass("SPG:1"));
        assertNull(park.findPass("PEG"));
    }

    @Test
    void consumedPassesKeptWhileTicketExists() {
        passes.buySpeedPasses("SP01", "T001", "1");
        park.findPass("SP01:1").consume();
        park.setCurrentDate(LocalDate.of(2027, 5, 1));
        assertTrue(park.findPass("SP01:1").isConsumed());
    }
}
