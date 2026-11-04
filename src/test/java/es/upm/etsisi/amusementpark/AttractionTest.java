package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private static User user(int n) {
        return new User("U" + n, "S", "0000000" + n + "X", "u" + n + "@correo.com", "Clave1!", "1", "1234567890123456");
    }

    private static GroupTicket groupTicket(String id, List<User> members) {
        Group g = new Group("G-" + id, "Grupo");
        members.forEach(g::addMember);
        return new GroupTicket(id, DAY, g, PRICE);
    }

    private static List<User> users(int from, int count) {
        List<User> list = new ArrayList<>();
        for (int i = from; i < from + count; i++) {
            list.add(user(i));
        }
        return list;
    }

    @Test
    void individualAndGroupEntryAreHandledUniformly() throws Exception {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = user(1);
        a.enter(new IndividualTicket("T001", DAY, ana, PRICE), DAY);
        a.enter(groupTicket("TG01", users(2, 3)), DAY);
        assertEquals(4, a.getPresent().size());
        assertEquals(16, a.getAvailable());
        assertTrue(a.isPresent(ana));
    }

    @Test
    void ticketMustBeForToday() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), PRICE);
        assertThrows(UserException.class, () -> a.enter(t, DAY.minusDays(1)));
        assertThrows(UserException.class, () -> a.enter(t, DAY.plusDays(1)));
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void groupEntryIsAtomicAtTheCapacityBoundary() throws Exception {
        Attraction a = new Attraction("A01", "Tornado", 20);
        a.enter(groupTicket("TG00", users(10, 17)), DAY);
        assertEquals(3, a.getAvailable());

        assertThrows(UserException.class, () -> a.enter(groupTicket("TG01", users(40, 4)), DAY));
        assertEquals(3, a.getAvailable());

        a.enter(groupTicket("TG02", users(50, 3)), DAY);
        assertEquals(0, a.getAvailable());
        assertThrows(UserException.class, () -> a.enter(new IndividualTicket("T9", DAY, user(9), PRICE), DAY));
        assertEquals(20, a.getPresent().size());
    }

    @Test
    void alreadyPresentUserIsRejectedAndCountedOnce() throws Exception {
        Attraction a = new Attraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), PRICE);
        a.enter(t, DAY);
        assertThrows(UserException.class, () -> a.enter(t, DAY));
        assertEquals(1, a.getPresent().size());
    }

    @Test
    void ticketWithoutUsersCannotEnter() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), PRICE);
        t.detachUser();
        assertThrows(UserException.class, () -> a.enter(t, DAY));
    }

    @Test
    void exitFreesPlacesWithoutDateCheck() throws Exception {
        Attraction a = new Attraction("A01", "Tornado", 20);
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), PRICE);
        GroupTicket g = groupTicket("TG01", users(2, 3));
        a.enter(t, DAY);
        a.enter(g, DAY);
        a.exit(t);
        assertEquals(17, a.getAvailable());
        a.exit(g);
        assertEquals(20, a.getAvailable());
        assertThrows(UserException.class, () -> a.exit(t));
    }

    @Test
    void removeDropsADeletedUser() throws Exception {
        Attraction a = new Attraction("A01", "Tornado", 20);
        User ana = user(1);
        a.enter(new IndividualTicket("T001", DAY, ana, PRICE), DAY);
        a.remove(ana);
        assertFalse(a.isPresent(ana));
    }
}
