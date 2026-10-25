package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

public class SystemCommandsTest extends ParkTestSupport {

    @Test
    public void startsWithOperatingSystemDateAndDefaultPrice() {
        assertEquals(LocalDate.now(ZoneId.systemDefault()), park().getCurrentDate());
        assertEquals("40.00", park().getTicketPrice().toPlainString());
    }

    @Test
    public void setsValidDateBackwardAndForward() {
        assertEquals("OK SYSTEM DATE -> 2026-09-15", run("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), park().getCurrentDate());
        ok("SYSTEM DATE;2020-01-01");
        assertEquals(LocalDate.of(2020, 1, 1), park().getCurrentDate());
    }

    @Test
    public void rejectsInvalidDates() {
        ok("SYSTEM DATE;2026-09-15");
        fails("SYSTEM DATE;15/09/2026");
        fails("SYSTEM DATE;2026-9-15");
        fails("SYSTEM DATE;2026-02-30");
        fails("SYSTEM DATE;");
        fails("SYSTEM DATE");
        assertEquals(LocalDate.of(2026, 9, 15), park().getCurrentDate());
    }

    @Test
    public void dateIsNotPersisted() {
        ok("SYSTEM DATE;2000-01-01");
        restart();
        assertEquals(LocalDate.now(ZoneId.systemDefault()), park().getCurrentDate());
    }

    @Test
    public void setsPriceUsedByNewTickets() {
        createAna();
        ok("SYSTEM DATE;2026-10-01");
        assertEquals("OK SYSTEM PRICE -> 60.00", run("SYSTEM PRICE;60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", run("TICKET CREATE;T001;" + ANA + ";2026-10-15"));
        assertEquals("OK SYSTEM PRICE -> 55.50", run("SYSTEM PRICE;55.5"));
    }

    @Test
    public void rejectsInvalidPrices() {
        fails("SYSTEM PRICE;-5");
        fails("SYSTEM PRICE;0");
        fails("SYSTEM PRICE;abc");
        fails("SYSTEM PRICE;10.123");
        assertEquals("40.00", park().getTicketPrice().toPlainString());
    }

    @Test
    public void priceIsNotPersisted() {
        ok("SYSTEM PRICE;60.00");
        restart();
        assertEquals("40.00", park().getTicketPrice().toPlainString());
    }
}
