package com.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.TestPark;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassCommandsTest {
    @TempDir
    Path dir;
    CommandProcessor p;

    private void ok(String c) {
        String r = p.process(c);
        assertTrue(r.startsWith("OK"), c + " -> " + r);
    }

    @BeforeEach
    void setUp() throws Exception {
        p = new CommandProcessor(TestPark.start(dir));
        ok("SYSTEM DATE;2026-10-01");
        ok("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        ok("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        ok("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        ok("GROUP CREATE;G01;Familia Garcia");
        ok("GROUP ADD;G01;ana@correo.com");
        ok("GROUP ADD;G01;pedro@correo.com");
        ok("GROUP ADD;G01;luis@correo.com");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    @AfterEach
    void tearDown() {
        com.parque.ApplicationContext.stop();
    }

    @Test
    void purchasesShowAmountCharged() {
        assertEquals("OK: SPEEDPASS CREATED -> 8.00", p.process("SPEEDPASS CREATE;P01;T001;1"));
        assertEquals("OK: SPEEDPASS CREATED -> 80.00", p.process("SPEEDPASS CREATE;P02;T001;10"));
        assertEquals("OK: SPECIALPASS CREATED -> 40.00", p.process("SPECIALPASS CREATE;S01;T001"));
        assertEquals("OK: SPEEDPASS CREATED -> 7.20", p.process("SPEEDPASS CREATE;P05;TG01-1;1"));
        assertEquals("OK: SPECIALPASS CREATED -> 36.00", p.process("SPECIALPASS CREATE;S05;TG01-1"));
        assertTrue(p.process("SPECIALPASS CREATE;S02;T001").startsWith("ERROR: "));
        assertTrue(p.process("SPEEDPASS CREATE;P03;T001;2").startsWith("ERROR: "));
        assertTrue(p.process("SPEEDPASS CREATE;P03;T001").startsWith("ERROR: "));
    }

    @Test
    void refundsByDate() {
        ok("SPEEDPASS CREATE;P01;T001;1");
        ok("SPECIALPASS CREATE;S01;T001");
        assertTrue(p.process("SPECIALPASS DELETE;P01-1").startsWith("ERROR: "));
        assertEquals("OK: SpeedPass refunded", p.process("SPEEDPASS DELETE;P01-1"));
        ok("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("SPECIALPASS DELETE;S01").startsWith("ERROR: "), "today is not future");
        ok("SYSTEM DATE;2026-10-01");
        assertEquals("OK: Special Pass refunded", p.process("SPECIALPASS DELETE;S01"));
    }

    @Test
    void ticketDeletionCascadesToPasses() throws Exception {
        ok("SPEEDPASS CREATE;P01;T001;1");
        ok("SPEEDPASS CREATE;P05;TG01-1;1");
        ok("TICKET DELETE;T001");
        ok("GROUPTICKET DELETE;TG01");
        assertEquals(List.of(), Files.readAllLines(dir.resolve("passes.txt")));
    }

    @Test
    void userInvoiceListsPersonalPassesButGroupInvoiceDoesNot() {
        ok("SPEEDPASS CREATE;P01;TG01-1;1");
        assertEquals(List.of("OK: Invoice for user Ana Garcia Lopez <ana@correo.com>", "  Individual tickets:",
                "    T001 (2026-10-15): 40.00", "  Personal passes:", "    SpeedPass P01-1 (ticket TG01-1): 7.20",
                "  Total: 47.20"), p.process("INVOICE CREATE;USER;ana@correo.com").lines().toList());
        assertTrue(p.process("INVOICE CREATE;GROUP;G01").endsWith("Total: 108.00"));
    }

    @Test
    void helpListsPassCommands() {
        String help = p.process("HELP");
        for (String l : List.of("SPEEDPASS CREATE;Identificador;Ticket;Cantidad", "SPEEDPASS DELETE;Identificador",
                "SPECIALPASS CREATE;Identificador;Ticket", "SPECIALPASS DELETE;Identificador")) {
            assertTrue(help.contains(l), l);
        }
    }
}
