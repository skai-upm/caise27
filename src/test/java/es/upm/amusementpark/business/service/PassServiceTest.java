package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.exception.PassOperationException;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.ParkRepositories;
import es.upm.amusementpark.persistence.PassRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 1);
    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final ParkController park = ParkController.getInstance();
    private final PassService passService = PassService.getInstance();
    private PassRepository passes;

    @BeforeEach
    void setUp() throws Exception {
        ParkRepositories repositories = ParkRepositories.inMemory();
        park.initialize(repositories, TODAY);
        passes = repositories.passes();
        UserService.getInstance().createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");
        UserService.getInstance().createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
                "623456789", "9876543210987654");
        TicketService.getInstance().createTicket("T001", "ana@correo.com", DAY);
        park.setCurrentPrice(new BigDecimal("50.00"));
    }

    @Test
    void singleSpeedPassUsesTheTicketPrice() throws PassOperationException {
        List<SpeedPass> bought = passService.buySpeedPasses("SP01", "T001", 1);
        assertEquals("SP01:1", bought.get(0).getIdentifier());
        assertEquals(new BigDecimal("8.00"), bought.get(0).getPrice());
        assertFalse(bought.get(0).isConsumed());
    }

    @Test
    void packOfTenCreatesIndependentPasses() throws PassOperationException {
        List<SpeedPass> bought = passService.buySpeedPasses("SP02", "T001", 10);
        assertEquals(10, bought.size());
        assertTrue(passes.exists("SP02:1") && passes.exists("SP02:10"));
        BigDecimal total = bought.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("80.00"), total);
    }

    @Test
    void invalidSpeedPassPurchasesCreateNothing() throws Exception {
        assertThrows(PassOperationException.class, () -> passService.buySpeedPasses("SP03", "T001", 5));
        assertThrows(PassOperationException.class, () -> passService.buySpeedPasses("SP03", "T999", 1));
        park.setCurrentDate(DAY.plusDays(1));
        assertThrows(PassOperationException.class, () -> passService.buySpeedPasses("SP03", "T001", 1));
        assertTrue(passes.findAll().isEmpty());
    }

    @Test
    void identifierConflictPreventsPartialPack() throws PassOperationException {
        passService.buySpecialPass("SP04:7", "T001");
        assertThrows(PassOperationException.class, () -> passService.buySpeedPasses("SP04", "T001", 10));
        assertEquals(1, passes.findAll().size());
    }

    @Test
    void oneSpecialPassPerTicketEvenWhenConsumed() throws PassOperationException {
        SpecialPass pass = passService.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pass.getPrice());
        assertThrows(PassOperationException.class, () -> passService.buySpecialPass("PE02", "T001"));
        pass.consume();
        assertThrows(PassOperationException.class, () -> passService.buySpecialPass("PE02", "T001"));
        assertFalse(passes.exists("PE02"));
    }

    @Test
    void duplicatePassIdentifierIsRejected() throws Exception {
        TicketService.getInstance().createTicket("T002", "pedro@correo.com", DAY);
        passService.buySpecialPass("PE01", "T001");
        assertThrows(PassOperationException.class, () -> passService.buySpecialPass("PE01", "T002"));
    }

    @Test
    void composedTicketPassesUseTheDiscountedPrice() throws Exception {
        GroupService.getInstance().createGroup("G01", "Familia");
        GroupService.getInstance().addUser("G01", "ana@correo.com");
        GroupService.getInstance().addUser("G01", "pedro@correo.com");
        park.setCurrentPrice(new BigDecimal("40.00"));
        TicketService.getInstance().createGroupTicket("TG01", "G01", DAY);
        assertEquals(new BigDecimal("40.00"), passService.buySpecialPass("PE09", "TG01:12345678Z").getPrice());
        assertThrows(PassOperationException.class, () -> passService.buySpecialPass("PE10", "TG01"));
    }

    @Test
    void unconsumedFuturePassesCanBeRefundedIndividually() throws PassOperationException {
        passService.buySpeedPasses("SP02", "T001", 10);
        passService.refund("SP02:3", SpeedPass.class);
        assertFalse(passes.exists("SP02:3"));
        assertTrue(passes.exists("SP02:4"));
    }

    @Test
    void refundsAreRejectedWhenNotAllowed() throws PassOperationException {
        passService.buySpeedPasses("SP01", "T001", 1);
        passService.buySpecialPass("PE01", "T001");
        assertThrows(PassOperationException.class, () -> passService.refund("SP01:1", SpecialPass.class));
        assertThrows(PassOperationException.class, () -> passService.refund("SP99:1", SpeedPass.class));
        passes.findById("SP01:1").orElseThrow().consume();
        assertThrows(PassOperationException.class, () -> passService.refund("SP01:1", SpeedPass.class));
        park.setCurrentDate(DAY);
        assertThrows(PassOperationException.class, () -> passService.refund("PE01", SpecialPass.class));
        assertEquals(2, passes.findAll().size());
    }

    @Test
    void deletingATicketRemovesAllItsPasses() throws Exception {
        passService.buySpeedPasses("SP01", "T001", 1);
        passService.buySpeedPasses("SP02", "T001", 10);
        passService.buySpecialPass("PE01", "T001");
        TicketService.getInstance().deleteTicket("T001");
        assertTrue(passes.findAll().isEmpty());
    }

    @Test
    void deletingAGroupTicketRemovesPassesOfItsComposition() throws Exception {
        GroupService.getInstance().createGroup("G01", "Familia");
        GroupService.getInstance().addUser("G01", "ana@correo.com");
        GroupService.getInstance().addUser("G01", "pedro@correo.com");
        TicketService.getInstance().createGroupTicket("TG01", "G01", DAY);
        passService.buySpeedPasses("SP05", "TG01:12345678Z", 1);
        passService.buySpecialPass("PE05", "TG01:87654321X");
        passService.buySpeedPasses("SP06", "T001", 1);
        TicketService.getInstance().deleteGroupTicket("TG01");
        assertEquals(List.of("SP06:1"), passes.findAll().stream().map(Pass::getIdentifier).toList());
        assertFalse(park.getTickets().exists("TG01:12345678Z"));
    }

    @Test
    void refusedTicketDeletionKeepsPasses() throws Exception {
        passService.buySpeedPasses("SP01", "T001", 1);
        park.setCurrentDate(DAY);
        assertThrows(Exception.class, () -> TicketService.getInstance().deleteTicket("T001"));
        assertTrue(passes.exists("SP01:1"));
        assertTrue(park.getTickets().exists("T001"));
    }
}
