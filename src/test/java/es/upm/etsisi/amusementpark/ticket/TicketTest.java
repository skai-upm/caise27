package es.upm.etsisi.amusementpark.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.group.Group;
import es.upm.etsisi.amusementpark.park.ParkFixture;
import es.upm.etsisi.amusementpark.user.User;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);

    @Test
    void hierarchy() {
        assertTrue(Modifier.isAbstract(Ticket.class.getModifiers()));
        assertEquals(Ticket.class, IndividualTicket.class.getSuperclass());
        assertEquals(Ticket.class, GroupTicket.class.getSuperclass());
    }

    @Test
    void dateStates() {
        Ticket t = new IndividualTicket("T", DAY, ParkFixture.ana(), new BigDecimal("40.00"), 0);
        assertTrue(t.isActiveOn(DAY));
        assertTrue(t.isActiveOn(DAY.minusDays(1)));
        assertFalse(t.isActiveOn(DAY.plusDays(1)));
        assertTrue(t.isValidOn(DAY));
        assertFalse(t.isValidOn(DAY.minusDays(1)));
        assertFalse(t.isDeletableOn(DAY));
        assertTrue(t.isDeletableOn(DAY.minusDays(1)));
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,0", "2,0", "3,10", "5,10", "6,20", "10,20", "11,50", "40,50"})
    void discountBrackets(int members, int discount) {
        assertEquals(discount, GroupTicket.discountFor(members));
    }

    @Test
    void groupPriceIsSumOfComposition() {
        List<IndividualTicket> composition = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            composition.add(new IndividualTicket("TG02:" + i, DAY, ParkFixture.user(i), new BigDecimal("40.00"), 20));
        }
        Group group = new Group("G02", "Six");
        GroupTicket ticket = new GroupTicket("TG02", DAY, group, composition);
        assertEquals(new BigDecimal("32.00"), composition.get(0).getPrice());
        assertEquals(new BigDecimal("192.00"), ticket.getPrice());
        assertSame(group, ticket.getGroup());
        assertEquals(6, ticket.getHolders().size());
        composition.clear();
        assertEquals(6, ticket.getIndividualTickets().size());
        assertThrows(UnsupportedOperationException.class, () -> ticket.getIndividualTickets().clear());
        assertEquals("TG01:12345678Z", GroupTicket.constituentId("TG01", "12345678Z"));
    }

    @Test
    void individualHoldsUser() {
        User ana = ParkFixture.ana();
        IndividualTicket t = new IndividualTicket("T", DAY, ana, new BigDecimal("45.50"), 0);
        assertEquals(List.of(ana), t.getHolders());
        assertEquals(new BigDecimal("45.50"), t.getPrice());
    }

    @Test
    void usersAndGroupsDoNotReferenceTickets() {
        for (Class<?> type : List.of(User.class, Group.class)) {
            for (var field : type.getDeclaredFields()) {
                assertFalse(field.getType().getName().contains("ticket"), type + "." + field.getName());
                assertFalse(field.getGenericType().getTypeName().contains("Ticket"), type + "." + field.getName());
                assertFalse(field.getGenericType().getTypeName().contains("Attraction"), type + "." + field.getName());
            }
        }
    }
}
