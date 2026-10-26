package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        Attraction attraction = new Attraction("A01", "Tornado", 20);
        IndividualTicket single = new IndividualTicket("T001", TODAY, user(0), PRICE);
        GroupTicket group = groupTicket("TG01", 2, 1);
        assertEquals(1, attraction.enter(single, TODAY));
        assertEquals(2, attraction.enter(group, TODAY));
        assertEquals(3, attraction.getVisitors().size());
        assertEquals(17, attraction.available());
        assertEquals(1, attraction.exit(single));
        assertEquals(18, attraction.available());
        assertThrows(UserException.class, () -> attraction.exit(single));
    }

    @Test
    void ticketMustBeForToday() {
        Attraction attraction = new Attraction("A01", "Tornado", 20);
        IndividualTicket ticket = new IndividualTicket("T001", TODAY, user(0), PRICE);
        assertThrows(UserException.class, () -> attraction.enter(ticket, TODAY.minusDays(1)));
        assertThrows(UserException.class, () -> attraction.enter(ticket, TODAY.plusDays(1)));
        assertTrue(attraction.getVisitors().isEmpty());
    }

    @Test
    void groupEntryIsAtomic() {
        Attraction attraction = new Attraction("A01", "Tornado", 4);
        attraction.enter(new IndividualTicket("T001", TODAY, user(9), PRICE), TODAY);
        assertThrows(UserException.class, () -> attraction.enter(groupTicket("TG01", 4, 0), TODAY));
        assertEquals(1, attraction.getVisitors().size());
        assertEquals(3, attraction.enter(groupTicket("TG02", 3, 0), TODAY));
        assertEquals(0, attraction.available());
    }

    @Test
    void sameUserCannotEnterTwice() {
        Attraction attraction = new Attraction("A01", "Tornado", 4);
        IndividualTicket ticket = new IndividualTicket("T001", TODAY, user(0), PRICE);
        attraction.enter(ticket, TODAY);
        assertThrows(UserException.class, () -> attraction.enter(ticket, TODAY));
        assertEquals(1, attraction.getVisitors().size());
    }
}
