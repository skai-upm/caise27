package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private static User user(int n) {
        return new User("U" + n, "S", "0000000" + n + "X", "u" + n + "@correo.com", "Clave1!", "600",
            "1234567890123456");
    }

    private static GroupTicket groupTicket(String id, int members) {
        Group group = new Group("G" + id, "Grupo");
        for (int i = 0; i < members; i++) {
            group.addMember(user(10 + i));
        }
        return new GroupTicket(id, DAY, group, PRICE);
    }

    @Test
    void admittedUsersAndValidity() {
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        assertEquals(1, t.getAdmittedUsers().size());
        assertTrue(t.isValidOn(DAY));
        assertFalse(t.isValidOn(DAY.minusDays(1)));
        assertFalse(t.isValidOn(DAY.plusDays(1)));
        t.detachOwner();
        assertTrue(t.getAdmittedUsers().isEmpty());
        assertEquals(3, groupTicket("TG01", 3).getAdmittedUsers().size());
    }

    @Test
    void individualAndGroupEntryAreHandledUniformly() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        List<Admission> admissions = List.of(new IndividualTicket("T001", DAY, PRICE, user(1)), groupTicket("TG01", 2));
        for (Admission admission : admissions) {
            a.enter(admission, DAY);
        }
        assertEquals(3, a.getPresent().size());
        assertEquals(17, a.getAvailable());
    }

    @Test
    void wrongDateIsRejected() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY.minusDays(1)));
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void groupEntryIsAtomic() {
        Attraction a = new Attraction("A01", "Tornado", 4);
        a.enter(new IndividualTicket("T001", DAY, PRICE, user(1)), DAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(groupTicket("TG01", 4), DAY));
        assertEquals(1, a.getPresent().size());
        a.enter(groupTicket("TG02", 3), DAY);
        assertEquals(0, a.getAvailable());
        assertThrows(IllegalArgumentException.class,
            () -> a.enter(new IndividualTicket("T002", DAY, PRICE, user(2)), DAY));
    }

    @Test
    void sameUserCannotEnterTwiceAndExitFreesPlaces() {
        Attraction a = new Attraction("A01", "Tornado", 5);
        User ana = user(1);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, ana);
        a.enter(t, DAY);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY));
        assertEquals(1, a.exit(t));
        assertEquals(5, a.getAvailable());
        assertThrows(IllegalArgumentException.class, () -> a.exit(t));
        a.enter(t, DAY);
        a.remove(ana);
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void ticketWithoutRegisteredUsersIsRejected() {
        Attraction a = new Attraction("A01", "Tornado", 5);
        IndividualTicket t = new IndividualTicket("T001", DAY, PRICE, user(1));
        t.detachOwner();
        assertThrows(IllegalArgumentException.class, () -> a.enter(t, DAY));
    }
}
