package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CascadeDeletionTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, TODAY);
        t.run(ParkTestSupport.CREATE_ANA, "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001", "GROUP CREATE;G01;Familia");
        t.addMembers("G01", 2);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SPEEDPASS CREATE;TG01-1;1", "SPECIALPASS CREATE;TG01-2");
    }

    private String passes() throws IOException {
        return Files.readString(dir.resolve("passes.txt"));
    }

    @Test
    void ticketDeleteRemovesItsPasses() throws IOException {
        assertEquals("OK TICKET DELETED", t.run("TICKET DELETE;T001"));
        assertFalse(passes().contains("T001"));
        assertTrue(passes().contains("TG01-1-SP1"));
        assertEquals(2, t.restart(TODAY).tickets.getIndividualTickets().stream()
                .mapToLong(i -> i.getPasses().size()).sum());
    }

    @Test
    void groupTicketDeleteRemovesPassesTicketsAndGroupTicket() throws IOException {
        assertEquals("OK GROUP TICKET DELETED", t.run("GROUPTICKET DELETE;TG01"));
        assertFalse(passes().contains("TG01"));
        assertFalse(Files.readString(dir.resolve("tickets.txt")).contains("TG01"));
        assertEquals("", Files.readString(dir.resolve("grouptickets.txt")));
        ParkTestSupport restarted = t.restart(TODAY);
        assertEquals(1, restarted.tickets.getTickets().size());
        assertEquals(11, restarted.tickets.getIndividual("T001").getPasses().size());
    }

    @Test
    void failedDeletionsLeaveEverythingInPlace() throws IOException {
        String before = passes();
        t.run("SYSTEM DATE;2026-10-15");
        assertTrue(t.run("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(t.run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(t.run("TICKET DELETE;TG01-1").startsWith("ERROR"));
        assertEquals(before, passes());
        assertEquals(11, t.tickets.getIndividual("T001").getPasses().size());
        assertEquals(1, t.tickets.getIndividual("TG01-1").getPasses().size());
        assertEquals(4, t.tickets.getTickets().size());
    }

    @Test
    void flyModeKeepsPassesInTheTemporaryDirectory() throws IOException {
        Path commands = dir.resolve("fly.txt");
        Files.writeString(commands, "SYSTEM DATE;2026-10-01\nSPEEDPASS CREATE;T001;1\nEXIT\n");
        String before = passes();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        App.run(new String[] {"--fly", commands.toString()}, dir, dir.resolve("tmp"),
                new java.io.ByteArrayInputStream(new byte[0]), new java.io.PrintStream(out, true));
        assertTrue(out.toString().startsWith("OK SYSTEM DATE"));
        assertTrue(out.toString().contains("ERROR Ticket not found: T001"));
        assertEquals(before, passes());
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, entries.count());
        }
    }
}
