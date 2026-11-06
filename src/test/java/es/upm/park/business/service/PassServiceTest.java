package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.invoice.Invoice;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.persistence.repository.ParkRepositories;

class PassServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private final ParkController park = ParkController.getInstance();
    private ParkRepositories repos;
    private PassService passes;

    @BeforeEach
    void setUp() {
        repos = ParkRepositories.inMemory();
        park.start(repos, TODAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getUserService().create("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.getUserService().create("Luis", "M", "00000000T", "luis@correo.com", "Clave1!", "6", "1111222233334444");
        park.getTicketService().createIndividual("T001", "ana@correo.com", TODAY);
        park.getTicketService().createIndividual("T002", "ana@correo.com", TODAY.plusDays(5));
        passes = park.getPassService();
    }

    private void assertRejected(String message, Runnable action) {
        int before = repos.passes().findAll().size();
        ParkException e = assertThrows(ParkException.class, action::run);
        assertEquals(message, e.getMessage());
        assertEquals(before, repos.passes().findAll().size());
    }

    @Test
    void speedPassPackagesAndSingles() {
        List<SpeedPass> ten = passes.buySpeedPasses("SP02", "T001", "10");
        assertEquals(10, ten.size());
        assertEquals("SP02:1", ten.get(0).getId());
        assertEquals("SP02:10", ten.get(9).getId());
        assertEquals(new BigDecimal("80.00"), PassService.total(ten));
        assertEquals("SP01:1", passes.buySpeedPasses("SP01", "T001", "1").get(0).getId());
        park.setBasePrice("50");
        assertEquals(new BigDecimal("8.00"), passes.buySpeedPasses("SP03", "T001", "1").get(0).getPrice());
    }

    @Test
    void speedPassRejections() {
        passes.buySpeedPasses("SP01", "T001", "1");
        assertRejected("quantity must be 1 or 10", () -> passes.buySpeedPasses("SP09", "T001", "5"));
        assertRejected("no individual ticket T999", () -> passes.buySpeedPasses("SP09", "T999", "1"));
        assertRejected("pass identifier SP01:1 already exists", () -> passes.buySpeedPasses("SP01", "T001", "10"));
        park.setCurrentDate("2027-04-16");
        assertRejected("ticket T001 is dated before the current date 2027-04-16",
                () -> passes.buySpeedPasses("SP05", "T001", "1"));
    }

    @Test
    void oneSpecialPassPerTicketEvenIfConsumed() {
        SpecialPass pe = passes.buySpecialPass("PE01", "T001");
        assertEquals(new BigDecimal("40.00"), pe.getPrice());
        pe.consume();
        assertRejected("ticket T001 already has a special pass", () -> passes.buySpecialPass("PE02", "T001"));
        assertRejected("pass identifier PE01 already exists", () -> passes.buySpecialPass("PE01", "T002"));
        passes.buySpecialPass("T001", "T002");
    }

    @Test
    void passesOnGroupTicketConstituentsUseDiscountedPrice() {
        park.getGroupService().create("G01", "F");
        for (String email : new String[] {"ana@correo.com", "pedro@correo.com", "luis@correo.com"}) {
            park.getGroupService().addUser("G01", email);
        }
        park.createGroupTicket("TG01", "G01", TODAY);
        assertEquals(new BigDecimal("7.20"), passes.buySpeedPasses("SP01", "TG01:12345678Z", "1").get(0).getPrice());
        assertEquals(new BigDecimal("36.00"), passes.buySpecialPass("PE01", "TG01:12345678Z").getPrice());
    }

    @Test
    void refundsOnlyUnconsumedPassesOfFutureTickets() {
        passes.buySpeedPasses("SP02", "T002", "10");
        assertEquals("SP02:3", passes.refundSpeedPass("SP02:3").getId());
        assertEquals(9, repos.passes().findAll().size());
        repos.passes().findById("SP02:4").orElseThrow().consume();
        assertRejected("speed pass SP02:4 has been consumed", () -> passes.refundSpeedPass("SP02:4"));
        passes.buySpeedPasses("SP01", "T001", "1");
        assertRejected("speed pass SP01:1 cannot be refunded: its ticket is not dated after 2027-04-15",
                () -> passes.refundSpeedPass("SP01:1"));
        assertRejected("no speed pass SP02:3", () -> passes.refundSpeedPass("SP02:3"));
        passes.buySpecialPass("PE01", "T002");
        assertRejected("no speed pass PE01", () -> passes.refundSpeedPass("PE01"));
        assertRejected("no special pass SP02:1", () -> passes.refundSpecialPass("SP02:1"));
        assertEquals("PE01", passes.refundSpecialPass("PE01").getId());
    }

    @Test
    void ticketRefundRemovesItsPassesAndRejectedRefundKeepsThem() {
        passes.buySpeedPasses("SP01", "T002", "1");
        passes.buySpeedPasses("SP02", "T002", "10");
        passes.buySpecialPass("PE01", "T002");
        passes.buySpeedPasses("SP03", "T001", "1");
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertTrue(repos.passes().existsId("SP03:1"));
        park.deleteTicket("T002");
        assertEquals(List.of("SP03:1"), repos.passes().findAll().stream().map(Pass::getId).toList());
    }

    @Test
    void groupTicketRefundRemovesConstituentPasses() {
        park.getGroupService().create("G01", "F");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.getGroupService().addUser("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(3));
        passes.buySpeedPasses("SP01", "TG01:12345678Z", "1");
        passes.buySpecialPass("PE01", "TG01:87654321X");
        park.deleteGroupTicket("TG01");
        assertTrue(repos.passes().findAll().isEmpty());
        assertFalse(repos.tickets().existsId("TG01:12345678Z"));
    }

    @Test
    void userInvoiceIncludesOwnAndConstituentPassesButGroupInvoiceDoesNot() {
        park.getGroupService().create("G01", "F");
        for (String email : new String[] {"ana@correo.com", "pedro@correo.com", "luis@correo.com"}) {
            park.getGroupService().addUser("G01", email);
        }
        park.createGroupTicket("TG01", "G01", TODAY);
        passes.buySpeedPasses("SP01", "T001", "1");
        passes.buySpecialPass("PE01", "TG01:12345678Z");
        park.getTicketService().deleteIndividual("T002");
        Invoice user = park.getInvoiceService().userInvoice("ana@correo.com");
        String text = user.render();
        assertTrue(text.contains("SpeedPasses:"));
        assertTrue(text.contains("SpeedPass SP01:1 (ticket T001)  8.00"));
        assertTrue(text.contains("Special pass PE01 (ticket TG01:12345678Z)  36.00"));
        assertFalse(text.contains("Ticket TG01"));
        assertEquals(new BigDecimal("84.00"), user.getTotal());
        Invoice group = park.getInvoiceService().groupInvoice("G01");
        assertFalse(group.render().contains("PE01"));
        assertEquals(new BigDecimal("108.00"), group.getTotal());
    }
}
