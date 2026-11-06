package es.upm.park.business.model;

import es.upm.park.business.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttractionEntryTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    private final IndividualTicket ticket = new IndividualTicket("T001", "ana@correo.com", DAY, new BigDecimal("40.00"));

    private GroupTicket groupTicket() {
        Map<String, String> members = new LinkedHashMap<>();
        members.put("pedro@correo.com", "P");
        members.put("luis@correo.com", "L");
        return GroupTicket.purchase("TG01", new Group("G01", "g"), DAY, new BigDecimal("40.00"), members);
    }

    @Test
    void individualSpeedEntryConsumesExactlyOnePass() {
        TestEntryRules rules = new TestEntryRules().speedPass("T001", "SP01:1").speedPass("T001", "SP02:1");
        EntryResult result = new GeneralAttraction("A01", "x", 20).admit(ticket, DAY, rules, true);
        assertEquals(List.of("ana@correo.com"), result.entrants());
        assertEquals(List.of("SP01:1"), result.consumedPasses());
        assertEquals(List.of("SP01:1"), rules.consumed);
    }

    @Test
    void speedEntryWithoutAvailablePassFails() {
        TestEntryRules rules = new TestEntryRules();
        Attraction<?> attraction = new IndividualAttraction("A02", "x", 20);
        assertThrows(BusinessRuleException.class, () -> attraction.admit(ticket, DAY, rules, true));
        assertTrue(attraction.getOccupantEmails().isEmpty());
    }

    @Test
    void failedEntryKeepsSpeedPasses() {
        TestEntryRules rules = new TestEntryRules().speedPass("T001", "SP01:1");
        Attraction<?> full = new GeneralAttraction("A01", "x", 1);
        full.enter("otro@correo.com");
        assertThrows(BusinessRuleException.class, () -> full.admit(ticket, DAY, rules, true));
        assertTrue(rules.consumed.isEmpty());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        GroupTicket group = groupTicket();
        TestEntryRules missing = new TestEntryRules().speedPass("TG01:P", "SP05:1");
        Attraction<?> attraction = new GroupAttraction("A03", "x", 20);
        assertThrows(BusinessRuleException.class, () -> attraction.admit(group, DAY, missing, true));
        assertTrue(attraction.getOccupantEmails().isEmpty());
        assertTrue(missing.consumed.isEmpty());

        TestEntryRules complete = new TestEntryRules().speedPass("TG01:P", "SP05:1").speedPass("TG01:L", "SP06:1");
        EntryResult result = attraction.admit(group, DAY, complete, true);
        assertEquals(2, result.entrants().size());
        assertEquals(List.of("SP05:1", "SP06:1"), complete.consumed);
    }

    @Test
    void specialEntryConsumesSpecialPassAndRejectsSpeed() {
        TestEntryRules rules = new TestEntryRules().specialPass("T001", "PE01").speedPass("T001", "SP01:1");
        Attraction<?> special = new SpecialAttraction("A04", "x", 20);
        assertThrows(BusinessRuleException.class, () -> special.admit(ticket, DAY, rules, true));
        assertTrue(rules.consumed.isEmpty());
        assertEquals(List.of("PE01"), special.admit(ticket, DAY, rules, false).consumedPasses());
        special.leave("ana@correo.com");
        assertThrows(BusinessRuleException.class, () -> special.admit(ticket, DAY, rules, false), "pass consumed");
    }

    @Test
    void failedSpecialEntryKeepsPass() {
        TestEntryRules rules = new TestEntryRules().specialPass("T001", "PE01");
        Attraction<?> full = new SpecialAttraction("A04", "x", 1);
        full.enter("otro@correo.com");
        assertThrows(BusinessRuleException.class, () -> full.admit(ticket, DAY, rules, false));
        assertTrue(rules.consumed.isEmpty());
    }
}
