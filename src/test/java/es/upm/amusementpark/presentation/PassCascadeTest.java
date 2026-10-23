package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static es.upm.amusementpark.presentation.GroupTicketCommandsTest.CREATE_LAURA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Ticket deletions take their passes with them, and user invoices list
 * personal passes.
 */
class PassCascadeTest {

    private static final String NL = System.lineSeparator();

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run(CREATE_LAURA);
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void ticketDeletionRemovesAllItsPasses() throws IOException {
        run("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        run("TICKET CREATE;T003;ana@correo.com;2027-04-20");
        run("SPEEDPASS CREATE;SP01;T002;1");
        run("SPEEDPASS CREATE;SP02;T002;10");
        run("SPECIALPASS CREATE;PE01;T002");
        run("SPEEDPASS CREATE;SP03;T003;1");
        park.findPass("SP02:5").consume();
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertNull(park.findTicket("T002"));
        assertEquals(1, park.passes().getPasses().size());
        assertNotNull(park.findPass("SP03:1"));
        assertEquals(List.of("SPEED;SP03:1;T003;8.00;false"), Files.readAllLines(dir.resolve("passes.txt")));

        Park reopened = TestParks.open(dir);
        assertNull(reopened.findPass("SP01:1"));
        assertNull(reopened.findPass("PE01"));
        assertNotNull(reopened.findPass("SP03:1"));
    }

    @Test
    void groupTicketDeletionRemovesMemberPasses() {
        run("GROUPTICKET CREATE;TG02;G01;2027-04-20");
        run("SPEEDPASS CREATE;SP01;TG02:12345678Z;10");
        run("SPECIALPASS CREATE;PE01;TG02:87654321X");
        assertEquals("GROUPTICKET DELETED", run("GROUPTICKET DELETE;TG02"));
        assertNull(park.findTicket("TG02:12345678Z"));
        assertTrue(park.passes().getPasses().isEmpty());
    }

    @Test
    void failedDeletionKeepsTicketAndPasses() {
        run("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        run("SPEEDPASS CREATE;SP01;T002;10");
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertTrue(run("TICKET DELETE;T002").startsWith("ERROR"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
        assertNotNull(park.findTicket("T002"));
        assertEquals(10, park.passes().getPasses().size());
    }

    @Test
    void ticketsDatedTodayKeepTheirPasses() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("SPEEDPASS CREATE;SP01;T001;1");
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR"));
        assertNotNull(park.findPass("SP01:1"));
    }

    @Test
    void userInvoiceFromTheSourceExample() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("SPEEDPASS CREATE;SP01;T001;10");
        run("SPECIALPASS CREATE;PE01;T001");
        run("SPEEDPASS CREATE;SP02;TG01:12345678Z;1");
        run("SPEEDPASS CREATE;SP03;TG01:87654321X;1");
        park.findPass("SP01:1").consume();

        String invoice = run("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(invoice.contains("Individual tickets:" + NL + "  Ticket T001 for 2027-04-15 ... 40.00 EUR"));
        assertTrue(invoice.contains("  SpeedPass SP01:1 for ticket T001 (consumed) ... 8.00 EUR"));
        assertTrue(invoice.contains("  SpeedPass SP02:1 for ticket TG01:12345678Z ... 7.20 EUR"));
        assertTrue(invoice.contains("Special Passes:" + NL + "  Special Pass PE01 for ticket T001 ... 40.00 EUR"));
        assertFalse(invoice.contains("SP03"), "Pedro's pass is not on Ana's invoice");
        assertFalse(invoice.contains("Ticket TG01"), "group components are not individual purchases");
        assertTrue(invoice.endsWith("TOTAL: 167.20 EUR"));

        String group = run("INVOICE CREATE;GROUP;G01");
        assertTrue(group.endsWith("TOTAL: 108.00 EUR"));
        assertFalse(group.contains("SpeedPass"));
    }

    @Test
    void userWithOnlyGroupPassesHasNoTicketSection() {
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        run("SPEEDPASS CREATE;SP03;TG01:87654321X;1");
        String invoice = run("INVOICE CREATE;USER;pedro@correo.com");
        assertFalse(invoice.contains("Individual tickets"));
        assertFalse(invoice.contains("Special Passes"));
        assertTrue(invoice.contains("SpeedPasses:"));
        assertTrue(invoice.endsWith("TOTAL: 7.20 EUR"));
    }
}
