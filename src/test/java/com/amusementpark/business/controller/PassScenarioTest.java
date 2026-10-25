package com.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.TestSupport;
import com.amusementpark.business.model.IndividualTicket;
import com.amusementpark.presentation.Bootstrap;
import com.amusementpark.presentation.CommandProcessor;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PassScenarioTest {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() throws Exception {
        start();
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        run("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    private void start() throws Exception {
        buffer = new ByteArrayOutputStream();
        processor = Bootstrap.initialize(dir, START, TestSupport.FAST_HASHER,
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String run(String command) {
        buffer.reset();
        processor.process(command);
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private void assertError(String command) {
        String out = run(command);
        assertTrue(out.startsWith("ERROR"), command + " -> " + out);
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().findTicket(id);
    }

    @Test
    public void buySpeedPasses() {
        assertEquals("OK SPEEDPASS CREATED -> 8.00 (T001-SP1)", run("SPEEDPASS CREATE;T001;1"));
        assertEquals("OK SPEEDPASS CREATED -> 80.00 (T001-SP2 .. T001-SP11)", run("SPEEDPASS CREATE;T001;10"));
        assertEquals(11, ticket("T001").getSpeedPasses().size());
        assertError("SPEEDPASS CREATE;T001;5");
        assertError("SPEEDPASS CREATE;T001;0");
        assertError("SPEEDPASS CREATE;TG01;1");
        assertError("SPEEDPASS CREATE;T999;1");
        assertTrue(run("SPEEDPASS CREATE;TG01-1;1").startsWith("OK SPEEDPASS CREATED -> 8.00"));
    }

    @Test
    public void pastTicketsCannotGetPasses() {
        run("SYSTEM DATE;2026-10-16");
        assertError("SPEEDPASS CREATE;T001;1");
        assertError("SPECIALPASS CREATE;T001");
    }

    @Test
    public void passPriceFollowsTicketPrice() {
        run("SYSTEM PRICE;60.00");
        run("TICKET CREATE;T002;ana@correo.com;2026-10-15");
        assertEquals("OK SPEEDPASS CREATED -> 12.00 (T002-SP1)", run("SPEEDPASS CREATE;T002;1"));
        assertEquals("OK SPECIALPASS CREATED -> 60.00 (T002-SPECIAL)", run("SPECIALPASS CREATE;T002"));
    }

    @Test
    public void singleSpecialPass() {
        assertEquals("OK SPECIALPASS CREATED -> 40.00 (T001-SPECIAL)", run("SPECIALPASS CREATE;T001"));
        assertError("SPECIALPASS CREATE;T001");
        ticket("T001").getSpecialPass().consume();
        assertError("SPECIALPASS CREATE;T001");
    }

    @Test
    public void refunds() {
        run("SPEEDPASS CREATE;T001;10");
        run("SPECIALPASS CREATE;T001");
        assertEquals("OK: SpeedPass T001-SP1 refunded -> 8.00", run("SPEEDPASS DELETE;T001-SP1"));
        assertError("SPEEDPASS DELETE;T001-SP1");
        assertError("SPEEDPASS DELETE;T001-SPECIAL");
        assertError("SPECIALPASS DELETE;T001-SP2");
        ticket("T001").findPass("T001-SP2").consume();
        assertError("SPEEDPASS DELETE;T001-SP2");
        assertTrue(ticket("T001").findPass("T001-SP2").isConsumed());
        run("SYSTEM DATE;2026-10-15");
        assertError("SPEEDPASS DELETE;T001-SP3");
        assertError("SPECIALPASS DELETE;T001-SPECIAL");
        run("SYSTEM DATE;2026-10-14");
        assertEquals("OK: Special Pass T001-SPECIAL refunded -> 40.00", run("SPECIALPASS DELETE;T001-SPECIAL"));
        assertNull(ticket("T001").getSpecialPass());
        assertEquals(9, ticket("T001").getSpeedPasses().size());
    }

    @Test
    public void passesSurviveRestartWithPriceAndStatus() throws Exception {
        run("SPEEDPASS CREATE;T001;1");
        run("SPECIALPASS CREATE;TG01-2");
        ticket("T001").findPass("T001-SP1").consume();
        TicketController.getInstance().savePasses();
        start();
        IndividualTicket t001 = ticket("T001");
        assertEquals(1, t001.getSpeedPasses().size());
        assertTrue(t001.getSpeedPasses().get(0).isConsumed());
        assertEquals(new BigDecimal("8.00"), t001.getSpeedPasses().get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), ticket("TG01-2").getSpecialPass().getPrice());
        assertTrue(!ticket("TG01-2").getSpecialPass().isConsumed());
    }

    @Test
    public void ticketDeletionCascadesToPasses() throws Exception {
        run("SPEEDPASS CREATE;T001;10");
        run("SPECIALPASS CREATE;T001");
        assertEquals("OK: ticket deleted with 11 pass(es)", run("TICKET DELETE;T001"));
        assertTrue(Files.readAllLines(dir.resolve("passes.txt")).isEmpty());
        start();
        assertNull(TicketController.getInstance().findTicket("T001"));
        assertError("SPEEDPASS DELETE;T001-SP1");
    }

    @Test
    public void groupTicketDeletionCascadesToMemberPasses() throws Exception {
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPECIALPASS CREATE;TG01-2");
        run("SPEEDPASS CREATE;T001;1");
        assertEquals("OK: group ticket deleted with 2 individual tickets and 2 pass(es)",
                run("GROUPTICKET DELETE;TG01"));
        assertEquals(1, Files.readAllLines(dir.resolve("passes.txt")).size());
        start();
        assertNull(TicketController.getInstance().findTicket("TG01-1"));
        assertEquals(1, ticket("T001").getSpeedPasses().size());
    }

    @Test
    public void rejectedDeletionKeepsEverything() throws Exception {
        run("SPEEDPASS CREATE;TG01-1;1");
        run("SPEEDPASS CREATE;T001;1");
        run("SYSTEM DATE;2026-10-15");
        assertError("GROUPTICKET DELETE;TG01");
        assertError("TICKET DELETE;T001");
        assertEquals(2, Files.readAllLines(dir.resolve("passes.txt")).size());
        assertEquals(1, ticket("TG01-1").getSpeedPasses().size());
        assertEquals(1, ticket("T001").getSpeedPasses().size());
    }
}
