package es.upm.park.business.model;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionTypesTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final EntryRules NO_PASSES = new TestEntryRules();
    private final TestEntryRules allPasses = new TestEntryRules().specialPass("T001", "PE01");

    private final IndividualTicket individual =
            new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal("40.00"));
    private final GroupTicket group;

    AttractionTypesTest() {
        Map<String, String> members = new LinkedHashMap<>();
        members.put("pedro@correo.com", "87654321X");
        members.put("luis@correo.com", "11111111H");
        group = GroupTicket.purchase("TG01", new Group("G01", "g"), DAY, new BigDecimal("40.00"), members);
    }

    @Test
    void acceptanceMatrix() {
        assertTrue(new GeneralAttraction("A01", "x", 20).canHandle(individual));
        assertTrue(new GeneralAttraction("A01", "x", 20).canHandle(group));
        assertTrue(new IndividualAttraction("A02", "x", 20).canHandle(individual));
        assertFalse(new IndividualAttraction("A02", "x", 20).canHandle(group));
        assertFalse(new GroupAttraction("A03", "x", 20).canHandle(individual));
        assertTrue(new GroupAttraction("A03", "x", 20).canHandle(group));
        assertTrue(new SpecialAttraction("A04", "x", 20).canHandle(individual));
        assertFalse(new SpecialAttraction("A04", "x", 20).canHandle(group));
    }

    @Test
    void rejectedKindsAdmitNobody() {
        Attraction<?> individualOnly = new IndividualAttraction("A02", "x", 20);
        assertThrows(BusinessRuleException.class, () -> individualOnly.admit(group, DAY, NO_PASSES));
        assertTrue(individualOnly.getOccupantEmails().isEmpty());
        Attraction<?> groupOnly = new GroupAttraction("A03", "x", 20);
        assertThrows(BusinessRuleException.class, () -> groupOnly.admit(individual, DAY, NO_PASSES));
        assertEquals(List.of("pedro@correo.com", "luis@correo.com"), groupOnly.admit(group, DAY, NO_PASSES));
    }

    @Test
    void specialRequiresUnconsumedSpecialPass() {
        Attraction<?> special = new SpecialAttraction("A04", "x", 20);
        assertThrows(BusinessRuleException.class, () -> special.admit(individual, DAY, NO_PASSES));
        assertTrue(special.getOccupantEmails().isEmpty());
        assertEquals(List.of("ana@correo.com"), special.admit(individual, DAY, allPasses));
    }

    @Test
    void typesAndFactory() {
        assertEquals(AttractionType.SPECIAL, AttractionType.parse("SPECIAL").create("A", "n", 1).getType());
        assertTrue(AttractionType.GROUP.create("A", "n", 1) instanceof GroupAttraction);
        assertThrows(InvalidInputException.class, () -> AttractionType.parse("VIP"));
        assertThrows(InvalidInputException.class, () -> AttractionType.parse("general"));
    }
}
