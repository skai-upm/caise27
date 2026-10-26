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

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private static User user(int i) {
        return new User("U" + i, "S", "dni" + i, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private static GroupTicket groupTicket(int members) {
        Group group = new Group("G", "Grupo");
        List<IndividualTicket> parts = new ArrayList<>();
        for (int i = 0; i < members; i++) {
            User u = user(100 + i);
            group.addMember(u);
            parts.add(new IndividualTicket("TG-" + i, DAY, u, PRICE));
        }
        return GroupTicket.purchase("TG", DAY, group, parts);
    }

    @Test
    void individualAndGroupEntryUseTheSameLogic() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        a.enter(new IndividualTicket("T001", DAY, user(1), PRICE));
        a.enter(groupTicket(3));
        assertEquals(4, a.getPresent().size());
        assertEquals(16, a.getAvailable());
    }

    @Test
    void groupEntryIsAtomic() {
        Attraction a = new Attraction("A01", "Tornado", 4);
        a.enter(new IndividualTicket("T001", DAY, user(1), PRICE));
        assertThrows(IllegalArgumentException.class, () -> a.enter(groupTicket(4)));
        assertEquals(1, a.getPresent().size());
        a.enter(groupTicket(3));
        assertEquals(0, a.getAvailable());
        assertThrows(IllegalArgumentException.class,
                () -> a.enter(new IndividualTicket("T002", DAY, user(2), PRICE)));
    }

    @Test
    void userCannotEnterTwiceAndExitFreesPlaces() {
        Attraction a = new Attraction("A01", "Tornado", 5);
        IndividualTicket t = new IndividualTicket("T001", DAY, user(1), PRICE);
        a.enter(t);
        assertThrows(IllegalArgumentException.class, () -> a.enter(t));
        GroupTicket gt = groupTicket(2);
        a.enter(gt);
        assertEquals(2, a.exit(gt));
        assertEquals(4, a.getAvailable());
        assertThrows(IllegalArgumentException.class, () -> a.exit(gt));
        assertTrue(a.getPresent().contains(t.getUser()));
    }
}
