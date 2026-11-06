package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTypesTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "p@correo.com", "x", "6", "9876543210987654");
    private final IndividualTicket single = new IndividualTicket("T001", DAY, new BigDecimal("40.00"), ana);
    private final GroupTicket group;

    AttractionTypesTest() {
        Group g = new Group("G01", "F");
        g.addMember(ana);
        g.addMember(pedro);
        group = new GroupTicket("TG01", DAY, new BigDecimal("40.00"), g, List.of(ana, pedro));
    }

    @Test
    void factoryCreatesEachType() {
        assertInstanceOf(GeneralAttraction.class, AttractionType.GENERAL.create("A01", "x", 1));
        assertInstanceOf(IndividualAttraction.class, AttractionType.INDIVIDUAL.create("A02", "x", 1));
        assertInstanceOf(GroupAttraction.class, AttractionType.GROUP.create("A03", "x", 1));
        assertInstanceOf(SpecialAttraction.class, AttractionType.SPECIAL.create("A04", "x", 1));
        for (AttractionType t : AttractionType.values()) {
            assertEquals(t, t.create("A", "x", 1).getType());
        }
        assertEquals(AttractionType.SPECIAL, AttractionType.fromName("SPECIAL"));
        assertNull(AttractionType.fromName("VIP"));
    }

    @Test
    void boundsDecideAdmissibleTickets() {
        assertTrue(new GeneralAttraction("A01", "x", 9).admissible(single).isPresent());
        assertTrue(new GeneralAttraction("A01", "x", 9).admissible(group).isPresent());
        assertTrue(new IndividualAttraction("A02", "x", 9).admissible(single).isPresent());
        assertFalse(new IndividualAttraction("A02", "x", 9).admissible(group).isPresent());
        assertFalse(new GroupAttraction("A03", "x", 9).admissible(single).isPresent());
        assertTrue(new GroupAttraction("A03", "x", 9).admissible(group).isPresent());
        assertTrue(new SpecialAttraction("A04", "x", 9).admissible(single).isPresent());
        assertFalse(new SpecialAttraction("A04", "x", 9).admissible(group).isPresent());
    }

    @Test
    void boundOperationsGiveTheHolders() {
        IndividualAttraction individual = new IndividualAttraction("A02", "x", 9);
        assertEquals(List.of(ana), individual.holdersOf(single));
        GroupAttraction groupAttraction = new GroupAttraction("A03", "x", 9);
        assertEquals(List.of(ana, pedro), groupAttraction.holdersOf(group));
        GeneralAttraction general = new GeneralAttraction("A01", "x", 9);
        assertEquals(List.of(ana, pedro), general.holdersOf(group));
    }

    @Test
    void specialAttractionNeedsAnUnconsumedSpecialPass() {
        SpecialAttraction special = new SpecialAttraction("A04", "x", 9);
        assertEquals("ticket T001 needs an unconsumed special pass for attraction A04",
                special.missingRequirement(single, t -> List.of()).orElseThrow());
        assertTrue(special.missingRequirement(single, t -> List.of(new SpeedPass("SP:1", single))).isPresent());
        SpecialPass pass = new SpecialPass("PE01", single);
        assertTrue(special.missingRequirement(single, t -> List.of(pass)).isEmpty());
        pass.consume();
        assertTrue(special.missingRequirement(single, t -> List.of(pass)).isPresent());
        assertTrue(new IndividualAttraction("A02", "x", 9).missingRequirement(single, t -> List.of()).isEmpty());
    }
}
