package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TicketCommandsTest {
    static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    UserService users;
    GroupService groups;
    TicketService tickets;
    CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserService(new UserRepository(dir.resolve("users.txt")));
        groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups);
        processor = new CommandProcessor(users, groups, tickets, new AttractionService(new AttractionRepository(dir.resolve("attractions.txt"))), LocalDate.of(2026, 9, 15));
        processor.process(ANA);
    }

    @Test
    void sourceExampleSequence() {
        assertEquals("OK: System date set to 2026-10-01", processor.process("SYSTEM DATE;2026-10-01"));
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR: "));
        assertNotNull(users.findByEmail("ana@correo.com"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR: "), "today blocks");
        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK: User deleted", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertNotNull(tickets.find("T001"), "past ticket retained as history");
    }

    @Test
    void blockedDeletionKeepsGroupMembership() {
        processor.process("GROUP CREATE;G01;Familia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR: "));
        assertEquals(1, groups.find("G01").getMembers().size());
    }

    @Test
    void wrongPasswordStillReportedAsAuthenticationError() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        assertEquals("ERROR: Incorrect password", processor.process("USER DELETE;ana@correo.com;Mala1!"));
    }

    @Test
    void priceChangesAffectOnlyLaterTickets() {
        assertEquals(new BigDecimal("40.00"), processor.getTicketPrice());
        assertEquals("OK TICKET CREATED -> 40.00", processor.process("TICKET CREATE;T001;ana@correo.com;2026-10-15"));
        assertEquals("OK: Ticket price set to 60.00", processor.process("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", processor.process("TICKET CREATE;T002;ana@correo.com;2026-10-15"));
        assertEquals("40.00", tickets.find("T001").getPriceText());
        assertEquals("OK: Ticket price set to 55.50", processor.process("SYSTEM PRICE;55.5"));
        assertEquals("OK: Ticket price set to 70.00", processor.process("SYSTEM PRICE;70"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM PRICE;abc", "SYSTEM PRICE;-5", "SYSTEM PRICE;0", "SYSTEM PRICE;0.00",
            "SYSTEM PRICE;60,00", "SYSTEM PRICE;60.001", "SYSTEM PRICE;60.", "SYSTEM PRICE;.5", "SYSTEM PRICE",
            "SYSTEM PRICE;", "SYSTEM PRICE;1e3", "SYSTEM PRICE;60.00;x"})
    void invalidPricesRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR: "), command);
        assertEquals(new BigDecimal("40.00"), processor.getTicketPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TICKET CREATE;T001;nadie@correo.com;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-09-14",
            "TICKET CREATE;T001;ana@correo.com;2026-10-32", "TICKET CREATE;T001;ana@correo.com",
            "TICKET CREATE;;ana@correo.com;2026-10-15", "TICKET DELETE;T999", "TICKET DELETE"})
    void invalidTicketCommandsRejected(String command) {
        assertTrue(processor.process(command).startsWith("ERROR: "), command);
        assertEquals(0, tickets.getTickets().size());
    }

    @Test
    void ticketDeleteBoundaries() {
        processor.process("TICKET CREATE;T001;ana@correo.com;2026-09-16");
        processor.process("TICKET CREATE;T002;ana@correo.com;2026-09-15");
        assertTrue(processor.process("TICKET DELETE;T002").startsWith("ERROR: "), "today is not future");
        assertEquals("OK: Ticket deleted", processor.process("TICKET DELETE;T001"));
        assertNull(tickets.find("T001"));
    }

    @Test
    void helpListsTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("TICKET CREATE;Identificador;Correo;yyyy-MM-dd"));
        assertTrue(help.contains("TICKET DELETE;Identificador"));
        assertTrue(help.contains("SYSTEM PRICE;Precio"));
    }
}
