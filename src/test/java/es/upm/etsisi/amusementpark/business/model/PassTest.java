package es.upm.etsisi.amusementpark.business.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestData;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PassTest {

    static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @Test
    void pricesComeFromTheStoredTicketPrice() {
        Ticket t = new Ticket("T001", TestData.user("12345678Z", "ana@correo.com"), DAY, new BigDecimal("40.00"));
        Pass speed = new SpeedPass("S1-1", t);
        Pass special = new SpecialPass("SP1", t);
        assertEquals("8.00", speed.getPrice().toPlainString());
        assertEquals("40.00", special.getPrice().toPlainString());
        assertSame(t, speed.getTicket());
        assertEquals("SPEED", speed.getType());
        assertEquals("SPECIAL", special.getType());
    }

    @Test
    void constituentTicketsStoreTheDiscountedShare() {
        GroupTicket g = new GroupTicket("TG01", TestData.groupOf(3), DAY, new BigDecimal("40.00"));
        Ticket member = g.getTickets().get(0);
        assertEquals("36.00", member.getPrice().toPlainString());
        assertEquals("108.00", g.getPrice().toPlainString());
        assertEquals("7.20", new SpeedPass("S1-1", member).getPrice().toPlainString());
        assertEquals("36.00", new SpecialPass("SP1", member).getPrice().toPlainString());
        assertEquals("33.33", GroupTicket.memberShare(new BigDecimal("33.33"), 2).toPlainString());
        assertEquals("30.00", GroupTicket.memberShare(new BigDecimal("33.33"), 3).toPlainString());
    }

    @Test
    void consumptionIsRecorded() {
        Ticket t = new Ticket("T001", TestData.user("12345678Z", "ana@correo.com"), DAY, new BigDecimal("40.00"));
        Pass p = new SpeedPass("S1-1", t, new BigDecimal("8"), false);
        assertFalse(p.isConsumed());
        p.markConsumed();
        assertTrue(p.isConsumed());
        assertEquals("8.00", p.getPrice().toPlainString());
    }
}
