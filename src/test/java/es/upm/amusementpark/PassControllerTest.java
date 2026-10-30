package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.invoice.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PassControllerTest extends ParkTestSupport {

    @Test
    void pricesComeFromStoredTicketPrice() {
        user(9);
        tickets.createIndividual("T001", email(9), "2027-04-15");
        groupWith("G01", 6);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        system.setPrice("99");
        assertEquals(new BigDecimal("8.00"), passes.buySpeedPasses("SP01", "T001", "1").get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), passes.buySpecialPass("PE01", "T001").getPrice());
        String composed = "TG01:" + dni(1);
        assertEquals(new BigDecimal("6.40"), passes.buySpeedPasses("SP02", composed, "1").get(0).getPrice());
        assertEquals(new BigDecimal("32.00"), passes.buySpecialPass("PE02", composed).getPrice());
    }

    @Test
    void packageOfTenHasDerivedIdsAndNoDiscount() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        List<SpeedPass> bought = passes.buySpeedPasses("SP02", "T001", "10");
        assertEquals(10, bought.size());
        assertEquals("SP02:1", bought.get(0).getId());
        assertEquals("SP02:10", bought.get(9).getId());
        assertEquals("SP01:1", passes.buySpeedPasses("SP01", "T001", "1").get(0).getId());
        assertEquals("10 SPEEDPASS(ES) CREATED (SP03:1 - SP03:10). TOTAL PRICE: 80.00 EUR",
                processor.process("SPEEDPASS CREATE;SP03;T001;10").output());
    }

    @Test
    void invalidPurchasesCreateNothing() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        tickets.createIndividual("T000", email(1), "2027-04-10");
        assertThrows(InvalidDataException.class, () -> passes.buySpeedPasses("SP01", "T001", "5"));
        passes.buySpeedPasses("SPX", "T001", "1");
        passes.buySpecialPass("SP02:3", "T001");
        assertThrows(DuplicateException.class, () -> passes.buySpeedPasses("SP02", "T001", "10"));
        assertNull(park.getPasses().findById("SP02:1"));
        assertThrows(NotFoundException.class, () -> passes.buySpeedPasses("SP04", "T999", "1"));
        system.setDate("2027-04-11");
        assertThrows(OperationNotAllowedException.class, () -> passes.buySpeedPasses("SP05", "T000", "1"));
        assertThrows(OperationNotAllowedException.class, () -> passes.buySpecialPass("PE09", "T000"));
        assertEquals(2, park.getPasses().findAll().size());
    }

    @Test
    void groupTicketIdRejected() {
        groupWith("G01", 2);
        tickets.createGroup("TG01", "G01", "2027-04-15");
        assertThrows(InvalidDataException.class, () -> passes.buySpeedPasses("SP01", "TG01", "1"));
        assertThrows(InvalidDataException.class, () -> passes.buySpecialPass("PE01", "TG01"));
    }

    @Test
    void oneSpecialPassPerTicketEvenWhenConsumed() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        attractions.create("A04", "Pasaje del Terror", "15", "SPECIAL");
        passes.buySpecialPass("PE01", "T001");
        attractions.enter("A04", "T001");
        assertTrue(park.getPasses().findById("PE01").isConsumed());
        assertThrows(OperationNotAllowedException.class, () -> passes.buySpecialPass("PE02", "T001"));
    }

    @Test
    void refunds() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        tickets.createIndividual("T002", email(1), "2027-04-10");
        passes.buySpeedPasses("SP02", "T001", "10");
        passes.buySpecialPass("PE01", "T001");
        passes.buySpeedPasses("SP09", "T002", "1");
        passes.refundSpeedPass("SP02:4");
        assertNull(park.getPasses().findById("SP02:4"));
        assertEquals(11, park.getPasses().findAll().size());
        assertThrows(NotFoundException.class, () -> passes.refundSpeedPass("PE01"));
        assertThrows(NotFoundException.class, () -> passes.refundSpecialPass("SP02:1"));
        passes.refundSpecialPass("PE01");
        assertThrows(OperationNotAllowedException.class, () -> passes.refundSpeedPass("SP09:1"));
        system.setDate("2027-04-15");
        assertThrows(OperationNotAllowedException.class, () -> passes.refundSpeedPass("SP02:1"));
    }

    @Test
    void consumedPassCannotBeRefunded() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        passes.buySpeedPasses("SP01", "T001", "1");
        attractions.create("A01", "Dragon Rojo", "20");
        attractions.enter("A01", "T001", true);
        system.setDate("2027-04-01");
        assertThrows(OperationNotAllowedException.class, () -> passes.refundSpeedPass("SP01:1"));
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-15");
        passes.buySpeedPasses("SP01", "T001", "1");
        passes.buySpeedPasses("SP02", "T001", "10");
        passes.buySpecialPass("PE01", "T001");
        tickets.deleteIndividual("T001");
        assertTrue(park.getPasses().findAll().isEmpty());
    }

    @Test
    void groupTicketDeletionCascadesToCompositionPasses() {
        groupWith("G01", 2);
        user(9);
        tickets.createIndividual("T009", email(9), "2027-04-15");
        tickets.createGroup("TG01", "G01", "2027-04-15");
        passes.buySpeedPasses("SP01", "TG01:" + dni(1), "1");
        passes.buySpecialPass("PE02", "TG01:" + dni(2));
        passes.buySpeedPasses("SP09", "T009", "1");
        tickets.deleteGroup("TG01");
        assertNull(park.getTickets().findById("TG01:" + dni(1)));
        assertEquals(List.of("SP09:1"), park.getPasses().findAll().stream().map(Pass::getId).toList());
    }

    @Test
    void failedTicketDeletionKeepsPasses() {
        user(1);
        tickets.createIndividual("T001", email(1), "2027-04-10");
        passes.buySpeedPasses("SP01", "T001", "1");
        assertThrows(OperationNotAllowedException.class, () -> tickets.deleteIndividual("T001"));
        assertNotNull(park.getPasses().findById("SP01:1"));
    }
}
