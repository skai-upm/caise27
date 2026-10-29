package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.PassController;
import es.upm.etsisi.amusementpark.business.exception.ConflictException;
import es.upm.etsisi.amusementpark.business.exception.ForbiddenOperationException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassTest extends ParkFixture {

    PassController passes;

    @BeforeEach
    void tickets() throws Exception {
        passes = PassController.getInstance();
        park.createTicket("T001", ANA, "2027-04-15");
        park.createTicket("T005", ANA, "2027-04-20");
        park.createGroup("G01", "Familia");
        addMembers("G01", 6);
        park.createGroupTicket("TG01", "G01", "2027-04-20");
    }

    @Test
    void speedPassPricesAndIds() throws Exception {
        park.setCurrentPrice("50");
        List<SpeedPass> one = park.buySpeedPasses("SP01", "T001", "1");
        assertEquals(1, one.size());
        assertEquals("SP01:1", one.get(0).getId());
        assertEquals(new BigDecimal("8.00"), one.get(0).getPrice());
        List<SpeedPass> ten = park.buySpeedPasses("SP02", "T001", "10");
        assertEquals(10, ten.size());
        assertEquals("SP02:1", ten.get(0).getId());
        assertEquals("SP02:10", ten.get(9).getId());
        assertSame(park.findTicket("T001"), ten.get(9).getTicket());
        assertTrue(ten.stream().noneMatch(SpeedPass::isConsumed));
    }

    @Test
    void passPricesUseDiscountedCompositionTicket() throws Exception {
        String member = "TG01:" + validDni(10000000);
        assertEquals(new BigDecimal("6.40"), park.buySpeedPasses("SP03", member, "1").get(0).getPrice());
        assertEquals(new BigDecimal("32.00"), park.buySpecialPass("PE09", member).getPrice());
    }

    @Test
    void commandShowsTotal() {
        CommandProcessor processor = processor();
        assertEquals("SPEEDPASS CREATED: SP02:1, SP02:2, SP02:3, SP02:4, SP02:5, SP02:6, SP02:7, SP02:8, SP02:9, "
                + "SP02:10. TOTAL PRICE: 80.00 EUR", processor.process("SPEEDPASS CREATE;SP02;T001;10"));
        assertEquals("SPECIALPASS CREATED. PRICE: 40.00 EUR", processor.process("SPECIALPASS CREATE;PE01;T001"));
    }

    @Test
    void invalidSpeedPassPurchases() throws Exception {
        assertThrows(ValidationException.class, () -> park.buySpeedPasses("SP01", "T001", "5"));
        assertThrows(NotFoundException.class, () -> park.buySpeedPasses("SP01", "NOPE", "1"));
        assertThrows(NotFoundException.class, () -> park.buySpeedPasses("SP01", "TG01", "1"));
        park.setCurrentDate("2027-04-16");
        assertThrows(ForbiddenOperationException.class, () -> park.buySpeedPasses("SP01", "T001", "1"));
        assertTrue(passes.getPasses().isEmpty());
    }

    @Test
    void packageConflictCreatesNothing() throws Exception {
        park.buySpecialPass("SP02:3", "T001");
        assertThrows(ConflictException.class, () -> park.buySpeedPasses("SP02", "T005", "10"));
        assertEquals(1, passes.getPasses().size());
        assertNull(passes.findPass("SP02:1"));
    }

    @Test
    void onlyOneSpecialPassPerTicket() throws Exception {
        SpecialPass pass = park.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());
        assertThrows(ConflictException.class, () -> park.buySpecialPass("PE02", "T001"));
        pass.consume();
        assertThrows(ConflictException.class, () -> park.buySpecialPass("PE02", "T001"));
        assertThrows(ConflictException.class, () -> park.buySpecialPass("PE01", "T005"));
        assertThrows(NotFoundException.class, () -> park.buySpecialPass("PE03", "TG01"));
    }

    @Test
    void refundRules() throws Exception {
        park.buySpeedPasses("SP02", "T005", "10");
        park.buySpecialPass("PE05", "T005");
        park.buySpeedPasses("SP01", "T001", "1");

        park.refundSpeedPass("SP02:4");
        assertNull(passes.findPass("SP02:4"));
        assertNotNull(passes.findPass("SP02:5"));

        assertThrows(NotFoundException.class, () -> park.refundSpeedPass("PE05"));
        assertThrows(NotFoundException.class, () -> park.refundSpecialPass("SP02:5"));
        assertThrows(ForbiddenOperationException.class, () -> park.refundSpeedPass("SP01:1"));

        passes.findPass("SP02:5").consume();
        assertThrows(ForbiddenOperationException.class, () -> park.refundSpeedPass("SP02:5"));

        park.refundSpecialPass("PE05");
        assertNull(passes.findPass("PE05"));
        park.buySpecialPass("PE06", "T005");
    }

    @Test
    void ticketDeletionCascadesToPasses() throws Exception {
        park.buySpeedPasses("SP02", "T005", "10");
        park.buySpeedPasses("SP03", "T005", "1");
        park.buySpecialPass("PE05", "T005");
        passes.findPass("SP03:1").consume();
        park.buySpeedPasses("SP01", "T001", "1");
        park.deleteTicket("T005");
        assertEquals(1, passes.getPasses().size());
        assertNotNull(passes.findPass("SP01:1"));
    }

    @Test
    void groupTicketDeletionCascadesToCompositionPasses() throws Exception {
        String member = "TG01:" + validDni(10000000);
        park.buySpeedPasses("SP04", member, "10");
        park.buySpecialPass("PE04", member);
        park.buySpeedPasses("SP01", "T001", "1");
        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket(member));
        assertEquals(1, passes.getPasses().size());
    }

    @Test
    void failedTicketDeletionKeepsPasses() throws Exception {
        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertInstanceOf(SpeedPass.class, passes.findPass("SP01:1"));
    }
}
