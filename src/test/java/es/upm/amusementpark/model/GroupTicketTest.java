package es.upm.amusementpark.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GroupTicketTest {

    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    private static List<User> users(int n) {
        List<User> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int number = 10000000 + i;
            String dni = number + "" + LETTERS.charAt(number % 23);
            list.add(new User("U" + i, "S", dni, "u" + i + "@correo.com", "Clave1!", "600", "1234567890123456"));
        }
        return list;
    }

    private static BigDecimal total(int members, String base) {
        return new GroupTicket("TG", LocalDate.of(2027, 4, 15), new BigDecimal(base), new Group("G", "g"),
                users(members)).getPrice();
    }

    @Test
    void discountBrackets() {
        assertEquals(new BigDecimal("80.00"), total(2, "40.00"));
        assertEquals(new BigDecimal("108.00"), total(3, "40.00"));
        assertEquals(new BigDecimal("180.00"), total(5, "40.00"));
        assertEquals(new BigDecimal("192.00"), total(6, "40.00"));
        assertEquals(new BigDecimal("320.00"), total(10, "40.00"));
        assertEquals(new BigDecimal("220.00"), total(11, "40.00"));
    }

    @Test
    void componentsCarryDiscountedPriceAndDerivedIds() {
        List<User> members = users(6);
        GroupTicket t = new GroupTicket("TG01", LocalDate.of(2027, 4, 15), new BigDecimal("40.00"),
                new Group("G01", "g"), members);
        assertEquals(6, t.getComposition().size());
        IndividualTicket first = t.getComposition().get(0);
        assertEquals("TG01:" + members.get(0).getDni(), first.getId());
        assertSame(members.get(0), first.getUser());
        assertEquals(new BigDecimal("32.00"), first.getPrice());
        assertTrue(t.usesId("TG01"));
        assertTrue(t.usesId(first.getId()));
        assertFalse(t.usesId("TG02"));
        assertTrue(t.involves(members.get(5)));
        assertFalse(t.involves(users(7).get(6)));
    }

    @Test
    void roundsEachComponentToCents() {
        assertEquals(new BigDecimal("123.00"), total(3, "45.55"));
    }

    @Test
    void groupReferenceCanBeCleared() {
        Group g = new Group("G01", "g");
        GroupTicket t = new GroupTicket("TG01", LocalDate.of(2027, 4, 15), new BigDecimal("40.00"), g, users(1));
        assertSame(g, t.getGroup());
        t.clearGroup();
        assertNull(t.getGroup());
        assertEquals(1, t.getComposition().size());
    }
}
