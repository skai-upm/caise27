package com.parque.presentation;

import com.parque.TestPark;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.business.controller.ParkController;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InvoiceCommandsTest {
    @TempDir
    Path dir;
    CommandProcessor p;

    private void ok(String command) {
        String r = p.process(command);
        assertTrue(r.startsWith("OK"), command + " -> " + r);
    }

    private List<String> lines(String command) {
        return p.process(command).lines().toList();
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

    @Test
    void userInvoice() {
        assertEquals(List.of("OK: Invoice for user Ana Garcia Lopez <ana@correo.com>", "  Individual tickets:",
                "    T001 (2026-10-15): 40.00", "  Total: 40.00"), lines("INVOICE CREATE;USER;ana@correo.com"));
    }

    @Test
    void groupInvoice() {
        assertEquals(List.of("OK: Invoice for group G01 - Familia Garcia", "  Group tickets:",
                "    TG01 (2026-10-15, 3 members): 108.00", "  Total: 108.00"), lines("INVOICE CREATE;GROUP;G01"));
    }

    @Test
    void recipientWithoutPurchases() {
        assertEquals(List.of("OK: Invoice for user Pedro Lopez Martin <pedro@correo.com>", "  Total: 0.00"),
                lines("INVOICE CREATE;USER;pedro@correo.com"));
        ok("GROUP CREATE;G02;Vacio");
        assertEquals(List.of("OK: Invoice for group G02 - Vacio", "  Total: 0.00"), lines("INVOICE CREATE;GROUP;G02"));
    }

    @Test
    void errors() {
        for (String c : List.of("INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99",
                "INVOICE CREATE;CLIENT;ana@correo.com", "INVOICE CREATE;USER", "INVOICE CREATE;USER;a;b",
                "INVOICE CREATE")) {
            assertTrue(p.process(c).startsWith("ERROR: "), c);
        }
    }

    @Test
    void helpListsInvoiceCommands() {
        String help = p.process("HELP");
        assertTrue(help.contains("INVOICE CREATE;USER;Correo"));
        assertTrue(help.contains("INVOICE CREATE;GROUP;Identificador"));
    }
}
