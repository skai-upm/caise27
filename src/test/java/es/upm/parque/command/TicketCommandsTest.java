package es.upm.parque.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.model.IndividualTicket;
import es.upm.parque.model.Ticket;
import es.upm.parque.park.ParkController;
import es.upm.parque.repository.UserRepository;

class TicketCommandsTest {

    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new UserRepository());
        park = ParkController.getInstance();
        processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457");
        processor.process("SYSTEM DATE;2027-04-01");
    }

    private Ticket ticket(String id) {
        return park.getTickets().findById(id);
    }

    @Test
    void ticketIsActiveFromItsDateAndHistoricalAfter() {
        Ticket t = new IndividualTicket("X", park.getUsers().findByDni("12345678Z"),
                LocalDate.of(2027, 4, 15), new BigDecimal("40.00"));
        assertTrue(t.isActiveOn(LocalDate.of(2027, 4, 15)));
        assertFalse(t.isActiveOn(LocalDate.of(2027, 4, 16)));
        assertTrue(t.isDeletableOn(LocalDate.of(2027, 4, 14)));
        assertFalse(t.isDeletableOn(LocalDate.of(2027, 4, 15)));
    }

    @Test
    void createsTicketAtInitialAndConfiguredPrice() {
        String r = processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(r.startsWith("OK") && r.contains("40.00"), r);
        IndividualTicket t001 = (IndividualTicket) ticket("T001");
        assertSame(park.getUsers().findByDni("12345678Z"), t001.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t001.getDate());

        processor.process("SYSTEM PRICE;45.50");
        r = processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-15");
        assertTrue(r.contains("45.50"), r);
        processor.process("SYSTEM PRICE;50.00");
        assertEquals(new BigDecimal("40.00"), ticket("T001").getPrice());
        assertEquals(new BigDecimal("45.50"), ticket("T002").getPrice());
    }

    @Test
    void todayIsAllowedButInvalidInputIsRejected() {
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("TICKET CREATE;T003;ana@correo.com;2027-04-15").startsWith("OK"));
        for (String bad : new String[] {
                "TICKET CREATE;T004;ana@correo.com;2027-04-14",
                "TICKET CREATE;T004;nadie@correo.com;2027-04-20",
                "TICKET CREATE;T004;ana@correo.com;2027-02-30",
                "TICKET CREATE;;ana@correo.com;2027-04-20",
                "TICKET CREATE;T003;pedro@correo.com;2027-04-20",
                "TICKET CREATE;T004;ana@correo.com"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertNull(ticket("T004"));
        assertEquals(1, park.getTickets().findAll().size());
    }

    @Test
    void onlyFutureTicketsCanBeDeleted() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("OK"));
        assertNull(ticket("T002"));
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertNotNull(ticket("T001"));
        assertTrue(processor.process("TICKET DELETE;T999").startsWith("ERROR"));
        assertTrue(processor.process("TICKET DELETE").startsWith("ERROR"));
    }

    @Test
    void activeTicketBlocksUserDeletionUntilDeleted() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(park.getUsers().existsByDni("12345678Z"));
        processor.process("TICKET DELETE;T001");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
    }

    @Test
    void historicalTicketDoesNotBlockDeletionAndKeepsUser() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("SYSTEM DATE;2027-04-16");
        assertFalse(park.hasActiveTicket(park.getUsers().findByDni("12345678Z")));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK"));
        IndividualTicket kept = (IndividualTicket) ticket("T001");
        assertEquals("12345678Z", kept.getUser().getDni());
        assertFalse(park.getUsers().existsByDni("12345678Z"));
    }

    @Test
    void deletedMemberLeavesUnlockedGroupsAndTicketsResetPerExecution() {
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        assertFalse(park.belongsToLockedGroup(park.getUsers().findByDni("87654321X")));
        assertTrue(processor.process("USER DELETE;pedro@correo.com;Otra2#").startsWith("OK"));
        assertTrue(park.getGroups().findById("G01").getMembers().isEmpty());
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        new CommandProcessor(new UserRepository());
        assertTrue(park.getTickets().findAll().isEmpty());
    }

    @Test
    void helpListsTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(help.contains("TICKET DELETE;<identifier>"));
    }
}
