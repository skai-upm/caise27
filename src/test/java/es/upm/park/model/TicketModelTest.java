package es.upm.park.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketModelTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    private static User user(int i) {
        return new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private static GroupTicket groupTicket(int members) {
        GroupDiscount discount = GroupDiscount.forMembers(members);
        List<IndividualTicket> composition = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            composition.add(new IndividualTicket("TG:" + i, DAY, user(i), FORTY, discount));
        }
        return new GroupTicket("TG", DAY, new Group("G", "Grupo"), composition);
    }

    @ParameterizedTest
    @CsvSource({"1,NONE", "2,NONE", "3,SMALL_GROUP", "5,SMALL_GROUP", "6,MEDIUM_GROUP", "10,MEDIUM_GROUP",
            "11,LARGE_GROUP", "40,LARGE_GROUP"})
    void discountBrackets(int members, GroupDiscount expected) {
        assertEquals(expected, GroupDiscount.forMembers(members));
    }

    @Test
    void discountFromPercent() {
        assertEquals(GroupDiscount.MEDIUM_GROUP, GroupDiscount.fromPercent(20));
        assertThrows(IllegalArgumentException.class, () -> GroupDiscount.fromPercent(15));
    }

    @Test
    void individualTicketUsesBasePrice() {
        IndividualTicket ticket = new IndividualTicket("T001", DAY, user(1), FORTY);
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertEquals(List.of(ticket.getUser()), ticket.getHolders());
    }

    @Test
    void sixMemberGroupTicketCosts192() {
        GroupTicket ticket = groupTicket(6);
        assertEquals(new BigDecimal("32.00"), ticket.getComposition().get(0).getPrice());
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertEquals(6, ticket.getHolders().size());
    }

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "11,220.00"})
    void groupTotals(int members, String total) {
        assertEquals(new BigDecimal(total), groupTicket(members).getPrice());
    }

    @Test
    void discountRoundsToCents() {
        IndividualTicket ticket = new IndividualTicket("T", DAY, user(1), new BigDecimal("45.55"),
                GroupDiscount.SMALL_GROUP);
        assertEquals(new BigDecimal("41.00"), ticket.getPrice());
    }

    @Test
    void dateRules() {
        Ticket ticket = new IndividualTicket("T", DAY, user(1), FORTY);
        assertTrue(ticket.isActiveOn(DAY));
        assertTrue(ticket.isActiveOn(DAY.minusDays(1)));
        assertFalse(ticket.isActiveOn(DAY.plusDays(1)));

        assertFalse(ticket.isDeletableOn(DAY));
        assertTrue(ticket.isDeletableOn(DAY.minusDays(1)));

        assertTrue(ticket.isValidOn(DAY));
        assertFalse(ticket.isValidOn(DAY.minusDays(1)));
    }

    @Test
    void compositionIsFixedAndGroupCanBeDetached() {
        GroupTicket ticket = groupTicket(3);
        assertThrows(UnsupportedOperationException.class, () -> ticket.getComposition().clear());
        ticket.detachGroup();
        assertNull(ticket.getGroup());
        assertEquals(3, ticket.getComposition().size());
    }

    @Test
    void involvesHolders() {
        GroupTicket ticket = groupTicket(2);
        assertTrue(ticket.involves(ticket.getComposition().get(1).getUser()));
        assertFalse(ticket.involves(user(99)));
    }

    @Test
    void attractionEntryIsAllOrNothing() {
        Attraction attraction = new Attraction("A", "Noria", 2);
        assertFalse(attraction.enter(List.of(user(1), user(2), user(3))));
        assertEquals(0, attraction.getOccupancy());
        assertTrue(attraction.enter(List.of(user(1), user(2))));
        assertEquals(0, attraction.getAvailablePlaces());
    }

    @Test
    void groupRejectsDuplicateMember() {
        Group group = new Group("G", "Grupo");
        User user = user(1);
        assertTrue(group.add(user));
        assertFalse(group.add(user));
        assertEquals(1, group.size());
    }
}
