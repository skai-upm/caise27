package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.TestContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SystemSettingsTest {

    @TempDir
    Path dir;

    @Test
    void startsWithOperatingSystemDateAndDefaultPrice() throws IOException {
        TestContext ctx = TestContext.start(dir, LocalDate.now());
        assertEquals(LocalDate.now(), ctx.park().getCurrentDate());
        assertEquals(new BigDecimal("40.00"), ctx.park().getTicketPrice());
    }

    @Test
    void setsValidDatesBackwardAndForward() throws IOException {
        TestContext ctx = TestContext.start(dir);
        ok(ctx.park().setSystemDate("2026-10-16"));
        assertEquals(LocalDate.of(2026, 10, 16), ctx.park().getCurrentDate());
        ok(ctx.park().setSystemDate("2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), ctx.park().getCurrentDate());
    }

    @Test
    void rejectsWrongDateFormats() throws IOException {
        TestContext ctx = TestContext.start(dir);
        for (String text : new String[] {"15/09/2026", "2026-9-15", "2026-02-30", "20260915", "", "2026-09-15x",
                "+2026-09-15"}) {
            error(ctx.park().setSystemDate(text));
        }
        assertEquals(TestContext.START, ctx.park().getCurrentDate());
    }

    @Test
    void setsAndValidatesPrice() throws IOException {
        TestContext ctx = TestContext.start(dir);
        assertEquals("OK SYSTEM PRICE 60.00", ctx.park().setTicketPrice("60.00"));
        ok(ctx.park().setTicketPrice("55"));
        assertEquals(new BigDecimal("55.00"), ctx.park().getTicketPrice());
        for (String text : new String[] {"-5", "0", "abc", "1.234", ""}) {
            error(ctx.park().setTicketPrice(text));
        }
        assertEquals(new BigDecimal("55.00"), ctx.park().getTicketPrice());
    }

    @Test
    void dateAndPriceAreNotPersisted() throws IOException {
        TestContext ctx = TestContext.start(dir, LocalDate.now());
        ok(ctx.park().setSystemDate("2030-01-01"));
        ok(ctx.park().setTicketPrice("60.00"));
        TestContext restarted = TestContext.start(dir, LocalDate.now());
        assertEquals(LocalDate.now(), restarted.park().getCurrentDate());
        assertEquals(new BigDecimal("40.00"), restarted.park().getTicketPrice());
    }
}
