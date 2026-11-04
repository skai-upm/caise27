package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class AttractionTypeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "h", "1", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "h", "2", "9876543210987654");

    private IndividualTicket individual() {
        return new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00"));
    }

    private GroupTicket group() {
        Group g = new Group("G01", "Familia");
        g.addMember(pedro);
        return new GroupTicket("TG01", DAY, g, new BigDecimal("40.00"));
    }

    @Test
    void typedEntryIsCheckedByTheCompiler() throws Exception {
        // These calls only compile because each ticket has the capability the attraction is bounded by.
        new GroupAttraction("A02", "Coches", 10).enter(group(), DAY);
        new IndividualAttraction("A03", "Noria", 10).enter(individual(), DAY);
        new SpecialAttraction("A04", "VIP", 10).enter(individual(), DAY);
        GeneralAttraction general = new GeneralAttraction("A01", "Tornado", 10);
        general.enter(individual(), DAY);
        general.enter(group(), DAY);
        assertEquals(2, general.getPresent().size());
    }

    @Test
    void runtimeAdmissionMatrix() throws Exception {
        assertThrows(UserException.class, () -> new GroupAttraction("A02", "Coches", 10).admit(individual(), DAY));
        assertThrows(UserException.class, () -> new IndividualAttraction("A03", "Noria", 10).admit(group(), DAY));
        assertThrows(UserException.class, () -> new SpecialAttraction("A04", "VIP", 10).admit(group(), DAY));
        UserException e = assertThrows(UserException.class, () -> new GroupAttraction("A02", "Coches", 10).admit(individual(), DAY));
        assertEquals("Attraction A02 (GROUP) does not admit individual tickets", e.getMessage());
        IndividualAttraction ind = new IndividualAttraction("A03", "Noria", 10);
        ind.admit(group().getParts().get(0), DAY);
        assertTrue(ind.isPresent(pedro));
    }

    @Test
    void checkAdmissionNeverChangesTheAttraction() throws Exception {
        GroupAttraction small = new GroupAttraction("A02", "Coches", 1);
        assertThrows(UserException.class, () -> small.checkAdmission(individual(), DAY));
        GeneralAttraction general = new GeneralAttraction("A01", "Tornado", 10);
        general.checkAdmission(individual(), DAY);
        assertTrue(general.getPresent().isEmpty());
        general.admit(individual(), DAY);
        assertEquals(1, general.getPresent().size());
    }

    @Test
    void typesAreCreatedAndParsed() throws Exception {
        assertInstanceOf(GeneralAttraction.class, AttractionType.GENERAL.create("A", "a", 1));
        assertInstanceOf(IndividualAttraction.class, AttractionType.INDIVIDUAL.create("A", "a", 1));
        assertInstanceOf(GroupAttraction.class, AttractionType.GROUP.create("A", "a", 1));
        assertInstanceOf(SpecialAttraction.class, AttractionType.SPECIAL.create("A", "a", 1));
        assertEquals(AttractionType.GROUP, AttractionType.parse("group"));
        assertEquals(AttractionType.SPECIAL, AttractionType.SPECIAL.create("A", "a", 1).getType());
        assertThrows(UserException.class, () -> AttractionType.parse("VIP"));
        assertThrows(UserException.class, () -> AttractionType.parse(null));
    }

    @Test
    void ticketsExposeTheirCapabilities() {
        assertTrue(individual().getUser().isPresent());
        assertEquals(1, group().getParts().size());
    }
}
