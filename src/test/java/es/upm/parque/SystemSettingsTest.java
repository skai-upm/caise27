package es.upm.parque;

import static es.upm.parque.ParkTestSupport.error;
import static es.upm.parque.ParkTestSupport.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SystemSettingsTest {

    @TempDir
    Path dir;

    @Test
    void startsWithOperatingSystemDateAndDefaultPrice() throws IOException {
        ParkController park = new ParkController(new StoragePaths(dir));
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getTicketPrice());
    }

    @Test
    void setsValidDatesBackwardAndForward() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        ok(park.setSystemDate("2026-10-16"));
        assertEquals(LocalDate.of(2026, 10, 16), park.getCurrentDate());
        ok(park.setSystemDate("2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), park.getCurrentDate());
    }

    @Test
    void rejectsWrongDateFormats() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        for (String text : new String[] {"15/09/2026", "2026-9-15", "2026-02-30", "20260915", "", "2026-09-15x",
                "+2026-09-15"}) {
            error(park.setSystemDate(text));
        }
        assertEquals(ParkTestSupport.START, park.getCurrentDate());
    }

    @Test
    void setsAndValidatesPrice() throws IOException {
        ParkController park = ParkTestSupport.park(dir);
        assertEquals("OK SYSTEM PRICE 60.00", park.setTicketPrice("60.00"));
        ok(park.setTicketPrice("55"));
        assertEquals(new BigDecimal("55.00"), park.getTicketPrice());
        for (String text : new String[] {"-5", "0", "abc", "1.234", ""}) {
            error(park.setTicketPrice(text));
        }
        assertEquals(new BigDecimal("55.00"), park.getTicketPrice());
    }

    @Test
    void dateAndPriceAreNotPersisted() throws IOException {
        ParkController park = new ParkController(new StoragePaths(dir));
        ok(park.setSystemDate("2030-01-01"));
        ok(park.setTicketPrice("60.00"));
        ParkController restarted = new ParkController(new StoragePaths(dir));
        assertEquals(LocalDate.now(), restarted.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), restarted.getTicketPrice());
    }
}
