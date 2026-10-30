package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpeedPass;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().findById(id);
    }

    @Test
    void packOfTenCreatesTenIndependentPasses() {
        String out = s.run("SPEEDPASS CREATE;T001;10");
        assertTrue(out.startsWith("OK SPEEDPASS CREATED -> 80.00"), out);
        List<SpeedPass> passes = ticket("T001").getSpeedPasses();
        assertEquals(10, passes.size());
        assertEquals("T001-SP1", passes.get(0).getId());
        assertEquals("T001-SP10", passes.get(9).getId());
        assertEquals(10, passes.stream().map(SpeedPass::getId).distinct().count());
        for (SpeedPass p : passes) {
            assertEquals(new BigDecimal("8.00"), p.getPrice());
            assertFalse(p.isConsumed());
        }
        assertTrue(out.contains("T001-SP10"));
    }

    @Test
    void singleSpeedPassAndNumberingContinues() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00 [T001-SP1]", s.run("SPEEDPASS CREATE;T001;1"));
        assertEquals("OK SPEEDPASS CREATED -> 8.00 [T001-SP2]", s.run("SPEEDPASS CREATE;T001;1"));
    }

    @Test
    void speedPassPriceUsesStoredTicketPrice() {
        s.run("SYSTEM PRICE;60.00");
        assertEquals("OK SPEEDPASS CREATED -> 8.00 [T001-SP1]", s.run("SPEEDPASS CREATE;T001;1"));
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 10.80 [TG01-1-SP1]", s.run("SPEEDPASS CREATE;TG01-1;1"));
    }

    @Test
    void invalidSpeedPassPurchases() {
        s.createGroup("G01", 2, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "TICKET CREATE;T002;ana@correo.com;2026-10-01",
                "SYSTEM DATE;2026-10-02");
        assertTrue(s.run("SPEEDPASS CREATE;T001;5").startsWith("ERROR"));
        assertTrue(s.run("SPEEDPASS CREATE;T999;1").startsWith("ERROR"));
        assertTrue(s.run("SPEEDPASS CREATE;TG01;1").startsWith("ERROR"));
        assertTrue(s.run("SPEEDPASS CREATE;T002;1").startsWith("ERROR"));
        assertTrue(ticket("T001").getSpeedPasses().isEmpty());
    }

    @Test
    void oneSpecialPassPerTicketEvenAfterUse() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00 [T001-SPECIAL]", s.run("SPECIALPASS CREATE;T001"));
        assertTrue(s.run("SPECIALPASS CREATE;T001").startsWith("ERROR"));
        ticket("T001").getSpecialPass().consume();
        assertTrue(s.run("SPECIALPASS CREATE;T001").startsWith("ERROR"));
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
    }

    @Test
    void onlyUnusedPassesOfFutureTicketsAreRefunded() {
        s.run("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001");
        ticket("T001").getSpeedPasses().get(0).consume();
        assertTrue(s.run("SPEEDPASS DELETE;T001-SP1").startsWith("ERROR"));
        assertTrue(s.run("SPEEDPASS DELETE;T001-SP2").startsWith("OK"));
        assertTrue(s.run("SPEEDPASS DELETE;T001-SP2").startsWith("ERROR"));
        assertTrue(s.run("SPEEDPASS DELETE;T001-SPECIAL").startsWith("ERROR"));
        assertTrue(s.run("SPECIALPASS DELETE;T001-SP3").startsWith("ERROR"));
        assertEquals(9, ticket("T001").getSpeedPasses().size());
        assertTrue(ticket("T001").getSpeedPasses().get(0).isConsumed());
        s.run("SYSTEM DATE;2026-10-15");
        assertTrue(s.run("SPEEDPASS DELETE;T001-SP3").startsWith("ERROR"));
        assertTrue(s.run("SPECIALPASS DELETE;T001-SPECIAL").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-14");
        assertTrue(s.run("SPECIALPASS DELETE;T001-SPECIAL").startsWith("OK"));
        assertNull(ticket("T001").getSpecialPass());
    }

    @Test
    void userInvoiceIncludesPassesOfGroupConstituents() {
        s.run("SPEEDPASS CREATE;T001;1", "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;" + s.createUser(1), "GROUP ADD;G01;" + s.createUser(2),
                "GROUPTICKET CREATE;TG01;G01;2026-10-15", "SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;TG01-2;1");
        String invoice = s.run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("Ticket T001 (2026-10-15) -> 40.00"), invoice);
        assertTrue(invoice.contains("SpeedPass T001-SP1 (ticket T001) -> 8.00"));
        assertTrue(invoice.contains("SpeedPass TG01-1-SP1 (ticket TG01-1) -> 7.20"));
        assertFalse(invoice.contains("TG01-2"));
        assertFalse(invoice.contains("Ticket TG01-1 ("));
        assertTrue(invoice.endsWith("TOTAL: 55.20"));
        String group = s.run("INVOICE CREATE;GROUP;G01");
        assertFalse(group.contains("SpeedPass"));
        assertTrue(group.endsWith("TOTAL: 108.00"));
    }
}
