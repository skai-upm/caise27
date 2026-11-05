package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketCommandsTest {

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(LocalDate.of(2027, 4, 15));
        park.setUserDirectory(email -> Set.of("ana@correo.com").contains(email));
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        processor = new CommandProcessor(out);
        SystemCommands.register(processor, park, out);
        TicketCommands.register(processor, park, out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createShowsPrice() {
        processor.execute("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(output().contains("TICKET CREATED. PRICE: 40.00"));
        processor.execute("SYSTEM PRICE;45.50");
        processor.execute("TICKET CREATE;T002;ana@correo.com;2027-04-16");
        assertTrue(output().contains("TICKET CREATED. PRICE: 45.50"));
    }

    @Test
    void rejectedCreationsReportErrors() {
        processor.execute("TICKET CREATE;T002;ana@correo.com;2027-04-14");
        processor.execute("TICKET CREATE;T003;nobody@correo.com;2027-04-20");
        processor.execute("TICKET CREATE;T004;ana@correo.com;20270420");
        processor.execute("TICKET CREATE;T005;ana@correo.com");
        assertNull(park.findTicket("T002"));
        assertNull(park.findTicket("T003"));
        assertNull(park.findTicket("T004"));
        assertTrue(output().contains("ERROR: Tickets cannot be created for past dates"));
        assertTrue(output().contains("ERROR: User nobody@correo.com does not exist"));
        assertTrue(output().contains("ERROR: Invalid date"));
        assertTrue(output().contains("ERROR: Invalid syntax"));
    }

    @Test
    void deleteFutureAndRejectToday() {
        processor.execute("TICKET CREATE;T001;ana@correo.com;2027-04-20");
        processor.execute("TICKET CREATE;T002;ana@correo.com;2027-04-15");
        processor.execute("TICKET DELETE;T001");
        processor.execute("TICKET DELETE;T002");
        assertTrue(output().contains("TICKET DELETED"));
        assertTrue(output().contains("must be kept"));
        assertNull(park.findTicket("T001"));
        assertNotNull(park.findTicket("T002"));
    }

    @Test
    void helpListsTicketCommands() {
        processor.execute("HELP");
        assertTrue(output().contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(output().contains("TICKET DELETE;<identifier>"));
    }
}
