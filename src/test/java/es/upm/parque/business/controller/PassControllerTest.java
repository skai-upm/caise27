package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestPark;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassControllerTest {

    @TempDir
    Path dir;

    private TestPark park;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        park.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 1));
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.groups.create("G01", "F");
        park.groups.addMember("G01", "pedro@correo.com");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
    }

    @Test
    void buyOneAndTenSpeedPasses() {
        List<SpeedPass> one = park.passes.buySpeedPasses("T001", "1");
        assertEquals(1, one.size());
        assertEquals(new BigDecimal("8.00"), one.get(0).getPrice());
        List<SpeedPass> ten = park.passes.buySpeedPasses("T001", "10");
        assertEquals(10, ten.size());
        Set<String> ids = ten.stream().map(SpeedPass::getId).collect(Collectors.toSet());
        assertEquals(10, ids.size());
        assertFalse(ids.contains(one.get(0).getId()));
        assertTrue(ten.stream().noneMatch(SpeedPass::isConsumed));
        assertEquals(11, park.tickets.findIndividualTicket("T001").getSpeedPasses().size());
    }

    @Test
    void speedPassRejections() {
        assertThrows(ParkException.class, () -> park.passes.buySpeedPasses("T001", "5"));
        assertThrows(ParkException.class, () -> park.passes.buySpeedPasses("T001", "uno"));
        assertThrows(ParkException.class, () -> park.passes.buySpeedPasses("T999", "1"));
        ParkException group = assertThrows(ParkException.class, () -> park.passes.buySpeedPasses("TG01", "1"));
        assertTrue(group.getMessage().contains("individual tickets"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertThrows(ParkException.class, () -> park.passes.buySpeedPasses("T001", "1"));
        assertTrue(park.tickets.findIndividualTicket("T001").getPasses().isEmpty());
    }

    @Test
    void passesForInternalTicketsUseTheirStoredPrice() {
        SpeedPass pass = park.passes.buySpeedPasses("TG01-1", "1").get(0);
        assertEquals(new BigDecimal("8.00"), pass.getPrice());
        assertEquals("TG01-1", park.passes.ticketOf(pass.getId()).getId());
    }

    @Test
    void onlyOneSpecialPassEvenIfUsed() {
        SpecialPass special = park.passes.buySpecialPass("T001");
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertThrows(ParkException.class, () -> park.passes.buySpecialPass("T001"));
        special.consume();
        assertThrows(ParkException.class, () -> park.passes.buySpecialPass("T001"));
    }

    @Test
    void refundsFollowTheTicketDateRule() {
        String sp = park.passes.buySpeedPasses("T001", "1").get(0).getId();
        String xp = park.passes.buySpecialPass("T001").getId();
        assertThrows(ParkException.class, () -> park.passes.refundSpecialPass(sp));
        assertThrows(ParkException.class, () -> park.passes.refundSpeedPass(xp));
        assertThrows(ParkException.class, () -> park.passes.refundSpeedPass("SP999"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertThrows(ParkException.class, () -> park.passes.refundSpeedPass(sp));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 14));
        assertEquals(new BigDecimal("8.00"), park.passes.refundSpeedPass(sp).getPrice());
        assertEquals(new BigDecimal("40.00"), park.passes.refundSpecialPass(xp).getPrice());
        assertNull(park.passes.findPass(sp));
        assertNotNull(park.passes.buySpecialPass("T001"));
    }

    @Test
    void consumedPassesCannotBeRefunded() {
        SpeedPass pass = park.passes.buySpeedPasses("T001", "1").get(0);
        pass.consume();
        assertThrows(ParkException.class, () -> park.passes.refundSpeedPass(pass.getId()));
        assertNotNull(park.passes.findPass(pass.getId()));
    }

    @Test
    void passesSurviveRestartWithStatusAndIdsStayUnique() {
        SpeedPass sp = park.passes.buySpeedPasses("T001", "1").get(0);
        park.passes.buySpecialPass("T001");
        sp.consume();
        park.tickets.save();

        TestPark restarted = new TestPark(dir);
        IndividualTicket ticket = restarted.tickets.findIndividualTicket("T001");
        assertTrue(ticket.getSpeedPasses().get(0).isConsumed());
        assertEquals(new BigDecimal("40.00"), ticket.getSpecialPass().getPrice());
        String next = restarted.passes.buySpeedPasses("T001", "1").get(0).getId();
        assertEquals("SP2", next);
    }

    @Test
    void removingATicketRemovesItsPasses() {
        park.tickets.create("T002", "ana@correo.com", "2026-10-20");
        park.passes.buySpeedPasses("T002", "10");
        park.tickets.delete("T002");
        assertTrue(new TestPark(dir).tickets.getIndividualTickets().stream().noneMatch(t -> t.getId().equals("T002")));
        assertNull(park.passes.findPass("SP1"));
    }
}
