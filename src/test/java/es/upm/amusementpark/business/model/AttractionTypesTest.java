package es.upm.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.exception.OperationNotAllowedException;

class AttractionTypesTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "x", "6", "9876543210987654");
    private final IndividualTicket single = new IndividualTicket("T001", DAY, ana, new BigDecimal("40.00"));
    private final GroupTicket group = new GroupTicket("TG01", DAY, new Group("G01", "g"), List.of(ana, pedro),
            new BigDecimal("40.00"));
    private final List<Pass> passes = new ArrayList<>();
    private final PassLookup lookup = t -> passes.stream().filter(p -> p.getTicket() == t).toList();

    @Test
    void acceptedTicketTypes() {
        assertTrue(new GeneralAttraction("A", "a", 5).accepts(single));
        assertTrue(new GeneralAttraction("A", "a", 5).accepts(group));
        assertTrue(new IndividualAttraction("A", "a", 5).accepts(single));
        assertFalse(new IndividualAttraction("A", "a", 5).accepts(group));
        assertFalse(new GroupAttraction("A", "a", 5).accepts(single));
        assertTrue(new GroupAttraction("A", "a", 5).accepts(group));
        assertTrue(new SpecialAttraction("A", "a", 5).accepts(single));
        assertFalse(new SpecialAttraction("A", "a", 5).accepts(group));
    }

    @Test
    void rejectedTypeLetsNobodyIn() {
        Attraction<?> individual = AttractionType.INDIVIDUAL.create("A02", "a", 5);
        assertThrows(OperationNotAllowedException.class, () -> individual.enterAny(group, DAY, lookup));
        assertTrue(individual.getOccupants().isEmpty());
        Attraction<?> groupOnly = AttractionType.GROUP.create("A03", "a", 5);
        assertThrows(OperationNotAllowedException.class, () -> groupOnly.enterAny(single, DAY, lookup));
    }

    @Test
    void typedEntryWithGroupTicket() throws Exception {
        GroupAttraction rafting = new GroupAttraction("A03", "Rafting", 24);
        assertEquals(List.of(ana, pedro), rafting.enter(group, DAY, lookup));
    }

    @Test
    void specialAttractionNeedsAnUnconsumedSpecialPass() throws Exception {
        SpecialAttraction terror = new SpecialAttraction("A04", "Terror", 15);
        assertThrows(OperationNotAllowedException.class, () -> terror.enter(single, DAY, lookup));
        passes.add(new SpeedPass("SP:1", single));
        assertThrows(OperationNotAllowedException.class, () -> terror.enter(single, DAY, lookup));
        SpecialPass special = new SpecialPass("PE01", single);
        special.consume();
        passes.add(special);
        assertThrows(OperationNotAllowedException.class, () -> terror.enter(single, DAY, lookup));
        passes.add(new SpecialPass("PE02", new IndividualTicket("T002", DAY, pedro, BigDecimal.ONE)));
        passes.set(passes.size() - 1, new SpecialPass("PE03", single));
        assertEquals(List.of(ana), terror.enter(single, DAY, lookup));
    }

    @Test
    void typeNames() {
        assertEquals(AttractionType.SPECIAL, AttractionType.fromName("SPECIAL").orElseThrow());
        assertTrue(AttractionType.fromName("special").isEmpty());
    }
}
