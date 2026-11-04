package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTicketCommandsTest {
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
        processor = new CommandProcessor(users, groups, tickets, new AttractionService(new AttractionRepository(dir.resolve("attractions.txt"))), LocalDate.of(2026, 10, 1));
        processor.process("GROUP CREATE;G01;Familia Garcia");
    }

    /** Registers n users (valid DNIs computed from their number) and adds them to G01. */
    private void members(int n) {
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        for (int i = 1; i <= n; i++) {
            int number = 10000000 + i;
            String dni = number + "" + letters.charAt(number % 23);
            String email = "u" + i + "@correo.com";
            String r = processor.process("USER CREATE;U" + i + ";S;" + dni + ";" + email + ";Clave1!;600;1234567890123456");
            assertTrue(r.startsWith("OK"), r);
            assertTrue(processor.process("GROUP ADD;G01;" + email).startsWith("OK"));
        }
    }

    @Test
    void sourcePriceExamples() {
        int[][] cases = {{2, 80}, {3, 108}, {6, 192}, {11, 220}};
        int seq = 0;
        for (int[] c : cases) {
            String gid = "G" + (++seq);
            processor.process("GROUP CREATE;" + gid + ";Grupo");
            String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
            for (int i = 1; i <= c[0]; i++) {
                int number = 20000000 + seq * 100 + i;
                String email = "c" + seq + "u" + i + "@correo.com";
                processor.process("USER CREATE;N;S;" + number + letters.charAt(number % 23) + ";" + email
                        + ";Clave1!;600;1234567890123456");
                processor.process("GROUP ADD;" + gid + ";" + email);
            }
            assertEquals("OK GROUP TICKET CREATED -> " + c[1] + ".00",
                    processor.process("GROUPTICKET CREATE;TG0" + seq + ";" + gid + ";2026-10-15"));
        }
    }

    @Test
    void configuredPriceIsTheBase() {
        members(3);
        processor.process("SYSTEM PRICE;60.00");
        assertEquals("OK GROUP TICKET CREATED -> 162.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
    }

    @Test
    void groupLockedUntilDatePasses() {
        members(3);
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertEquals("OK GROUP TICKET CREATED -> 108.00", processor.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        processor.process("SYSTEM DATE;2026-10-15");
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR: "));
        assertTrue(processor.process("GROUP REMOVE;G01;u1@correo.com").startsWith("ERROR: "));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR: "));
        assertTrue(processor.process("USER DELETE;u1@correo.com;Clave1!").startsWith("ERROR: "));
        assertEquals(3, groups.find("G01").getMembers().size());
        processor.process("SYSTEM DATE;2026-10-16");
        assertEquals("OK: User added to group", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("OK: User deleted", processor.process("USER DELETE;u1@correo.com;Clave1!"));
        assertEquals("OK: Group deleted", processor.process("GROUP DELETE;G01"));
        assertNotNull(tickets.findGroupTicket("TG01"), "historical group ticket retained");
    }

    @Test
    void groupTicketDeleteCommand() {
        members(2);
        processor.process("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        assertTrue(processor.process("TICKET DELETE;TG001-1").startsWith("ERROR: "));
        assertEquals("OK: Group ticket deleted", processor.process("GROUPTICKET DELETE;TG001"));
        assertEquals(0, tickets.getTickets().size());
        assertEquals("OK: User removed from group", processor.process("GROUP REMOVE;G01;u1@correo.com"),
                "lock released once the future group ticket is deleted");
        assertTrue(processor.process("GROUPTICKET DELETE;TG001").startsWith("ERROR: "));
        assertTrue(processor.process("GROUPTICKET DELETE").startsWith("ERROR: "));
        assertTrue(processor.process("GROUPTICKET CREATE;TG2;G01").startsWith("ERROR: "));
    }

    @Test
    void helpListsGroupTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd"));
        assertTrue(help.contains("GROUPTICKET DELETE;Identificador"));
    }
}
