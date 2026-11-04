package com.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {
    static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    static final BigDecimal P40 = new BigDecimal("40.00");

    private static User user(int i) {
        return new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "600", "1234567890123456");
    }

    private static GroupTicket groupTicket(String id, int members, LocalDate date) {
        List<IndividualTicket> c = new ArrayList<>();
        for (int i = 1; i <= members; i++) {
            c.add(new IndividualTicket(id + "-" + i, user(100 + i), date, P40));
        }
        return new GroupTicket(id, null, "G", date, P40, GroupTicket.totalFor(P40, members), c);
    }

    @Test
    void individualAndGroupEntryThroughSameAbstraction() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = user(1);
        Ticket t001 = new IndividualTicket("T001", ana, TODAY, P40);
        Ticket tg01 = groupTicket("TG01", 3, TODAY);
        a.enter(t001, TODAY);
        a.enter(tg01, TODAY);
        assertEquals(4, a.getPresent().size());
        assertEquals(16, a.available());
        assertTrue(a.isPresent(ana));
        assertEquals(1, a.exit(t001));
        assertEquals(17, a.available());
        assertEquals(3, a.exit(tg01));
        assertEquals(20, a.available());
    }

    @Test
    void groupLargerThanRemainingPlacesEntersNobody() {
        Attraction a = new Attraction("A01", "Tornado", 5);
        a.enter(groupTicket("TG00", 2, TODAY), TODAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(groupTicket("TG01", 4, TODAY), TODAY));
        assertEquals(2, a.getPresent().size());
        a.enter(groupTicket("TG02", 3, TODAY), TODAY);
        assertEquals(0, a.available(), "exactly full is allowed");
        assertThrows(IllegalArgumentException.class, () -> a.enter(new IndividualTicket("T9", user(9), TODAY, P40), TODAY));
    }

    @Test
    void ticketMustBeForToday() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        Ticket t = new IndividualTicket("T001", user(1), TODAY, P40);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, TODAY.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, TODAY.plusDays(1)));
        assertEquals(0, a.getPresent().size());
    }

    @Test
    void alreadyInsideRejectedAndExitNeedsSomebody() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = user(1);
        Ticket t = new IndividualTicket("T001", ana, TODAY, P40);
        a.enter(t, TODAY);
        Ticket other = new IndividualTicket("T002", ana, TODAY, P40);
        assertThrows(IllegalArgumentException.class, () -> a.enter(other, TODAY));
        assertEquals(1, a.getPresent().size());
        a.exit(t);
        assertThrows(IllegalArgumentException.class, () -> a.exit(t));
        a.enter(other, TODAY);
        assertEquals(1, a.getPresent().size());
    }

    @Test
    void historyTicketWithoutVisitorsRejected() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        assertThrows(IllegalArgumentException.class,
                () -> a.enter(new IndividualTicket("T001", null, "x", TODAY, P40), TODAY));
    }
}
