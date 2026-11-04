package com.parque.business.model;

import com.parque.business.controller.*;
import com.parque.business.service.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class GroupTicketTest {
    static final BigDecimal P40 = new BigDecimal("40.00");

    @ParameterizedTest
    @CsvSource({"1,0,40.00", "2,0,80.00", "3,10,108.00", "5,10,180.00", "6,20,192.00", "10,20,320.00",
            "11,50,220.00", "20,50,400.00"})
    void discountBrackets(int members, int discount, String total) {
        assertEquals(discount, GroupTicket.discountPercent(members));
        assertEquals(new BigDecimal(total), GroupTicket.totalFor(P40, members));
    }

    @Test
    void roundsToTwoDecimals() {
        assertEquals(new BigDecimal("40.50"), GroupTicket.totalFor(new BigDecimal("15.00"), 3));
        assertEquals(new BigDecimal("30.02"), GroupTicket.totalFor(new BigDecimal("11.12"), 3));
    }

    @Test
    void isATicketWithComposition() {
        User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        Group g = new Group("G01", "Familia");
        g.addMember(ana);
        IndividualTicket c = new IndividualTicket("TG01-1", ana, LocalDate.of(2026, 10, 15), P40);
        Ticket t = new GroupTicket("TG01", g, "G01", LocalDate.of(2026, 10, 15), P40, P40, List.of(c));
        assertEquals("TG01", t.getId());
        assertEquals("40.00", t.getPriceText());
        GroupTicket gt = (GroupTicket) t;
        assertSame(g, gt.getGroup());
        assertTrue(gt.contains(c));
        assertEquals(List.of(ana), t.getVisitors());
        assertThrows(UnsupportedOperationException.class, () -> gt.getConstituents().add(c));
        assertTrue(t.isCurrentOrFuture(LocalDate.of(2026, 10, 15)));
        assertTrue(!t.isFuture(LocalDate.of(2026, 10, 15)));
    }
}
