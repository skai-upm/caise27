package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.parque.Fixtures;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private final IndividualTicket individual = new IndividualTicket("T001", DAY, Fixtures.ana(), FORTY);
    private final GroupTicket group = new GroupTicket("TG01", DAY, new Group("G01", "G"), Fixtures.users(2),
            Collections.nCopies(2, FORTY), 0);

    @Test
    void tracksPresentUsersAndCapacity() {
        Attraction<Admission> tornado = new GeneralAttraction("A01", "Tornado", 3);
        List<User> visitors = Fixtures.users(2);
        User extra = Fixtures.ana();
        assertEquals("A01", tornado.getId());
        assertEquals("Tornado", tornado.getName());
        assertEquals(3, tornado.getCapacity());
        assertTrue(tornado.isEmpty());
        assertTrue(tornado.canAdmit(visitors));
        tornado.admit(visitors);
        assertEquals(1, tornado.available());
        assertTrue(tornado.contains(visitors.get(0)));
        assertFalse(tornado.canAdmit(Fixtures.users(2)));
        assertTrue(tornado.canAdmit(List.of(extra)));
        assertEquals(0, tornado.release(List.of(extra)));
        assertEquals(2, tornado.release(visitors));
        assertTrue(tornado.isEmpty());
        List<User> present = tornado.getPresent();
        assertThrows(UnsupportedOperationException.class, () -> present.add(extra));
    }

    @Test
    void generalAdmitsBothTicketKinds() {
        Attraction<Admission> general = new GeneralAttraction("A01", "Tornado", 5);
        assertEquals(AttractionType.GENERAL, general.getType());
        assertTrue(general.accepts(individual));
        assertTrue(general.accepts(group));
        general.enter(general.cast(group));
        general.enter(general.cast(individual));
        assertEquals(3, general.getPresent().size());
    }

    @Test
    void individualAndSpecialAdmitOnlyIndividualTickets() {
        Attraction<IndividualAdmission> single = new IndividualAttraction("A02", "Sillas", 1);
        Attraction<IndividualAdmission> special = new SpecialAttraction("A03", "VIP", 1);
        assertEquals(AttractionType.INDIVIDUAL, single.getType());
        assertEquals(AttractionType.SPECIAL, special.getType());
        assertFalse(single.accepts(group));
        assertFalse(special.accepts(group));
        IndividualAdmission admission = special.cast(individual);
        assertSame(individual.getHolder(), admission.getHolder());
        assertTrue(special.hasRoomFor(admission));
        special.enter(admission);
        assertFalse(special.hasRoomFor(admission));
        assertThrows(ClassCastException.class, () -> single.cast(group));
    }

    @Test
    void groupAdmitsOnlyGroupTickets() {
        Attraction<GroupAdmission> boats = new GroupAttraction("A04", "Barcas", 6);
        assertEquals(AttractionType.GROUP, boats.getType());
        assertFalse(boats.accepts(individual));
        GroupAdmission admission = boats.cast(group);
        assertEquals(2, admission.getTickets().size());
        boats.enter(admission);
        assertEquals(4, boats.available());
    }

    @Test
    void typesParseAndCreateAttractions() {
        assertEquals(AttractionType.GROUP, AttractionType.parse(" group "));
        assertNull(AttractionType.parse("VIP"));
        assertInstanceOf(GeneralAttraction.class, AttractionType.GENERAL.create("A", "a", 1));
        assertInstanceOf(IndividualAttraction.class, AttractionType.INDIVIDUAL.create("A", "a", 1));
        assertInstanceOf(GroupAttraction.class, AttractionType.GROUP.create("A", "a", 1));
        assertInstanceOf(SpecialAttraction.class, AttractionType.SPECIAL.create("A", "a", 1));
    }

    @Test
    void parsesCapacities() {
        assertEquals(20, Attraction.parseCapacity("20"));
        assertNull(Attraction.parseCapacity("0"));
        assertNull(Attraction.parseCapacity("x"));
    }

    @Test
    void admissionsExposeTheirIndividualTickets() {
        assertEquals(List.of(individual), individual.getIndividualTickets());
        assertEquals(group.getTickets(), group.getIndividualTickets());
    }

    @Test
    void onlySpecialAttractionsRestrictPasses() {
        Attraction<Admission> general = new GeneralAttraction("A01", "Tornado", 5);
        assertTrue(general.allowsSpeed());
        assertTrue(general.checkPasses(group).getPasses().isEmpty());
        assertFalse(general.checkPasses(group).isRejected());
        SpecialAttraction special = new SpecialAttraction("A04", "VIP", 5);
        assertFalse(special.allowsSpeed());
        assertEquals(SpecialAttraction.NO_SPECIAL_PASS, special.checkPasses(individual).getRejection());
        SpecialPass pass = new SpecialPass("T001-SPECIAL", FORTY, false);
        individual.setSpecialPass(pass);
        assertEquals(List.of(pass), special.checkPasses(individual).getPasses());
        pass.consume();
        EntryCheck consumed = special.checkPasses(individual);
        assertTrue(consumed.isRejected());
        assertEquals(SpecialAttraction.SPECIAL_PASS_CONSUMED, consumed.getRejection());
    }
}
