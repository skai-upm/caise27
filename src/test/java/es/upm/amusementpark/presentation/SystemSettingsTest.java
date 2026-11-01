package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import es.upm.amusementpark.business.controller.SystemController;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemSettingsTest extends ParkTestSupport {

    @Test
    void controllerStartsOnGivenDateWithDefaultPrice() {
        stopPark();
        SystemController fresh = SystemController.initialize(LocalDate.now());
        assertEquals(LocalDate.now(), fresh.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), fresh.getBasePrice());
    }

    @Test
    void setsDateForwardAndBackward() {
        expect("OK: system date set to 2026-09-15", "SYSTEM DATE;2026-09-15");
        assertEquals(LocalDate.of(2026, 9, 15), system.getCurrentDate());
        ok("SYSTEM DATE;2026-10-16");
        ok("SYSTEM DATE;2026-10-01");
        assertEquals(LocalDate.of(2026, 10, 1), system.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"15-09-2026", "2026-02-30", "2026-9-5", "tomorrow", "2026/09/15", "20260915"})
    void rejectsInvalidDatesKeepingPreviousOne(String value) {
        ok("SYSTEM DATE;2026-09-15");
        error("SYSTEM DATE;" + value);
        assertEquals(LocalDate.of(2026, 9, 15), system.getCurrentDate());
    }

    @Test
    void dateCommandRequiresExactlyOneParameter() {
        error("SYSTEM DATE");
        error("SYSTEM DATE;2026-09-15;extra");
        assertEquals(START, system.getCurrentDate());
    }

    @Test
    void setsPrice() {
        expect("OK: base price set to 60.00", "SYSTEM PRICE;60.00");
        assertEquals(new BigDecimal("60.00"), system.getBasePrice());
        ok("SYSTEM PRICE;55.5");
        assertEquals(new BigDecimal("55.50"), system.getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-5", "0", "0.00", "1.2.3", ".5", "5.", "1e2"})
    void rejectsInvalidPrices(String value) {
        error("SYSTEM PRICE;" + value);
        assertEquals(new BigDecimal("40.00"), system.getBasePrice());
    }

    @Test
    void settingsAreNotPersisted() throws IOException {
        ok("SYSTEM DATE;2027-01-01");
        ok("SYSTEM PRICE;60.00");
        restart();
        assertEquals(START, system.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), system.getBasePrice());
    }
}
