package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpeedPass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PassTest {

    @TempDir
    Path dir;

    private TestContext ctx;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        ctx.user(1);
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
    }

    private IndividualTicket t001() {
        return (IndividualTicket) ctx.tickets().find("T001");
    }

    @Test
    void speedPassesAreSoldInOnesAndTensAtTwentyPercent() {
        assertTrue(ctx.run("SPEEDPASS CREATE;T001;10").startsWith("OK SPEEDPASS CREATED -> 80.00"));
        assertEquals(10, t001().getSpeedPasses().size());
        for (SpeedPass pass : t001().getSpeedPasses()) {
            assertEquals(new BigDecimal("8.00"), pass.getPrice());
            assertFalse(pass.isConsumed());
        }
        assertEquals(10, t001().getSpeedPasses().stream().map(SpeedPass::getId).distinct().count());
        assertTrue(ctx.run("SPEEDPASS CREATE;T001;1").startsWith("OK SPEEDPASS CREATED -> 8.00 (T001-SP11)"));
        error(ctx.run("SPEEDPASS CREATE;T001;5"));
        error(ctx.run("SPEEDPASS CREATE;T404;1"));
        assertEquals(11, t001().getSpeedPasses().size());
    }

    @Test
    void passPriceFollowsTheTicketPrice() {
        ok(ctx.park().setTicketPrice("60.00"));
        ok(ctx.tickets().createTicket("T002", email(1), "2026-10-15"));
        assertTrue(ctx.run("SPEEDPASS CREATE;T002;1").startsWith("OK SPEEDPASS CREATED -> 12.00"));
        assertTrue(ctx.run("SPECIALPASS CREATE;T002").startsWith("OK SPECIALPASS CREATED -> 60.00"));
        assertTrue(ctx.run("SPECIALPASS CREATE;T001").startsWith("OK SPECIALPASS CREATED -> 40.00"));
    }

    @Test
    void passesNeedAnIndividualCurrentOrFutureTicket() {
        ctx.group("G01", 2);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        error(ctx.run("SPEEDPASS CREATE;TG01;1"));
        ok(ctx.run("SPEEDPASS CREATE;TG01-" + TestContext.dni(1) + ";1"));
        ok(ctx.park().setSystemDate("2026-10-16"));
        error(ctx.run("SPEEDPASS CREATE;T001;1"));
        error(ctx.run("SPECIALPASS CREATE;T001"));
    }

    @Test
    void onlyOneSpecialPassPerTicketEver() {
        ok(ctx.run("SPECIALPASS CREATE;T001"));
        error(ctx.run("SPECIALPASS CREATE;T001"));
        ok(ctx.run("SPECIALPASS DELETE;T001-SPECIAL"));
        assertNull(t001().getSpecialPass());
        error(ctx.run("SPECIALPASS CREATE;T001"));
    }

    @Test
    void refundsOnlyUnusedPassesOfFutureTickets() {
        ok(ctx.run("SPEEDPASS CREATE;T001;1"));
        ok(ctx.run("SPEEDPASS CREATE;T001;1"));
        assertEquals("OK SPEEDPASS DELETED T001-SP1 -> refund 8.00", ctx.run("SPEEDPASS DELETE;T001-SP1"));
        error(ctx.run("SPEEDPASS DELETE;T001-SP1"));
        error(ctx.run("SPECIALPASS DELETE;T001-SP2"));
        t001().findUnusedSpeedPass().consume();
        error(ctx.run("SPEEDPASS DELETE;T001-SP2"));
        ok(ctx.run("SPEEDPASS CREATE;T001;1"));
        ok(ctx.park().setSystemDate("2026-10-15"));
        error(ctx.run("SPEEDPASS DELETE;T001-SP3"));
        assertEquals(2, t001().getSpeedPasses().size());
    }

    @Test
    void passesSurviveRestartWithPriceStatusAndTicket() throws IOException {
        ok(ctx.run("SPEEDPASS CREATE;T001;10"));
        ok(ctx.run("SPECIALPASS CREATE;T001"));
        t001().findUnusedSpeedPass().consume();
        assertTrue(ctx.tickets().save());
        ok(ctx.run("SPECIALPASS DELETE;T001-SPECIAL"));

        IndividualTicket restored = (IndividualTicket) ctx.restart().tickets().find("T001");
        assertEquals(10, restored.getSpeedPasses().size());
        assertTrue(restored.getSpeedPasses().get(0).isConsumed());
        assertFalse(restored.getSpeedPasses().get(1).isConsumed());
        assertEquals(new BigDecimal("8.00"), restored.getSpeedPasses().get(1).getPrice());
        assertNull(restored.getSpecialPass());
        assertTrue(restored.isSpecialPassPurchased());
        assertTrue(Files.readString(dir.resolve("passes.txt")).contains("T001-SP1;SPEED;T001;8.00;true"));
    }

    @Test
    void ticketDeletionCascadesToPasses() throws IOException {
        ok(ctx.run("SPEEDPASS CREATE;T001;10"));
        ok(ctx.run("SPECIALPASS CREATE;T001"));
        ok(ctx.run("TICKET DELETE;T001"));
        assertTrue(Files.readString(dir.resolve("passes.txt")).isBlank());
        assertNull(ctx.passes().ownerOf("T001-SP1"));
        assertNull(ctx.restart().tickets().find("T001"));
    }

    @Test
    void groupTicketDeletionRemovesPassesTicketsAndGroupTicket() throws IOException {
        ctx.group("G01", 3);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        String internal = "TG01-" + TestContext.dni(2);
        ok(ctx.run("SPEEDPASS CREATE;" + internal + ";10"));
        ok(ctx.park().setSystemDate("2026-10-15"));
        error(ctx.run("GROUPTICKET DELETE;TG01"));
        assertNotNull(ctx.passes().ownerOf(internal + "-SP1"));
        ok(ctx.park().setSystemDate("2026-10-01"));
        ok(ctx.run("GROUPTICKET DELETE;TG01"));
        TestContext restarted = ctx.restart();
        assertNull(restarted.tickets().find("TG01"));
        assertNull(restarted.tickets().find(internal));
        assertNull(restarted.passes().ownerOf(internal + "-SP1"));
        assertTrue(Files.readString(dir.resolve("passes.txt")).isBlank());
        assertFalse(Files.readString(dir.resolve("tickets.txt")).contains("TG01"));
    }
}
