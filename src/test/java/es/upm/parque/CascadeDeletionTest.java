package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CascadeDeletionTest extends ParkTestSupport {

    @BeforeEach
    void purchases() {
        createAna();
        createPedro();
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;" + ANA);
        ok("GROUP ADD;G01;" + PEDRO);
        ok("SYSTEM DATE;2026-10-01");
        ok("TICKET CREATE;T001;" + ANA + ";2026-10-15");
        ok("SPEEDPASS CREATE;T001;10");
        ok("SPECIALPASS CREATE;T001");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;1");
        ok("SPECIALPASS CREATE;TG01-2");
    }

    @Test
    public void ticketDeletionRemovesItsPasses() {
        ok("TICKET DELETE;T001");
        assertNull(tickets().find("T001"));
        assertTrue(passes().getPasses().stream().noneMatch(pass -> pass.getTicket().getId().equals("T001")));
        assertEquals(2, passes().getPasses().size());
        restart();
        assertEquals(2, passes().getPasses().size());
    }

    @Test
    public void groupTicketDeletionRemovesPassesTicketsAndGroupTicket() {
        ok("GROUPTICKET DELETE;TG01");
        assertNull(tickets().find("TG01"));
        assertNull(tickets().find("TG01-1"));
        assertNull(tickets().find("TG01-2"));
        assertNull(passes().find("TG01-1-SP1"));
        assertNull(passes().find("TG01-2-SPECIAL"));
        assertEquals(11, passes().getPasses().size());
        restart();
        assertEquals(11, passes().getPasses().size());
    }

    @Test
    public void failedDeletionLeavesEverythingInPlace() {
        ok("SYSTEM DATE;2026-10-15");
        fails("TICKET DELETE;T001");
        fails("GROUPTICKET DELETE;TG01");
        fails("TICKET DELETE;TG01-1");
        assertNotNull(tickets().find("T001"));
        assertNotNull(tickets().find("TG01-1"));
        assertEquals(13, passes().getPasses().size());
    }

    @Test
    public void flyModeAlsoPersistsPassesInTheTemporaryDirectory() {
        boolean dataExisted = Files.exists(App.DATA_DIRECTORY);
        boolean tmpExisted = Files.exists(App.TEMPORARY_BASE);
        String commands = String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "SYSTEM DATE;2026-10-15",
                "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "SPEEDPASS CREATE;T001;1",
                "ATTRACTION CREATE;A01;Tornado;5;GENERAL",
                "ATTRACTION ENTER;A01;T001;SPEED",
                "INVOICE CREATE;USER;ana@correo.com",
                "EXIT") + "\n";
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
        try {
            App.main(new String[] {"--fly"});
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        String output = captured.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("OK ATTRACTION ENTER -> 1 (SpeedPass T001-SP1 consumed)"), output);
        assertTrue(output.contains("TOTAL: 48.00"), output);
        assertFalse(output.contains("ERROR"), output);
        assertEquals(dataExisted, Files.exists(App.DATA_DIRECTORY));
        assertEquals(tmpExisted, Files.exists(App.TEMPORARY_BASE));
    }
}
