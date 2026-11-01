package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Invoice;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.presentation.AppBootstrap;

public class PassTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    public void setUp() throws IOException, OperationException {
        park = start(LocalDate.of(2026, 10, 1));
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createTicket("T001", "ana@correo.com", "2026-10-15");
    }

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    private ParkController start(LocalDate date) throws IOException {
        return AppBootstrap.start(new StoragePaths(dir), date, new BCryptPasswordHasher(4));
    }

    private ParkController restart() throws IOException {
        LocalDate date = park.getSystemDate();
        AppBootstrap.shutdown();
        return start(date);
    }

    private IndividualTicket t001(ParkController p) {
        return (IndividualTicket) p.getTickets().find("T001");
    }

    @Test
    public void buyOneAndTenSpeedPasses() throws Exception {
        List<SpeedPass> one = park.buySpeedPasses("T001", "1");
        assertEquals(1, one.size());
        assertEquals("T001-SP1", one.get(0).getId());
        assertEquals(new BigDecimal("8.00"), one.get(0).getPrice());

        List<SpeedPass> ten = park.buySpeedPasses("T001", "10");
        assertEquals(10, ten.size());
        assertEquals(10, new HashSet<>(ten.stream().map(SpeedPass::getId).toList()).size());
        assertEquals("T001-SP2", ten.get(0).getId());
        assertEquals("T001-SP11", ten.get(9).getId());
        assertTrue(ten.stream().noneMatch(SpeedPass::isConsumed));
        assertEquals(11, t001(park).getSpeedPasses().size());
    }

    @Test
    public void speedPassPriceFollowsTicketPrice() throws Exception {
        park.setBasePrice("45.55");
        park.createTicket("T002", "ana@correo.com", "2026-10-15");
        assertEquals(new BigDecimal("9.11"), park.buySpeedPasses("T002", "1").get(0).getPrice());
    }

    @Test
    public void invalidSpeedPassPurchases() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        park.createTicket("OLD", "ana@correo.com", "2026-10-01");
        park.setSystemDate("2026-10-02");
        assertThrows(OperationException.class, () -> park.buySpeedPasses("T001", "5"));
        assertThrows(OperationException.class, () -> park.buySpeedPasses("T001", "x"));
        assertThrows(OperationException.class, () -> park.buySpeedPasses("NOPE", "1"));
        assertThrows(OperationException.class, () -> park.buySpeedPasses("TG01", "1"));
        assertThrows(OperationException.class, () -> park.buySpeedPasses("OLD", "1"));
        assertTrue(t001(park).getSpeedPasses().isEmpty());
    }

    @Test
    public void passesCanBeBoughtForGroupOwnedTickets() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        SpeedPass pass = park.buySpeedPasses("TG01-1", "1").get(0);
        assertEquals("TG01-1-SP1", pass.getId());
        assertEquals(new BigDecimal("8.00"), pass.getPrice());
    }

    @Test
    public void onlyOneSpecialPassPerTicketLifetime() throws Exception {
        SpecialPass special = park.buySpecialPass("T001");
        assertEquals("T001-SPECIAL", special.getId());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertThrows(OperationException.class, () -> park.buySpecialPass("T001"));

        special.consume();
        assertThrows(OperationException.class, () -> park.buySpecialPass("T001"));
    }

    @Test
    public void refundedSpecialPassCannotBeRebought() throws Exception {
        park.buySpecialPass("T001");
        park.refundSpecialPass("T001-SPECIAL");
        assertNull(t001(park).getSpecialPass());
        assertThrows(OperationException.class, () -> park.buySpecialPass("T001"));
        ParkController second = restart();
        assertThrows(OperationException.class, () -> second.buySpecialPass("T001"));
    }

    @Test
    public void onlyUnusedPassesOfFutureTicketsAreRefundable() throws Exception {
        List<SpeedPass> passes = park.buySpeedPasses("T001", "10");
        passes.get(0).consume();
        assertThrows(OperationException.class, () -> park.refundSpeedPass("T001-SP1"));
        park.refundSpeedPass("T001-SP2");
        assertEquals(9, t001(park).getSpeedPasses().size());
        assertThrows(OperationException.class, () -> park.refundSpeedPass("T001-SP2"));

        park.buySpecialPass("T001");
        assertThrows(OperationException.class, () -> park.refundSpeedPass("T001-SPECIAL"));
        assertThrows(OperationException.class, () -> park.refundSpecialPass("T001-SP3"));

        park.setSystemDate("2026-10-15");
        assertThrows(OperationException.class, () -> park.refundSpeedPass("T001-SP3"));
        assertThrows(OperationException.class, () -> park.refundSpecialPass("T001-SPECIAL"));
        assertEquals(9, t001(park).getSpeedPasses().size());
    }

    @Test
    public void passesSurviveRestartWithPriceAndState() throws Exception {
        park.buySpeedPasses("T001", "1").get(0).consume();
        park.buySpecialPass("T001");
        park.getTickets().save();

        IndividualTicket t = t001(restart());
        assertEquals(1, t.getSpeedPasses().size());
        assertTrue(t.getSpeedPasses().get(0).isConsumed());
        assertEquals(new BigDecimal("8.00"), t.getSpeedPasses().get(0).getPrice());
        assertFalse(t.getSpecialPass().isConsumed());
        assertEquals(new BigDecimal("40.00"), t.getSpecialPass().getPrice());
        assertTrue(t.hasAcquiredSpecialPass());
    }

    @Test
    public void userInvoiceIncludesPassesOfGroupOwnedTickets() throws Exception {
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        park.buySpeedPasses("TG01-1", "1");
        park.buySpecialPass("T001");

        Invoice user = park.invoice("USER", "ana@correo.com");
        assertEquals(new BigDecimal("88.00"), user.getTotal());
        String text = user.render();
        assertTrue(text.contains("Personal passes:"));
        assertTrue(text.contains("TG01-1-SP1 SpeedPass for TG01-1: 8.00"));
        assertTrue(text.contains("T001-SPECIAL Special Pass for T001: 40.00"));
        assertFalse(text.contains("TG01-1 ("));

        Invoice group = park.invoice("GROUP", "G01");
        assertEquals(new BigDecimal("80.00"), group.getTotal());
        assertFalse(group.render().contains("SP1"));
    }
}
