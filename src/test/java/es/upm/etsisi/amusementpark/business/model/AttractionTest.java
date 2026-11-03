package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 15);
    static final BigDecimal PRICE = new BigDecimal("40.00");

    @Test
    void individualAndGroupTicketsAreHandledUniformly() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = TestData.user("12345678Z", "ana@correo.com");
        Group g = TestData.groupOf(2);
        List<ParkTicket> tickets = List.of(new Ticket("T001", ana, TODAY, PRICE), new GroupTicket("TG01", g, TODAY, PRICE));
        for (ParkTicket t : tickets) {
            a.enter(t, TODAY);
        }
        assertEquals(3, a.getPresent().size());
        assertEquals(17, a.getAvailable());
        assertTrue(a.isPresent(ana));
        assertTrue(a.isPresent(g.getMembers().get(1)));
    }

    @Test
    void onlyTicketsForTodayAreValid() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = TestData.user("12345678Z", "ana@correo.com");
        Ticket tomorrow = new Ticket("T001", ana, TODAY.plusDays(1), PRICE);
        Ticket yesterday = new Ticket("T002", ana, TODAY.minusDays(1), PRICE);
        assertThrows(IllegalArgumentException.class, () -> a.enter(tomorrow, TODAY));
        assertThrows(IllegalArgumentException.class, () -> a.enter(yesterday, TODAY));
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void groupEntryIsAtomicAndCapacityIsNeverExceeded() {
        Attraction a = new Attraction("A01", "Tornado", 4);
        a.enter(new Ticket("T001", TestData.user("12345678Z", "ana@correo.com"), TODAY, PRICE), TODAY);
        GroupTicket four = new GroupTicket("TG01", TestData.groupOf(4), TODAY, PRICE);
        assertThrows(IllegalArgumentException.class, () -> a.enter(four, TODAY));
        assertEquals(1, a.getPresent().size());
        GroupTicket three = new GroupTicket("TG02", TestData.groupOf(3), TODAY, PRICE);
        a.enter(three, TODAY);
        assertEquals(0, a.getAvailable());
        User other = TestData.user("87654321X", "pedro@correo.com");
        assertThrows(IllegalArgumentException.class, () -> a.enter(new Ticket("T003", other, TODAY, PRICE), TODAY));
    }

    @Test
    void alreadyPresentUserCannotEnterTwice() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = TestData.user("12345678Z", "ana@correo.com");
        Ticket t = new Ticket("T001", ana, TODAY, PRICE);
        a.enter(t, TODAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, TODAY));
        assertEquals(1, a.getPresent().size());
    }

    @Test
    void exitReleasesPlaces() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        GroupTicket g = new GroupTicket("TG01", TestData.groupOf(3), TODAY, PRICE);
        a.enter(g, TODAY);
        assertEquals(3, a.exit(g.getVisitors()));
        assertEquals(20, a.getAvailable());
        assertEquals(0, a.exit(g.getVisitors()));
    }

    @Test
    void historicalTicketHasNoVisitors() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        Ticket history = new Ticket("T001", "12345678Z", TODAY, PRICE);
        assertTrue(history.getVisitors().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> a.enter(history, TODAY));
    }
}
