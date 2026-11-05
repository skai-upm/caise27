package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.model.GroupDiscount;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupTicketTest {

    /** Valid DNIs: number n with its check letter. */
    static String dni(int n) {
        return String.format("%08d", n) + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23);
    }

    private Park park;
    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        park = Park.getInstance();
        park.start(new UserRepository());
        p = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()),
                new GroupService(park.users(), park.groups(), park.tickets()),
                new TicketService(park.users(), park.tickets(), park.groups()));
        p.process("SYSTEM DATE;2027-04-01");
        p.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456");
        p.process("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654");
        p.process("USER CREATE;Luis;Martin;" + dni(11) + ";luis@correo.com;Clave1!;6;1111222233334444");
        p.process("GROUP CREATE;G01;Familia");
        p.process("GROUP ADD;G01;ana@correo.com");
        p.process("GROUP ADD;G01;pedro@correo.com");
    }

    private GroupTicket tg(String id) {
        return (GroupTicket) park.tickets().findById(id).orElseThrow();
    }

    private String group(String id, int size) {
        p.process("GROUP CREATE;" + id + ";Big");
        for (int i = 0; i < size; i++) {
            String email = id.toLowerCase() + i + "@correo.com";
            assertEquals("USER CREATED", p.process("USER CREATE;U;S;" + dni(1000 + i + 100 * id.length() + id.hashCode() % 50 * 1000) + ";" + email + ";Clave1!;6;1234567890123456"));
            p.process("GROUP ADD;" + id + ";" + email);
        }
        return id;
    }

    @Test
    void discountBrackets() {
        BigDecimal base = new BigDecimal("40.00");
        assertEquals(new BigDecimal("40.00"), GroupDiscount.forMembers(2).apply(base));
        assertEquals(new BigDecimal("36.00"), GroupDiscount.forMembers(3).apply(base));
        assertEquals(new BigDecimal("36.00"), GroupDiscount.forMembers(5).apply(base));
        assertEquals(new BigDecimal("32.00"), GroupDiscount.forMembers(6).apply(base));
        assertEquals(new BigDecimal("32.00"), GroupDiscount.forMembers(10).apply(base));
        assertEquals(new BigDecimal("20.00"), GroupDiscount.forMembers(11).apply(base));
    }

    @Test
    void sixMembersCost192() {
        group("G06", 6);
        assertEquals("GROUP TICKET CREATED. Total price: 192.00 EUR", p.process("GROUPTICKET CREATE;TG02;G06;2027-04-15"));
        assertEquals(6, tg("TG02").getComposition().size());
        tg("TG02").getComposition().forEach(t -> assertEquals(new BigDecimal("32.00"), t.getPrice()));
    }

    @Test
    void compositionWithDerivedIdentifiers() {
        assertEquals("GROUP TICKET CREATED. Total price: 80.00 EUR", p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        IndividualTicket ana = (IndividualTicket) park.tickets().findById("TG01:12345678Z").orElseThrow();
        assertEquals("ana@correo.com", ana.getUser().getEmail());
        assertTrue(park.tickets().findById("TG01:87654321X").isPresent());
        assertTrue(tg("TG01").contains(ana));
    }

    @Test
    void emptyPastAndUnknownRejected() {
        p.process("GROUP CREATE;G03;Vacio");
        assertTrue(p.process("GROUPTICKET CREATE;TG03;G03;2027-04-15").startsWith("ERROR"));
        assertTrue(p.process("GROUPTICKET CREATE;TG03;G01;2027-03-01").startsWith("ERROR"));
        assertTrue(p.process("GROUPTICKET CREATE;TG03;G99;2027-04-15").startsWith("ERROR"));
        assertTrue(park.tickets().findAll().isEmpty());
    }

    @Test
    void identifierConflictsCreateNothing() {
        p.process("TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-20");
        assertTrue(p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15").startsWith("ERROR"));
        assertEquals(1, park.tickets().findAll().size());
        p.process("TICKET CREATE;T001;ana@correo.com;2027-04-20");
        assertTrue(p.process("GROUPTICKET CREATE;T001;G01;2027-04-15").startsWith("ERROR"));
        assertEquals(2, park.tickets().findAll().size());
    }

    @Test
    void deleteFutureGroupTicketWithComposition() {
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(p.process("TICKET DELETE;TG01:12345678Z").startsWith("ERROR"));
        assertEquals("GROUP TICKET DELETED", p.process("GROUPTICKET DELETE;TG01"));
        assertTrue(park.tickets().findAll().isEmpty());
    }

    @Test
    void todaysGroupTicketKept() {
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        p.process("SYSTEM DATE;2027-04-15");
        assertTrue(p.process("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(p.process("GROUPTICKET DELETE;NOPE").startsWith("ERROR"));
        assertEquals(3, park.tickets().findAll().size());
    }

    @Test
    void activeTicketLocksGroup() {
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(p.process("GROUP ADD;G01;luis@correo.com").startsWith("ERROR"));
        assertTrue(p.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(p.process("GROUP DELETE;G01").startsWith("ERROR"));
        p.process("GROUPTICKET DELETE;TG01");
        assertEquals("USER ADDED TO GROUP", p.process("GROUP ADD;G01;luis@correo.com"));
    }

    @Test
    void historicalTicketsDoNotLockAndSurviveGroupDeletion() {
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        p.process("SYSTEM DATE;2027-04-16");
        assertEquals("USER ADDED TO GROUP", p.process("GROUP ADD;G01;luis@correo.com"));
        assertEquals(2, tg("TG01").getComposition().size());
        assertEquals("GROUP DELETED", p.process("GROUP DELETE;G01"));
        assertNull(tg("TG01").getGroup());
        assertEquals("G01", tg("TG01").getGroupIdentifier());
        assertEquals(new BigDecimal("80.00"), tg("TG01").getPrice());
        assertTrue(p.process("GROUP DELETE;G99").startsWith("ERROR"));
    }

    @Test
    void groupTicketsBlockUserDeletion() {
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        p.process("GROUPTICKET DELETE;TG01");
        p.process("GROUP ADD;G01;luis@correo.com");
        p.process("GROUP REMOVE;G01;pedro@correo.com");
        p.process("GROUPTICKET CREATE;TG02;G01;2027-04-15");
        p.process("GROUP CREATE;G02;Otro");
        p.process("GROUP ADD;G02;pedro@correo.com");
        assertEquals("USER DELETED", p.process("USER DELETE;pedro@correo.com;Otra2#"));
    }

    /** Defensive check: membership of a locked group blocks deletion even without own ticket. */
    @Test
    void memberOfLockedGroupWithoutTicketNotDeleted() {
        p.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        p.process("GROUPTICKET DELETE;TG01");
        p.process("GROUP ADD;G01;luis@correo.com");
        p.process("GROUPTICKET CREATE;TG02;G01;2027-04-15");
        park.tickets().remove(park.tickets().findById("TG02:" + dni(11)).orElseThrow());
        assertTrue(p.process("USER DELETE;luis@correo.com;Clave1!").startsWith("ERROR"));
        assertTrue(park.groups().findById("G01").orElseThrow().contains(park.users().findByEmail("luis@correo.com").orElseThrow()));
    }
}
