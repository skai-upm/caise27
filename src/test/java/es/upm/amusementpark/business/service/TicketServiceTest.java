package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TicketServiceTest {

    private Park park;
    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        park = Park.getInstance();
        park.start(new UserRepository());
        p = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()),
                new GroupService(park.users(), park.groups()), new TicketService(park.users(), park.tickets()));
        p.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        p.process("SYSTEM DATE;2027-04-01");
    }

    private Ticket ticket(String id) {
        return park.tickets().findById(id).orElseThrow();
    }

    @Test
    void createdAtInitialPriceAndReferencesUser() {
        assertEquals("TICKET CREATED. Price: 40.00 EUR", p.process("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        IndividualTicket t = (IndividualTicket) ticket("T001");
        assertEquals(LocalDate.of(2027, 4, 15), t.getDate());
        assertEquals("12345678Z", t.getUser().getDni());
    }

    @Test
    void configuredPriceAndLaterChangesDoNotAlterExisting() {
        p.process("SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED. Price: 45.50 EUR", p.process("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        p.process("SYSTEM PRICE;50");
        assertEquals(new BigDecimal("45.50"), ticket("T001").getPrice());
    }

    @Test
    void todayAllowedPastAndUnknownRejected() {
        assertTrue(p.process("TICKET CREATE;T002;ana@correo.com;2027-04-01").startsWith("TICKET CREATED"));
        assertTrue(p.process("TICKET CREATE;T003;ana@correo.com;2027-03-31").startsWith("ERROR"));
        assertTrue(p.process("TICKET CREATE;T004;nadie@correo.com;2027-04-20").startsWith("ERROR"));
        assertTrue(p.process("TICKET CREATE;T002;ana@correo.com;2027-04-20").startsWith("ERROR"));
        assertTrue(p.process("TICKET CREATE;T005;ana@correo.com;2027-02-30").startsWith("ERROR"));
        assertEquals(1, park.tickets().findAll().size());
    }

    @Test
    void deleteOnlyFutureTickets() {
        p.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        p.process("TICKET CREATE;T002;ana@correo.com;2027-04-20");
        assertEquals("TICKET DELETED", p.process("TICKET DELETE;T002"));
        p.process("SYSTEM DATE;2027-04-15");
        assertTrue(p.process("TICKET DELETE;T001").startsWith("ERROR"));
        p.process("SYSTEM DATE;2027-05-01");
        assertTrue(p.process("TICKET DELETE;T001").startsWith("ERROR"));
        assertTrue(park.tickets().findById("T001").isPresent());
        assertTrue(p.process("TICKET DELETE;NOPE").startsWith("ERROR"));
    }

    @Test
    void activeTicketsBlockUserDeletion() {
        p.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        p.process("SYSTEM DATE;2027-04-15");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        p.process("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", p.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("12345678Z", ((IndividualTicket) ticket("T001")).getUser().getDni());
    }

    @Test
    void deletingFutureTicketUnblocksUser() {
        p.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("TICKET DELETED", p.process("TICKET DELETE;T001"));
        assertEquals("USER DELETED", p.process("USER DELETE;ana@correo.com;Clave1!"));
    }
}
