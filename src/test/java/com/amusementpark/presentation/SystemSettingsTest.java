package com.amusementpark.presentation;

import com.amusementpark.business.controller.ParkController;
import com.amusementpark.persistence.StoragePaths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemSettingsTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
    }

    @Test
    void startsWithOperatingSystemDateAndDefaultPrice() throws IOException {
        ParkController park = new ParkController(StoragePaths.in(dir));
        assertEquals(LocalDate.now(), park.getSystemDate());
        assertEquals("40.00", park.getBasePrice().toPlainString());
    }

    @Test
    void setsDateForwardAndBackward() {
        assertEquals("OK: System date set to 2026-09-15", f.run("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), f.park.getSystemDate());
        f.ok("SYSTEM DATE;2026-10-20", "SYSTEM DATE;2026-10-01");
        assertEquals(LocalDate.of(2026, 10, 1), f.park.getSystemDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13-01", "2026-02-30", "15/09/2026", "2026-9-15", "", "tomorrow"})
    void rejectsInvalidDateKeepingPrevious(String value) {
        f.ok("SYSTEM DATE;2026-09-15");
        assertTrue(f.run("SYSTEM DATE;" + value).startsWith("ERROR:"));
        assertEquals(LocalDate.of(2026, 9, 15), f.park.getSystemDate());
    }

    @Test
    void changesPrice() {
        assertEquals("OK: Base price set to 60.00", f.run("SYSTEM PRICE;60.00"));
        assertEquals("OK: Base price set to 55.50", f.run("SYSTEM PRICE;55.5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-5", "0", "10.555", ""})
    void rejectsInvalidPriceKeepingPrevious(String value) {
        assertTrue(f.run("SYSTEM PRICE;" + value).startsWith("ERROR:"));
        assertEquals("40.00", f.park.getBasePrice().toPlainString());
    }

    @Test
    void dateAndPriceResetOnRestart() throws IOException {
        f.ok("SYSTEM DATE;2027-01-01", "SYSTEM PRICE;60.00");
        f.restart();
        assertEquals(ParkFixture.START, f.park.getSystemDate());
        assertEquals("40.00", f.park.getBasePrice().toPlainString());
    }
}
