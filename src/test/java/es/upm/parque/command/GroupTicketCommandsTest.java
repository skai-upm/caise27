package es.upm.parque.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.upm.parque.model.GroupTicket;
import es.upm.parque.model.IndividualTicket;
import es.upm.parque.park.ParkController;
import es.upm.parque.repository.UserRepository;

class GroupTicketCommandsTest {

    private static final String[] DNIS = {"12345678Z", "87654321X", "11111111H", "22222222J", "33333333P",
            "44444444A", "55555555K", "66666666Q", "77777777B", "88888888Y", "99999999R"};

    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        processor = new CommandProcessor(new UserRepository());
        park = ParkController.getInstance();
        for (int i = 0; i < DNIS.length; i++) {
            processor.process("USER CREATE;U" + i + ";S;" + DNIS[i] + ";u" + i + "@correo.com;Clave1!;600;"
                    + "123456789012345" + (i % 10));
        }
        processor.process("SYSTEM DATE;2027-04-01");
    }

    private void group(String id, int members) {
        processor.process("GROUP CREATE;" + id + ";Grupo");
        for (int i = 0; i < members; i++) {
            processor.process("GROUP ADD;" + id + ";u" + i + "@correo.com");
        }
    }

    private GroupTicket groupTicket(String id) {
        return (GroupTicket) park.getTickets().findById(id);
    }

    @ParameterizedTest
    @CsvSource({"1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50"})
    void discountBrackets(int members, int percent) {
        assertEquals(percent, GroupTicket.discountPercentFor(members));
    }

    @Test
    void sixMembersAtFortyCost192() {
        group("G02", 6);
        String r = processor.process("GROUPTICKET CREATE;TG02;G02;2027-04-15");
        assertTrue(r.startsWith("OK") && r.contains("192.00"), r);
        GroupTicket tg02 = groupTicket("TG02");
        assertEquals(new BigDecimal("192.00"), tg02.getPrice());
        assertEquals(6, tg02.getComposition().size());
        tg02.getComposition().forEach(t -> assertEquals(new BigDecimal("32.00"), t.getPrice()));
        processor.process("SYSTEM PRICE;50.00");
        assertEquals(new BigDecimal("192.00"), groupTicket("TG02").getPrice());
    }

    @Test
    void bracketPricesAtBoundaries() {
        int[][] cases = {{2, 4000}, {3, 3600}, {5, 3600}, {10, 3200}, {11, 2000}};
        for (int[] c : cases) {
            assertEquals(BigDecimal.valueOf(c[1], 2), GroupTicket.discountedPrice(new BigDecimal("40.00"), c[0]));
        }
    }

    @Test
    void compositionHasDerivedIdsAndReferencesUsersAndGroup() {
        group("G01", 2);
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15").startsWith("OK"));
        GroupTicket tg01 = groupTicket("TG01");
        assertSame(park.getGroups().findById("G01"), tg01.getGroup());
        IndividualTicket first = (IndividualTicket) park.getTickets().findById("TG01:12345678Z");
        assertNotNull(first);
        assertEquals("12345678Z", first.getUser().getDni());
        assertNotNull(park.getTickets().findById("TG01:87654321X"));
        assertTrue(park.isPartOfGroupTicket(first));
        assertThrows(UnsupportedOperationException.class, () -> tg01.getComposition().clear());
    }

    @Test
    void invalidPurchasesAreRejected() {
        processor.process("GROUP CREATE;G00;Vacio");
        group("G01", 2);
        for (String bad : new String[] {
                "GROUPTICKET CREATE;TG01;G00;2027-04-15",
                "GROUPTICKET CREATE;TG01;G99;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01;2027-03-31",
                "GROUPTICKET CREATE;TG01;G01;2027-02-30",
                "GROUPTICKET CREATE;;G01;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertTrue(park.getTickets().findAll().isEmpty());
    }

    @Test
    void identifierConflictCreatesNothing() {
        group("G01", 3);
        processor.process("TICKET CREATE;TG01:87654321X;u1@correo.com;2027-04-20");
        assertTrue(processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15").startsWith("ERROR"));
        assertNull(park.getTickets().findById("TG01"));
        assertNull(park.getTickets().findById("TG01:12345678Z"));
        assertEquals(1, park.getTickets().findAll().size());
        processor.process("TICKET CREATE;TG09;u0@correo.com;2027-04-20");
        assertTrue(processor.process("GROUPTICKET CREATE;TG09;G01;2027-04-15").startsWith("ERROR"));
        assertEquals(2, park.getTickets().findAll().size());
    }

    @Test
    void futureGroupTicketDeletedWithCompositionTodaysKept() {
        group("G01", 2);
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(processor.process("TICKET DELETE;TG01:12345678Z").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG99").startsWith("ERROR"));
        assertTrue(processor.process("GROUPTICKET DELETE;TG01:12345678Z").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertNotNull(groupTicket("TG01"));
        processor.process("SYSTEM DATE;2027-04-10");
        assertTrue(processor.process("GROUPTICKET DELETE;TG01").startsWith("OK"));
        assertTrue(park.getTickets().findAll().isEmpty());
    }

    @Test
    void activeGroupTicketLocksGroupAndMembers() {
        group("G01", 2);
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(processor.process("GROUP ADD;G01;u5@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP REMOVE;G01;u0@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;u0@correo.com;Clave1!").startsWith("ERROR"));
        processor.process("GROUPTICKET DELETE;TG01");
        assertTrue(processor.process("GROUP ADD;G01;u5@correo.com").startsWith("OK"));
    }

    @Test
    void todaysTicketKeepsLockAndHistorySurvivesGroupDeletion() {
        group("G01", 2);
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertFalse(park.hasActiveGroupTicket(park.getGroups().findById("G01")));
        assertTrue(processor.process("GROUP ADD;G01;u5@correo.com").startsWith("OK"));
        assertEquals(2, groupTicket("TG01").getComposition().size());
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("OK"));
        GroupTicket kept = groupTicket("TG01");
        assertNull(kept.getGroup());
        assertEquals(2, kept.getComposition().size());
        assertEquals(new BigDecimal("80.00"), kept.getPrice());
    }

    @Test
    void helpListsGroupTicketCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>"));
        assertTrue(help.contains("GROUPTICKET DELETE;<id>"));
    }
}
