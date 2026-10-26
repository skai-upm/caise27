package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.parque.business.ParkException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    private final User ana = user("Ana", "12345678Z");
    private final User pedro = user("Pedro", "87654321X");
    private final User luis = user("Luis", "11111111H");

    private static User user(String name, String dni) {
        return new User(name, "X", dni, name.toLowerCase() + "@correo.com", "Clave1!", "6", "1234567890123456");
    }

    private GroupTicket groupTicket(User... users) {
        List<IndividualTicket> internal = new ArrayList<>();
        for (int i = 0; i < users.length; i++) {
            internal.add(new IndividualTicket("TG01-" + (i + 1), DAY, users[i], PRICE));
        }
        return new GroupTicket("TG01", DAY, new Group("G01", "F"), PRICE, internal);
    }

    @Test
    void individualAndGroupTicketsEnterUniformly() {
        Attraction a = new Attraction("A01", "Tornado", 20);
        assertEquals(1, a.admit(new IndividualTicket("T001", DAY, ana, PRICE)));
        assertEquals(2, a.admit(groupTicket(pedro, luis)));
        assertEquals(List.of(ana, pedro, luis), a.getVisitors());
        assertEquals(3, a.getOccupancy());
        assertEquals(17, a.getAvailablePlaces());
        assertEquals("Tornado", a.getName());
        assertEquals(20, a.getCapacity());
    }

    @Test
    void exactFitAndOverflowAreAtomic() {
        Attraction a = new Attraction("A01", "Tornado", 3);
        a.admit(new IndividualTicket("T001", DAY, ana, PRICE));
        GroupTicket tooBig = groupTicket(pedro, luis, user("Eva", "22222222J"));
        assertThrows(ParkException.class, () -> a.admit(tooBig));
        assertEquals(1, a.getOccupancy());
        assertEquals(2, a.admit(groupTicket(pedro, luis)));
        assertEquals(0, a.getAvailablePlaces());
    }

    @Test
    void alreadyPresentRejectsWholeTicket() {
        Attraction a = new Attraction("A01", "Tornado", 10);
        IndividualTicket t = new IndividualTicket("T001", DAY, ana, PRICE);
        a.admit(t);
        assertThrows(ParkException.class, () -> a.admit(t));
        GroupTicket withAna = groupTicket(pedro, ana);
        assertThrows(ParkException.class, () -> a.admit(withAna));
        assertEquals(List.of(ana), a.getVisitors());
    }

    @Test
    void emptyGroupTicketCannotEnter() {
        Attraction a = new Attraction("A01", "Tornado", 10);
        GroupTicket empty = groupTicket();
        assertThrows(ParkException.class, () -> a.admit(empty));
    }

    @Test
    void releaseRemovesOnlyPresentUsers() {
        Attraction a = new Attraction("A01", "Tornado", 10);
        GroupTicket g = groupTicket(pedro, luis);
        a.admit(g);
        a.admit(new IndividualTicket("T001", DAY, ana, PRICE));
        assertEquals(1, a.release(new IndividualTicket("TG01-1", DAY, pedro, PRICE)));
        assertEquals(1, a.release(g));
        assertEquals(0, a.release(g));
        assertEquals(List.of(ana), a.getVisitors());
        assertEquals(9, a.getAvailablePlaces());
    }
}
