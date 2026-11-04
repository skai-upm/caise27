package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    static final BigDecimal PRICE = new BigDecimal("40.00");

    static User user(int i) {
        int n = 20000000 + i;
        return new User("U" + i, "S", n + String.valueOf("TRWAGMYFPDXBNJZSQVHLCKE".charAt(n % 23)),
                "u" + i + "@correo.com", "Clave1!", "1", "1234567890123456");
    }

    static GroupTicket groupTicket(String id, int members, int offset) {
        Group g = new Group("G" + id, "Grupo");
        for (int i = 0; i < members; i++) {
            g.add(user(offset + i));
        }
        return GroupTicket.purchase(id, g, DAY, PRICE);
    }

    @Test
    void individualAndGroupEntryAreUniform() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = user(0);
        Ticket individual = new IndividualTicket("T001", ana, DAY, PRICE);
        Ticket group = groupTicket("TG01", 2, 1);
        for (Ticket t : List.of(individual, group)) {
            a.enter(t, DAY);
        }
        assertEquals(3, a.getPresentCount());
        assertEquals(17, a.getAvailablePlaces());
        assertTrue(a.getPresentUsers().contains(ana));
    }

    @Test
    void wrongDateRejected() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        Ticket t = new IndividualTicket("T001", user(0), DAY, PRICE);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY.plusDays(1)));
        assertEquals(0, a.getPresentCount());
    }

    @Test
    void groupEntryIsAtomicWhenCapacityIsShort() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        a.enter(groupTicket("TG00", 17, 100), DAY);
        assertEquals(3, a.getAvailablePlaces());
        GroupTicket four = groupTicket("TG01", 4, 0);
        assertThrows(IllegalArgumentException.class, () -> a.enter(four, DAY));
        assertEquals(17, a.getPresentCount());
        for (User u : four.getHolders()) {
            assertTrue(!a.getPresentUsers().contains(u));
        }
    }

    @Test
    void exactlyFullThenNoMore() {
        Attraction a = new Attraction("A01", "Tornado", 2);
        a.enter(groupTicket("TG01", 2, 0), DAY);
        assertEquals(0, a.getAvailablePlaces());
        assertThrows(IllegalArgumentException.class,
                () -> a.enter(new IndividualTicket("T001", user(9), DAY, PRICE), DAY));
    }

    @Test
    void alreadyPresentRejectedAndCountedOnce() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        Ticket t = new IndividualTicket("T001", user(0), DAY, PRICE);
        a.enter(t, DAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY));
        assertEquals(1, a.getPresentCount());
    }

    @Test
    void exitFreesPlacesWithoutDateCheck() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        Ticket t = new IndividualTicket("T001", user(0), DAY, PRICE);
        GroupTicket g = groupTicket("TG01", 2, 1);
        a.enter(t, DAY);
        a.enter(g, DAY);
        assertEquals(1, a.exit(t));
        assertEquals(2, a.getPresentCount());
        assertEquals(2, a.exit(g));
        assertEquals(20, a.getAvailablePlaces());
        assertThrows(IllegalArgumentException.class, () -> a.exit(t));
    }

    @Test
    void ticketOfDeletedOwnerCannotEnter() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        Ticket orphan = new IndividualTicket("T001", null, "12345678Z", DAY, PRICE);
        assertThrows(IllegalArgumentException.class, () -> a.enter(orphan, DAY));
    }
}
