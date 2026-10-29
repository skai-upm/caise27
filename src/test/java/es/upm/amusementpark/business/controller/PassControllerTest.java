package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.amusementpark.business.exception.DuplicateException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.SpecialPass;
import es.upm.amusementpark.business.model.pass.SpeedPass;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * SpeedPasses and Special Passes: purchase, prices, refunds and cascades (Delivery 3, F3-C06).
 */
class PassControllerTest extends ParkTestSupport {

    @BeforeEach
    void ticket() throws Exception {
        createAna();
        createPedro();
        park.createTicket("T001", "ana@correo.com", TODAY_TEXT);
        park.createTicket("T002", "ana@correo.com", TOMORROW_TEXT);
    }

    @Test
    void pricesComeFromStoredTicketPrice() throws Exception {
        park.setCurrentPrice("50");
        List<SpeedPass> speed = park.buySpeedPasses("SP01", "T001", "1");
        SpecialPass special = park.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("8.00"), speed.get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertSame(park.findTicket("T001"), special.getTicket());
        assertFalse(special.isConsumed());
    }

    @Test
    void discountedCompositionTicketPrices() throws Exception {
        createGroupWith("G01", 1, 6);
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        String memberTicket = "TG01:" + park.findUser("user1@correo.com").getDni();
        assertEquals(new BigDecimal("6.40"), park.buySpeedPasses("SP01", memberTicket, "1").get(0).getPrice());
        assertEquals(new BigDecimal("32.00"), park.buySpecialPass("PE01", memberTicket).getPrice());
    }

    @Test
    void packOfTenNamingAndTotal() throws Exception {
        List<SpeedPass> pack = park.buySpeedPasses("SP02", "T001", "10");
        assertEquals(10, pack.size());
        for (int i = 1; i <= 10; i++) {
            assertNotNull(park.findPass("SP02:" + i));
        }
        BigDecimal total = pack.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("80.00"), total);
        assertEquals("SP01:1", park.buySpeedPasses("SP01", "T001", "1").get(0).getId());
    }

    @Test
    void invalidPurchasesCreateNothing() throws Exception {
        assertThrows(ValidationException.class, () -> park.buySpeedPasses("SP01", "T001", "2"));
        assertThrows(NotFoundException.class, () -> park.buySpeedPasses("SP01", "T999", "1"));
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY_TEXT);
        assertThrows(OperationNotAllowedException.class, () -> park.buySpeedPasses("SP01", "TG01", "1"));
        assertThrows(OperationNotAllowedException.class, () -> park.buySpecialPass("PE01", "TG01"));
        park.setCurrentDate(TOMORROW_TEXT);
        assertThrows(OperationNotAllowedException.class, () -> park.buySpeedPasses("SP01", "T001", "1"));
        assertThrows(OperationNotAllowedException.class, () -> park.buySpecialPass("PE01", "T001"));
        assertEquals(0, park.getPasses().size());
    }

    @Test
    void identifierConflictIsAtomic() throws Exception {
        park.buySpeedPasses("SP03:5", "T001", "1");
        park.buySpecialPass("SP03:4", "T002");
        assertEquals("SP03:5:1", park.findPass("SP03:5:1").getId());
        assertThrows(DuplicateException.class, () -> park.buySpeedPasses("SP03", "T001", "10"));
        assertEquals(2, park.getPasses().size());
        assertNull(park.findPass("SP03:1"));
        assertThrows(DuplicateException.class, () -> park.buySpecialPass("SP03:4", "T001"));
    }

    @Test
    void oneSpecialPassPerTicketEvenAfterConsumption() throws Exception {
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.buySpecialPass("PE01", "T001");
        assertThrows(DuplicateException.class, () -> park.buySpecialPass("PE02", "T001"));
        park.enterAttraction("A04", "T001", false);
        assertThrows(DuplicateException.class, () -> park.buySpecialPass("PE02", "T001"));
    }

    @Test
    void refundsOnlyUnconsumedPassesOfFutureTickets() throws Exception {
        park.buySpeedPasses("SP02", "T002", "10");
        park.buySpecialPass("PE02", "T002");
        park.refundSpeedPass("SP02:3");
        assertNull(park.findPass("SP02:3"));
        assertEquals(10, park.getPasses().size());
        assertThrows(NotFoundException.class, () -> park.refundSpecialPass("SP02:4"));
        assertThrows(NotFoundException.class, () -> park.refundSpeedPass("PE02"));
        park.refundSpecialPass("PE02");
        park.buySpecialPass("PE03", "T002");

        park.buySpeedPasses("SP01", "T001", "1");
        assertThrows(OperationNotAllowedException.class, () -> park.refundSpeedPass("SP01:1"));
        assertNotNull(park.findPass("SP01:1"));
    }

    @Test
    void consumedPassCannotBeRefunded() throws Exception {
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.buySpeedPasses("SP01", "T001", "1");
        park.enterAttraction("A01", "T001", true);
        park.setCurrentDate(YESTERDAY_TEXT);
        ParkException e = assertThrows(OperationNotAllowedException.class, () -> park.refundSpeedPass("SP01:1"));
        assertEquals("pass already consumed: SP01:1", e.getMessage());
    }

    @Test
    void ticketDeletionCascadesPasses() throws Exception {
        park.buySpeedPasses("SP01", "T002", "1");
        park.buySpeedPasses("SP02", "T002", "10");
        park.buySpecialPass("PE01", "T002");
        park.buySpeedPasses("SP03", "T001", "1");
        park.deleteTicket("T002");
        assertNull(park.findTicket("T002"));
        assertEquals(1, park.getPasses().size());
        assertNotNull(park.findPass("SP03:1"));
    }

    @Test
    void groupTicketDeletionCascadesPasses() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TOMORROW_TEXT);
        park.buySpeedPasses("SP01", "TG01:12345678Z", "10");
        park.buySpecialPass("PE01", "TG01:87654321X");
        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findTicket("TG01:12345678Z"));
        assertEquals(0, park.getPasses().size());
    }

    @Test
    void rejectedTicketDeletionKeepsPasses() throws Exception {
        park.buySpecialPass("PE01", "T001");
        assertThrows(OperationNotAllowedException.class, () -> park.deleteTicket("T001"));
        assertNotNull(park.findPass("PE01"));
    }
}
