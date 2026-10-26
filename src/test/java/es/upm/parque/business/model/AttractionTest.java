package es.upm.parque.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 20);
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
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 3);
        a.admit(new IndividualTicket("T001", DAY, ana, PRICE));
        GroupTicket tooBig = groupTicket(pedro, luis, user("Eva", "22222222J"));
        assertThrows(ParkException.class, () -> a.admit(tooBig));
        assertEquals(1, a.getOccupancy());
        assertEquals(2, a.admit(groupTicket(pedro, luis)));
        assertEquals(0, a.getAvailablePlaces());
    }

    @Test
    void alreadyPresentRejectsWholeTicket() {
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 10);
        IndividualTicket t = new IndividualTicket("T001", DAY, ana, PRICE);
        a.admit(t);
        assertThrows(ParkException.class, () -> a.admit(t));
        GroupTicket withAna = groupTicket(pedro, ana);
        assertThrows(ParkException.class, () -> a.admit(withAna));
        assertEquals(List.of(ana), a.getVisitors());
    }

    @Test
    void emptyGroupTicketCannotEnter() {
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 10);
        GroupTicket empty = groupTicket();
        assertThrows(ParkException.class, () -> a.admit(empty));
    }

    private final IndividualTicket single = new IndividualTicket("T001", DAY, ana, PRICE);

    @Test
    void eachTypeAdmitsItsTicketKindsOnly() {
        GroupTicket group = groupTicket(pedro, luis);
        assertTrue(new GeneralAttraction("A", "a", 10).accepts(single));
        assertTrue(new GeneralAttraction("A", "a", 10).accepts(group));
        assertTrue(new IndividualAttraction("A", "a", 10).accepts(single));
        assertFalse(new IndividualAttraction("A", "a", 10).accepts(group));
        assertFalse(new GroupAttraction("A", "a", 10).accepts(single));
        assertTrue(new GroupAttraction("A", "a", 10).accepts(group));
        assertTrue(new SpecialAttraction("A", "a", 10).accepts(single));
        assertFalse(new SpecialAttraction("A", "a", 10).accepts(group));
    }

    @Test
    void rejectedKindLetsNobodyIn() {
        GroupAttraction groupOnly = new GroupAttraction("A02", "Familiar", 10);
        ParkException e = assertThrows(ParkException.class, () -> groupOnly.admit(single));
        assertEquals("Attraction A02 (GROUP) does not admit individual tickets", e.getMessage());
        assertEquals(2, groupOnly.admit(groupTicket(pedro, luis)));
        IndividualAttraction individualOnly = new IndividualAttraction("A03", "Solo", 10);
        GroupTicket group = groupTicket(pedro);
        assertThrows(ParkException.class, () -> individualOnly.admit(group));
        assertEquals(0, individualOnly.getOccupancy());
    }

    @Test
    void typesAreCreatedAndParsed() {
        for (AttractionType type : AttractionType.values()) {
            Attraction<?> a = type.create("A", "a", 5);
            assertEquals(type, a.getType());
            assertEquals(type, AttractionType.parse(type.name().toLowerCase()));
        }
        assertThrows(ParkException.class, () -> AttractionType.parse("VIP"));
    }

    @Test
    void releaseRemovesOnlyPresentUsers() {
        GeneralAttraction a = new GeneralAttraction("A01", "Tornado", 10);
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
