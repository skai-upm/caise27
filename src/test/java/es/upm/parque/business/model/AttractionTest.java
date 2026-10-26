package es.upm.parque.business.model;

import es.upm.parque.business.BusinessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private static User user(int n) {
        return new User("U" + n, "S" + n, "dni" + n, "u" + n + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private static GroupTicket groupTicket(String id, int members, int firstUser) {
        List<IndividualTicket> tickets = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            tickets.add(new IndividualTicket(id + "-" + (i + 1), TODAY, user(firstUser + i), PRICE));
        }
        return new GroupTicket(id, TODAY, new Group("G", "G"), tickets);
    }

    @Test
    void holdersArePolymorphic() {
        User ana = user(0);
        assertEquals(List.of(ana), new IndividualTicket("T001", TODAY, ana, PRICE).getHolders());
        assertEquals(3, groupTicket("TG01", 3, 0).getHolders().size());
    }

    @Test
    void individualAndGroupEntryAndExit() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 20);
        IndividualTicket single = new IndividualTicket("T001", TODAY, user(0), PRICE);
        GroupTicket group = groupTicket("TG01", 2, 1);
        assertEquals(1, attraction.enter(single, TODAY));
        assertEquals(2, attraction.enter(group, TODAY));
        assertEquals(3, attraction.getVisitors().size());
        assertEquals(17, attraction.available());
        assertEquals(1, attraction.exit(single));
        assertEquals(18, attraction.available());
        assertThrows(BusinessException.class, () -> attraction.exit(single));
    }

    @Test
    void ticketMustBeForToday() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 20);
        IndividualTicket ticket = new IndividualTicket("T001", TODAY, user(0), PRICE);
        assertThrows(BusinessException.class, () -> attraction.enter(ticket, TODAY.minusDays(1)));
        assertThrows(BusinessException.class, () -> attraction.enter(ticket, TODAY.plusDays(1)));
        assertTrue(attraction.getVisitors().isEmpty());
    }

    @Test
    void groupEntryIsAtomic() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 4);
        attraction.enter(new IndividualTicket("T001", TODAY, user(9), PRICE), TODAY);
        assertThrows(BusinessException.class, () -> attraction.enter(groupTicket("TG01", 4, 0), TODAY));
        assertEquals(1, attraction.getVisitors().size());
        assertEquals(3, attraction.enter(groupTicket("TG02", 3, 0), TODAY));
        assertEquals(0, attraction.available());
    }

    @Test
    void typesAdmitTheirCapabilitiesOnly() {
        IndividualTicket single = new IndividualTicket("T001", TODAY, user(0), PRICE);
        GroupTicket group = groupTicket("TG01", 2, 1);
        assertEquals(1, new GeneralAttraction("A", "A", 9).enterWith(single, TODAY));
        assertEquals(2, new GeneralAttraction("A", "A", 9).enterWith(group, TODAY));
        assertEquals(1, new IndividualAttraction("A", "A", 9).enterWith(single, TODAY));
        assertThrows(BusinessException.class, () -> new IndividualAttraction("A", "A", 9).enterWith(group, TODAY));
        assertEquals(2, new GroupAttraction("A", "A", 9).enterWith(group, TODAY));
        assertThrows(BusinessException.class, () -> new GroupAttraction("A", "A", 9).enterWith(single, TODAY));
        SpecialAttraction special = new SpecialAttraction("A", "A", 9);
        IndividualAccess admitted = special.admit(single);
        assertSame(single, admitted);
        assertThrows(BusinessException.class, () -> special.admit(group));
        assertEquals(AttractionType.SPECIAL, special.getType());
    }

    @Test
    void passHooksAndIndividualTickets() {
        assertTrue(new GeneralAttraction("A", "A", 1).acceptsSpeed());
        assertFalse(new GroupAttraction("A", "A", 1).requiresSpecialPass());
        assertFalse(new SpecialAttraction("A", "A", 1).acceptsSpeed());
        assertTrue(new SpecialAttraction("A", "A", 1).requiresSpecialPass());
        IndividualTicket single = new IndividualTicket("T001", TODAY, user(0), PRICE);
        assertEquals(List.of(single), single.getIndividualTickets());
        GroupTicket group = groupTicket("TG01", 3, 1);
        assertEquals(group.getTickets(), group.getIndividualTickets());
    }

    @Test
    void factoryCreatesEachType() {
        for (AttractionType type : AttractionType.values()) {
            assertEquals(type, type.create("A", "A", 1).getType());
        }
        assertNull(AttractionType.fromName("VIP"));
    }

    @Test
    void sameUserCannotEnterTwice() {
        GeneralAttraction attraction = new GeneralAttraction("A01", "Tornado", 4);
        IndividualTicket ticket = new IndividualTicket("T001", TODAY, user(0), PRICE);
        attraction.enter(ticket, TODAY);
        assertThrows(BusinessException.class, () -> attraction.enter(ticket, TODAY));
        assertEquals(1, attraction.getVisitors().size());
    }
}
