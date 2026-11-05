package es.upm.park.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal P40 = new BigDecimal("40.00");

    private static User user(int i) {
        String dni = String.format("%08d", i) + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(i % 23);
        return new User("U" + i, "S", dni, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private static GroupTicket groupTicket(String id, int members, LocalDate date) {
        Group g = new Group("G" + id, "G");
        for (int i = 0; i < members; i++) {
            g.addMember(user(100 + i));
        }
        return GroupTicket.purchase(id, date, g, P40);
    }

    @Test
    void individualAndGroupEntryUseSameOperation() {
        Attraction a = new GeneralAttraction("A01", "Tornado", 20);
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new IndividualTicket("T001", DAY, P40, user(1)));
        tickets.add(groupTicket("TG01", 2, DAY));
        for (Ticket t : tickets) {
            assertTrue(a.enter(t, DAY).success());
        }
        assertEquals(3, a.getPresent().size());
        assertEquals(17, a.getAvailable());
    }

    @Test
    void ticketMustBeForTheSystemDate() {
        Attraction a = new GeneralAttraction("A01", "Tornado", 20);
        assertFalse(a.enter(new IndividualTicket("T002", DAY.plusDays(1), P40, user(1)), DAY).success());
        assertFalse(a.enter(new IndividualTicket("T003", DAY.minusDays(1), P40, user(1)), DAY).success());
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void groupEntryIsAllOrNothing() {
        Attraction a = new GeneralAttraction("A01", "Tornado", 4);
        a.enter(new IndividualTicket("T001", DAY, P40, user(1)), DAY);
        assertEquals(3, a.getAvailable());
        assertFalse(a.enter(groupTicket("TG01", 4, DAY), DAY).success());
        assertEquals(1, a.getPresent().size());
        assertTrue(a.enter(groupTicket("TG02", 3, DAY), DAY).success(), "exact fit");
        assertEquals(0, a.getAvailable());
        assertFalse(a.enter(new IndividualTicket("T009", DAY, P40, user(9)), DAY).success(), "full");
    }

    @Test
    void userAlreadyPresentIsRejected() {
        Attraction a = new GeneralAttraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, P40, user(1));
        a.enter(t, DAY);
        assertFalse(a.enter(t, DAY).success());
        assertEquals(1, a.getPresent().size());
    }

    @Test
    void exitReleasesPlacesWithoutDateCheck() {
        Attraction a = new GeneralAttraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, P40, user(1));
        GroupTicket g = groupTicket("TG01", 2, DAY);
        a.enter(t, DAY);
        a.enter(g, DAY);
        assertTrue(a.exit(t).success());
        assertEquals(18, a.getAvailable());
        assertEquals(2, a.getPresent().size());
        assertFalse(a.exit(t).success(), "nobody of the ticket present");
        assertTrue(a.exit(g).success());
        assertEquals(20, a.getAvailable());
    }
}
