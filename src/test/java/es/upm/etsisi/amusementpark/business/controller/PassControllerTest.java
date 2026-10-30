package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.exception.DuplicateException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PassControllerTest extends ParkFixture {

    private PassController passes;
    private TicketController tickets;

    @BeforeEach
    void setUp() {
        passes = PassController.getInstance();
        tickets = TicketController.getInstance();
        tickets.createTicket("T001", "ana@correo.com", TODAY.plusDays(1).toString());
    }

    @Test
    void speedPassPackageOfTen() {
        List<SpeedPass> created = passes.createSpeedPasses("SP02", "T001", "10");
        assertEquals(10, created.size());
        assertEquals("SP02:1", created.get(0).getId());
        assertEquals("SP02:10", created.get(9).getId());
        assertEquals(new BigDecimal("8.00"), created.get(0).getPrice());
        assertEquals(new BigDecimal("80.00"), PassController.total(created));
        assertFalse(created.get(0).isConsumed());
    }

    @Test
    void singleSpeedPassIdentifier() {
        assertEquals("SP01:1", passes.createSpeedPasses("SP01", "T001", "1").get(0).getId());
    }

    @Test
    void pricesUseStoredTicketPrice() {
        park.setPrice("50");
        assertEquals(new BigDecimal("8.00"), passes.createSpeedPasses("SP01", "T001", "1").get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), passes.createSpecialPass("PE01", "T001").getPrice());
    }

    @Test
    void groupDiscountedTicketPrice() {
        GroupController groups = GroupController.getInstance();
        groups.create("G02", "Grande");
        groups.addUser("G02", "ana@correo.com");
        groups.addUser("G02", "pedro@correo.com");
        for (int i = 1; i <= 4; i++) {
            groups.addUser("G02", createUser(i).getEmail());
        }
        tickets.createGroupTicket("TG02", "G02", TODAY.toString());
        assertEquals(new BigDecimal("6.40"),
                passes.createSpeedPasses("SPG", "TG02:12345678Z", "1").get(0).getPrice());
        assertEquals(new BigDecimal("32.00"), passes.createSpecialPass("PEG", "TG02:12345678Z").getPrice());
    }

    @Test
    void invalidQuantities() {
        for (String q : new String[]{"5", "0", "2", "x", "-1"}) {
            assertThrows(ValidationException.class, () -> passes.createSpeedPasses("SP03", "T001", q));
        }
        assertTrue(park.passes().findAll().isEmpty());
    }

    @Test
    void purchaseRequiresExistingCurrentIndividualTicket() {
        assertThrows(NotFoundException.class, () -> passes.createSpeedPasses("SP01", "T999", "1"));
        GroupController.getInstance().create("G01", "F");
        GroupController.getInstance().addUser("G01", "ana@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.toString());
        assertThrows(NotFoundException.class, () -> passes.createSpeedPasses("SP01", "TG01", "1"));
        tickets.createTicket("T002", "pedro@correo.com", TODAY.toString());
        park.setDate(TODAY.plusDays(1).toString());
        assertThrows(OperationNotAllowedException.class, () -> passes.createSpeedPasses("SP01", "T002", "1"));
        assertThrows(OperationNotAllowedException.class, () -> passes.createSpecialPass("PE01", "T002"));
        assertTrue(park.passes().findAll().isEmpty());
    }

    @Test
    void packageWithOneConflictIsRejectedEntirely() {
        passes.createSpecialPass("SP04:3", "T001");
        assertThrows(DuplicateException.class, () -> passes.createSpeedPasses("SP04", "T001", "10"));
        assertEquals(1, park.passes().findAll().size());
        assertTrue(park.passes().findById("SP04:1").isEmpty());
    }

    @Test
    void onlyOneSpecialPassPerTicket() {
        SpecialPass pass = passes.createSpecialPass("PE01", "T001");
        assertThrows(OperationNotAllowedException.class, () -> passes.createSpecialPass("PE02", "T001"));
        pass.consume();
        assertThrows(OperationNotAllowedException.class, () -> passes.createSpecialPass("PE02", "T001"));
    }

    @Test
    void refundSinglePassOfPackage() {
        passes.createSpeedPasses("SP02", "T001", "10");
        passes.deleteSpeedPass("SP02:3");
        assertTrue(park.passes().findById("SP02:3").isEmpty());
        assertEquals(9, park.passes().findAll().size());
    }

    @Test
    void refundRules() {
        passes.createSpeedPasses("SP01", "T001", "1");
        passes.createSpecialPass("PE01", "T001");
        assertThrows(NotFoundException.class, () -> passes.deleteSpeedPass("PE01"));
        assertThrows(NotFoundException.class, () -> passes.deleteSpecialPass("SP01:1"));
        assertThrows(NotFoundException.class, () -> passes.deleteSpeedPass("NOPE"));

        park.passes().findById("SP01:1").orElseThrow().consume();
        assertThrows(OperationNotAllowedException.class, () -> passes.deleteSpeedPass("SP01:1"));

        park.setDate(TODAY.plusDays(1).toString());
        assertThrows(OperationNotAllowedException.class, () -> passes.deleteSpecialPass("PE01"));
        assertEquals(2, park.passes().findAll().size());

        park.setDate(TODAY.toString());
        passes.deleteSpecialPass("PE01");
        assertEquals(1, park.passes().findAll().size());
    }

    @Test
    void ticketDeletionCascadesToPasses() {
        passes.createSpeedPasses("SP01", "T001", "1");
        passes.createSpeedPasses("SP02", "T001", "10");
        passes.createSpecialPass("PE01", "T001");
        tickets.createTicket("T002", "ana@correo.com", TODAY.plusDays(1).toString());
        passes.createSpeedPasses("SPX", "T002", "1");

        tickets.deleteTicket("T001");

        assertEquals(List.of("SPX:1"), park.passes().findAll().stream().map(Pass::getId).toList());
        restart();
        assertEquals(1, park.passes().findAll().size());
    }

    @Test
    void groupTicketDeletionCascadesToMemberPasses() {
        GroupController.getInstance().create("G01", "F");
        GroupController.getInstance().addUser("G01", "ana@correo.com");
        GroupController.getInstance().addUser("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", TODAY.plusDays(2).toString());
        passes.createSpeedPasses("SPA", "TG01:12345678Z", "10");
        passes.createSpecialPass("PEB", "TG01:87654321X");

        tickets.deleteGroupTicket("TG01");

        assertTrue(park.passes().findAll().isEmpty());
        assertFalse(park.tickets().existsId("TG01:12345678Z"));
    }

    @Test
    void failedTicketDeletionKeepsPasses() {
        tickets.createTicket("T002", "pedro@correo.com", TODAY.toString());
        passes.createSpecialPass("PE02", "T002");
        assertThrows(OperationNotAllowedException.class, () -> tickets.deleteTicket("T002"));
        assertTrue(park.passes().findById("PE02").isPresent());
    }

    @Test
    void passesSurviveRestart() {
        passes.createSpeedPasses("SP02", "T001", "10");
        passes.createSpecialPass("PE01", "T001");
        park.passes().findById("SP02:4").orElseThrow().consume();
        passes.save();
        restart();
        assertEquals(11, park.passes().findAll().size());
        Pass consumed = park.passes().findById("SP02:4").orElseThrow();
        assertTrue(consumed.isConsumed());
        assertInstanceOf(SpeedPass.class, consumed);
        assertSame(park.tickets().findById("T001").orElseThrow(), consumed.getTicket());
        Pass special = park.passes().findById("PE01").orElseThrow();
        assertInstanceOf(SpecialPass.class, special);
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertFalse(special.isConsumed());
    }
}
