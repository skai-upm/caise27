package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.controller.TicketController;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.business.model.SpeedPass;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
    }

    private IndividualTicket t001() {
        return (IndividualTicket) TicketController.getInstance().find("T001");
    }

    @Test
    void packOfTenCreatesIndependentPasses() {
        assertEquals("OK SPEEDPASS CREATED -> 80.00", f.run("SPEEDPASS CREATE;T001;10"));
        List<SpeedPass> passes = t001().getSpeedPasses();
        assertEquals(10, passes.size());
        assertEquals("T001-S1", passes.get(0).getId());
        assertEquals("T001-S10", passes.get(9).getId());
        for (SpeedPass p : passes) {
            assertEquals("8.00", p.getPrice().toPlainString());
            assertFalse(p.isConsumed());
        }
        assertEquals("OK SPEEDPASS CREATED -> 8.00", f.run("SPEEDPASS CREATE;T001;1"));
        assertEquals("T001-S11", t001().getSpeedPasses().get(10).getId());
    }

    @Test
    void speedPassPriceFollowsStoredTicketPrice() {
        f.ok("SYSTEM PRICE;33.33", "TICKET CREATE;T002;ana@correo.com;2026-10-15", "SYSTEM PRICE;100.00");
        assertEquals("OK SPEEDPASS CREATED -> 6.67", f.run("SPEEDPASS CREATE;T002;1"));
    }

    @Test
    void rejectsInvalidSpeedPassPurchases() {
        assertTrue(f.run("SPEEDPASS CREATE;T001;5").startsWith("ERROR:"));
        assertTrue(f.run("SPEEDPASS CREATE;T001;0").startsWith("ERROR:"));
        assertTrue(f.run("SPEEDPASS CREATE;NOPE;1").startsWith("ERROR:"));
        f.group("G01", 2, 20000000);
        f.ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(f.run("SPEEDPASS CREATE;TG01;1").startsWith("ERROR:"), "group ticket is not individual");
        f.ok("SPEEDPASS CREATE;TG01-1;1");
        f.ok("SYSTEM DATE;2026-10-16");
        assertTrue(f.run("SPEEDPASS CREATE;T001;1").startsWith("ERROR:"), "past ticket");
        assertEquals(0, t001().getSpeedPasses().size());
    }

    @Test
    void specialPassOncePerTicketLifetime() {
        f.ok("SYSTEM PRICE;60.00", "TICKET CREATE;T002;ana@correo.com;2026-10-20");
        assertEquals("OK SPECIALPASS CREATED -> 60.00", f.run("SPECIALPASS CREATE;T002"));
        assertEquals("OK SPECIALPASS CREATED -> 40.00", f.run("SPECIALPASS CREATE;T001"));
        assertTrue(f.run("SPECIALPASS CREATE;T001").startsWith("ERROR:"));
        f.ok("SPECIALPASS DELETE;T001-SPECIAL");
        assertNull(t001().getSpecialPass());
        assertTrue(f.run("SPECIALPASS CREATE;T001").startsWith("ERROR:"), "refunded pass still counts");
        assertTrue(f.run("SPECIALPASS CREATE;NOPE").startsWith("ERROR:"));
    }

    @Test
    void consumedSpecialPassBlocksAnother() {
        f.ok("SPECIALPASS CREATE;T001");
        t001().getSpecialPass().consume();
        assertTrue(f.run("SPECIALPASS CREATE;T001").startsWith("ERROR:"));
        assertTrue(t001().getSpecialPass().isConsumed(), "consumed pass is kept");
    }

    @Test
    void refundsOnlyUnusedPassesOfRefundableTickets() {
        f.ok("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001");
        t001().getSpeedPasses().get(1).consume();
        assertEquals("OK: SpeedPass deleted: T001-S1", f.run("SPEEDPASS DELETE;T001-S1"));
        assertTrue(f.run("SPEEDPASS DELETE;T001-S2").startsWith("ERROR:"), "consumed");
        assertTrue(f.run("SPEEDPASS DELETE;T001-SPECIAL").startsWith("ERROR:"), "wrong kind");
        assertTrue(f.run("SPECIALPASS DELETE;T001-S3").startsWith("ERROR:"), "wrong kind");
        assertTrue(f.run("SPEEDPASS DELETE;NOPE").startsWith("ERROR:"));
        f.ok("SYSTEM DATE;2026-10-15");
        assertTrue(f.run("SPEEDPASS DELETE;T001-S3").startsWith("ERROR:"), "ticket dated today");
        assertTrue(f.run("SPECIALPASS DELETE;T001-SPECIAL").startsWith("ERROR:"), "ticket dated today");
        assertEquals(9, t001().getSpeedPasses().size());
    }

    @Test
    void ticketDeletionDropsItsPasses() {
        f.ok("TICKET CREATE;T002;ana@correo.com;2026-10-20", "SPEEDPASS CREATE;T002;10", "SPECIALPASS CREATE;T002",
                "TICKET DELETE;T002");
        assertTrue(f.run("SPEEDPASS DELETE;T002-S1").startsWith("ERROR:"));
        assertTrue(f.run("SPECIALPASS DELETE;T002-SPECIAL").startsWith("ERROR:"));
    }

    @Test
    void userInvoiceIncludesPassesOfConstituentTickets() {
        f.group("G01", 2, 30000000);
        f.ok("GROUP ADD;G01;ana@correo.com", "GROUPTICKET CREATE;TG01;G01;2026-10-15",
                "SPEEDPASS CREATE;T001;1", "SPECIALPASS CREATE;TG01-3");
        String invoice = f.run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("Ticket T001 (2026-10-15)  40.00"));
        assertFalse(invoice.contains("Ticket TG01-3"));
        assertTrue(invoice.contains("SpeedPass T001-S1  8.00"));
        assertTrue(invoice.contains("Special Pass TG01-3-SPECIAL  40.00"));
        assertTrue(invoice.endsWith("TOTAL: 88.00"));
        String group = f.run("INVOICE CREATE;GROUP;G01");
        assertFalse(group.contains("Pass"));
        assertTrue(group.endsWith("TOTAL: 108.00"));
    }
}
