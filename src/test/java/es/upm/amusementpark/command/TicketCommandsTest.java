package es.upm.amusementpark.command;

import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.service.Park;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketCommandsTest {

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
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createsTicketAtCurrentPrice() {
        assertEquals("TICKET CREATED. Price: 40.00 EUR", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        Ticket ticket = park.findTicket("T001");
        assertNotNull(ticket);
        assertSame(park.findUserByEmail("ana@correo.com"), ((IndividualTicket) ticket).getUser());

        run("SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED. Price: 45.50 EUR", run("TICKET CREATE;T002;ana@correo.com;2027-04-20"));
        assertEquals(new BigDecimal("40.00"), park.findTicket("T001").getPrice());
    }

    @Test
    void rejectsInvalidTickets() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(run("TICKET CREATE;T001;pedro@correo.com;2027-04-16").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;T002;ana@correo.com;2027-04-14").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;T003;nobody@correo.com;2027-04-15").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;T004;ana@correo.com;2027-02-30").startsWith("ERROR"));
        assertTrue(run("TICKET CREATE;;ana@correo.com;2027-04-15").startsWith("ERROR"));
        assertEquals(1, park.getTickets().size());
        assertEquals("ana@correo.com", ((IndividualTicket) park.findTicket("T001")).getUser().getEmail());
    }

    @Test
    void deletesOnlyFutureTickets() {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR"));
        assertNotNull(park.findTicket("T001"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertNull(park.findTicket("T002"));
        assertTrue(run("TICKET DELETE;T002").startsWith("ERROR"));

        run("SYSTEM DATE;2027-04-16");
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR"));
        assertNotNull(park.findTicket("T001"));
    }

    @Test
    void ticketsSurviveRestart() throws IOException {
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("SYSTEM PRICE;45.50");
        run("TICKET CREATE;T002;pedro@correo.com;2027-04-20");
        assertEquals(List.of(
                "INDIVIDUAL;T001;2027-04-15;40.00;0;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "INDIVIDUAL;T002;2027-04-20;45.50;0;87654321X;pedro@correo.com;Pedro;Lopez Martin"),
                Files.readAllLines(dir.resolve("tickets.txt")));

        park = TestParks.open(dir);
        IndividualTicket t2 = (IndividualTicket) park.findTicket("T002");
        assertEquals(new BigDecimal("45.50"), t2.getPrice());
        assertEquals(LocalDate.of(2027, 4, 20), t2.getDate());
        assertSame(park.findUserByEmail("pedro@correo.com"), t2.getUser());
    }

    @Test
    void failedSaveUndoesTicketChanges() {
        run("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertTrue(run("TICKET CREATE;T003;ana@correo.com;2027-04-20").startsWith("ERROR"));
            assertTrue(run("TICKET DELETE;T002").startsWith("ERROR"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
        assertNull(park.findTicket("T003"));
        assertNotNull(park.findTicket("T002"));
    }

    @Test
    void helpListsTicketCommands() {
        String help = run("HELP");
        assertTrue(help.contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(help.contains("TICKET DELETE;<identifier>"));
    }
}
