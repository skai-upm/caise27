package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionCommandsTest {
    @TempDir
    Path dir;
    CommandProcessor processor;
    AttractionService attractions;

    private void ok(String command) {
        String r = processor.process(command);
        assertTrue(r.startsWith("OK"), command + " -> " + r);
    }

    @BeforeEach
    void setUp() throws Exception {
        UserService users = new UserService(new UserRepository(dir.resolve("users.txt")));
        GroupService groups = new GroupService(new GroupRepository(dir.resolve("groups.txt")), users);
        TicketService tickets = new TicketService(new TicketRepository(dir.resolve("tickets.txt")),
                new GroupTicketRepository(dir.resolve("grouptickets.txt")), users, groups);
        attractions = new AttractionService(new AttractionRepository(dir.resolve("attractions.txt")));
        processor = new CommandProcessor(users, groups, tickets, attractions, LocalDate.of(2026, 10, 1));
        ok("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        ok("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        ok("USER CREATE;Luis;Perez;11111111H;luis@correo.com;Luis3$;634567890;1111222233334444");
        ok("USER CREATE;Marta;Ruiz;22222222J;marta@correo.com;Marta4%;645678901;2222333344445555");
        ok("GROUP CREATE;G01;Amigos");
        ok("GROUP ADD;G01;pedro@correo.com");
        ok("GROUP ADD;G01;luis@correo.com");
        ok("GROUP ADD;G01;marta@correo.com");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("ATTRACTION CREATE;A01;Tornado;20");
    }

    @Test
    void sourceSequence() {
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR: "), "not valid before the date");
        ok("SYSTEM DATE;2026-10-15");
        assertEquals("OK: 1 user(s) entered attraction A01", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK: 3 user(s) entered attraction A01", processor.process("ATTRACTION ENTER;A01;TG01"));
        List<String> status = processor.process("ATTRACTION STATUS;A01").lines().map(String::trim).toList();
        assertEquals("OK: Attraction A01 - Tornado", status.get(0));
        assertTrue(status.contains("Maximum capacity: 20"));
        assertTrue(status.contains("Users present: 4"));
        assertTrue(status.contains("Available places: 16"));
        for (String e : List.of("ana", "pedro", "luis", "marta")) {
            assertTrue(status.stream().anyMatch(l -> l.contains("<" + e + "@correo.com>")), e);
        }
        assertEquals("OK: 1 user(s) left attraction A01", processor.process("ATTRACTION EXIT;A01;T001"));
        List<String> after = processor.process("ATTRACTION STATUS;A01").lines().map(String::trim).toList();
        assertTrue(after.contains("Users present: 3"));
        assertTrue(after.contains("Available places: 17"));
        assertFalse(after.stream().anyMatch(l -> l.contains("ana@correo.com")));
    }

    @Test
    void groupDoesNotFitEntersNobody() {
        ok("SYSTEM DATE;2026-10-15");
        ok("ATTRACTION CREATE;A02;Mini;3");
        ok("TICKET CREATE;T002;ana@correo.com;2026-10-15");
        ok("ATTRACTION ENTER;A02;T002");
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01").startsWith("ERROR: "));
        assertEquals(1, attractions.find("A02").getPresent().size());
        assertTrue(processor.process("ATTRACTION STATUS;A02").contains("Available places: 2"));
    }

    @Test
    void exitDoesNotCheckDate() {
        ok("SYSTEM DATE;2026-10-15");
        ok("ATTRACTION ENTER;A01;TG01");
        ok("SYSTEM DATE;2026-10-16");
        assertEquals("OK: 3 user(s) left attraction A01", processor.process("ATTRACTION EXIT;A01;TG01"));
        assertTrue(processor.process("ATTRACTION EXIT;A01;TG01").startsWith("ERROR: "));
    }

    @Test
    void errors() {
        ok("SYSTEM DATE;2026-10-15");
        for (String c : List.of("ATTRACTION ENTER;A99;T001", "ATTRACTION ENTER;A01;T999", "ATTRACTION STATUS;A99",
                "ATTRACTION EXIT;A01;T001", "ATTRACTION ENTER;A01", "ATTRACTION STATUS", "ATTRACTION CREATE;A01;X;5",
                "ATTRACTION CREATE;A03;X;0")) {
            assertTrue(processor.process(c).startsWith("ERROR: "), c);
        }
        assertTrue(processor.process("ATTRACTION STATUS;A01").contains("Present: none"));
    }

    @Test
    void helpListsAttractionCommands() {
        String help = processor.process("HELP");
        for (String l : List.of("ATTRACTION CREATE;Identificador;Nombre;Aforo", "ATTRACTION ENTER;Identificador;Ticket",
                "ATTRACTION EXIT;Identificador;Ticket", "ATTRACTION STATUS;Identificador")) {
            assertTrue(help.contains(l), l);
        }
    }
}
