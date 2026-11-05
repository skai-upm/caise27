package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class AttractionTypeTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal P40 = new BigDecimal("40.00");

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "h", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez", "87654321X", "pedro@correo.com", "h", "6", "9876543210987654");

    private IndividualTicket individual() {
        return new IndividualTicket("T001", DAY, P40, ana);
    }

    private GroupTicket group() {
        Group g = new Group("G01", "Familia");
        g.addMember(pedro);
        return GroupTicket.purchase("TG01", DAY, g, P40);
    }

    private void assertAdmits(AttractionType type, boolean individual, boolean group) {
        Attraction<?> a = type.create("A", "X", 10);
        assertEquals(type, a.getType());
        assertEquals(individual, a.enter(individual(), DAY).success(), type + " individual");
        assertEquals(group, a.enter(group(), DAY).success(), type + " group");
        assertEquals((individual ? 1 : 0) + (group ? 1 : 0), a.getPresent().size());
    }

    @Test
    void eachTypeAdmitsItsTicketKinds() {
        assertAdmits(AttractionType.GENERAL, true, true);
        assertAdmits(AttractionType.INDIVIDUAL, true, false);
        assertAdmits(AttractionType.GROUP, false, true);
        assertAdmits(AttractionType.SPECIAL, true, false);
    }

    @Test
    void rejectionMessageAndNobodyAdmitted() {
        Attraction<?> a = AttractionType.GROUP.create("A02", "Familias", 30);
        Result r = a.enter(individual(), DAY);
        assertEquals("ERROR: Attraction A02 (GROUP) does not admit ticket T001.", r.toString());
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void boundedTypeParameterFixesTheAdmittedClass() {
        assertEquals(IndividualTicket.class, new SpecialAttraction("S", "X", 1).getAdmittedClass());
        assertEquals(IndividualTicket.class, new IndividualAttraction("I", "X", 1).getAdmittedClass());
        assertEquals(GroupTicket.class, new GroupAttraction("G", "X", 1).getAdmittedClass());
        assertEquals(Ticket.class, new GeneralAttraction("A", "X", 1).getAdmittedClass());
        assertFalse(new GroupAttraction("G", "X", 1).accepts(individual()));
    }

    @Test
    void typedHookRunsBeforeAdmission() {
        Attraction<IndividualTicket> picky = new Attraction<>("P", "Picky", 5, IndividualTicket.class) {
            @Override
            public AttractionType getType() {
                return AttractionType.SPECIAL;
            }

            @Override
            protected Result checkAdditional(IndividualTicket ticket, LocalDate today) {
                return ticket.getUser() == ana ? Result.error("no") : null;
            }
        };
        assertFalse(picky.enter(individual(), DAY).success());
        assertTrue(picky.getPresent().isEmpty());
    }

    @Test
    void parseIsExactUppercase() {
        assertEquals(Optional.of(AttractionType.GROUP), AttractionType.parse("GROUP"));
        assertTrue(AttractionType.parse("group").isEmpty());
        assertTrue(AttractionType.parse("VIP").isEmpty());
    }

    @Test
    void onlySpecialRequiresSpecialPassAndForbidsSpeed() {
        for (AttractionType type : AttractionType.values()) {
            Attraction<?> a = type.create("A", "X", 1);
            assertEquals(type == AttractionType.SPECIAL, a.requiresSpecialPass(), type.name());
            assertEquals(type != AttractionType.SPECIAL, a.allowsSpeed(), type.name());
        }
    }
}
