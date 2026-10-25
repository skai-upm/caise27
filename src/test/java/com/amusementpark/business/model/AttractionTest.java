package com.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    private static GroupTicket groupTicket(String id, int members) {
        Group group = new Group("G" + id, "G");
        for (int i = 0; i < members; i++) {
            group.add(TicketTest.user(100 + i));
        }
        return GroupTicket.purchase(id, DAY, BigDecimal.TEN, group);
    }

    @Test
    public void individualAndGroupEntryAndExit() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 20);
        Ticket single = new IndividualTicket("T001", DAY, BigDecimal.TEN, TicketTest.user(1));
        Ticket group = groupTicket("TG01", 2);
        assertNull(attraction.enter(single));
        assertNull(attraction.enter(group));
        assertEquals(3, attraction.getPresent().size());
        assertEquals(17, attraction.getAvailable());
        assertEquals(1, attraction.exit(single));
        assertEquals(18, attraction.getAvailable());
        assertEquals(0, attraction.exit(single));
    }

    @Test
    public void groupEntryIsAtomic() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 4);
        assertNull(attraction.enter(new IndividualTicket("T001", DAY, BigDecimal.TEN, TicketTest.user(1))));
        assertNotNull(attraction.enter(groupTicket("TG01", 4)));
        assertEquals(1, attraction.getPresent().size());
        assertNull(attraction.enter(groupTicket("TG02", 3)));
        assertEquals(0, attraction.getAvailable());
    }

    @Test
    public void typesAdmitTheirTicketKinds() {
        Ticket single = new IndividualTicket("T001", DAY, BigDecimal.TEN, TicketTest.user(1));
        Ticket group = groupTicket("TG01", 2);
        assertNotNull(new GeneralAttraction("A", "A", 5).admit(single));
        assertNotNull(new GeneralAttraction("A", "A", 5).admit(group));
        assertNotNull(new IndividualAttraction("A", "A", 5).admit(single));
        assertNull(new IndividualAttraction("A", "A", 5).admit(group));
        assertNull(new GroupAttraction("A", "A", 5).admit(single));
        assertNotNull(new GroupAttraction("A", "A", 5).admit(group));
        SpecialAttraction special = new SpecialAttraction("A", "A", 5);
        assertNotNull(special.admit(single));
        assertNull(special.admit(group));
        assertTrue(special.requiresSpecialPass());
        assertFalse(special.allowsSpeed());
        assertTrue(new GroupAttraction("A", "A", 5).allowsSpeed());
    }

    @Test
    public void typedEntryUsesTheAdmittedType() {
        GroupAttraction attraction = new GroupAttraction("A", "A", 5);
        GroupTicket admitted = attraction.admit(groupTicket("TG01", 3));
        assertNull(attraction.enter(admitted));
        assertEquals(3, attraction.getPresent().size());
    }

    @Test
    public void factoryCreatesEachType() {
        for (AttractionType type : AttractionType.values()) {
            assertEquals(type, type.create("A", "A", 1).getType());
        }
        assertNull(AttractionType.fromName("OTHER"));
    }

    @Test
    public void userCannotBePresentTwice() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 5);
        Ticket ticket = new IndividualTicket("T001", DAY, BigDecimal.TEN, TicketTest.user(1));
        assertNull(attraction.enter(ticket));
        assertNotNull(attraction.enter(ticket));
        assertEquals(1, attraction.getPresent().size());
    }
}
