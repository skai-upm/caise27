package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.controller.ApplicationInitializer;

class TicketCommandsTest {

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("SYSTEM DATE;2027-04-10");
    }

    @Test
    void createsTicketAtCurrentPrice() {
        assertEquals("TICKET CREATED - Price: 40.00 EUR", processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        IndividualTicket t = (IndividualTicket) park.getTickets().findById("T001");
        assertSame(park.getUsers().findByEmail("ana@correo.com"), t.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t.getDate());
    }

    @Test
    void priceIsFixedAtPurchase() {
        processor.process("SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED - Price: 45.50 EUR", processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        processor.process("SYSTEM PRICE;50");
        assertEquals(new BigDecimal("45.50"), park.getTickets().findById("T001").getPrice());
    }

    @Test
    void ticketForTodayAllowedPastRejected() {
        assertTrue(processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-10").startsWith("TICKET CREATED"));
        assertEquals("ERROR: Tickets cannot be created for past dates",
                processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-09"));
        assertNull(park.getTickets().findById("T002"));
    }

    @Test
    void rejectsDuplicateIdUnknownUserAndBadInput() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("ERROR: Ticket identifier already exists", processor.process("TICKET CREATE;T001;pedro@correo.com;2027-04-16"));
        assertEquals("ERROR: User not found", processor.process("TICKET CREATE;T002;nadie@correo.com;2027-04-16"));
        assertTrue(processor.process("TICKET CREATE;T002;ana@correo.com;16/04/2027").startsWith("ERROR"));
        assertTrue(processor.process("TICKET CREATE;T002;ana@correo.com").startsWith("ERROR"));
        assertNull(park.getTickets().findById("T002"));
    }

    @Test
    void deletesOnlyFutureTickets() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-10");
        assertEquals("TICKET DELETED", processor.process("TICKET DELETE;T001"));
        assertNull(park.getTickets().findById("T001"));
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-11");
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("ERROR"));
        assertNotNull(park.getTickets().findById("T002"));
        assertEquals("ERROR: Ticket not found", processor.process("TICKET DELETE;T999"));
    }

    @Test
    void activeTicketBlocksUserDeletion() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("ERROR: User has active tickets", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertNotNull(park.getUsers().findByEmail("ana@correo.com"));
        processor.process("TICKET DELETE;T001");
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void historicalTicketsDoNotBlockUserDeletion() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("SYSTEM DATE;2027-04-15");
        assertEquals("ERROR: User has active tickets", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertNotNull(park.getTickets().findById("T001"));
    }

    @Test
    void helpListsTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(help.contains("TICKET DELETE;<identifier>"));
    }
}
