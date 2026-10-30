package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkSettingsTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
    }

    @Test
    void startsWithOsDateAndDefaultPrice() {
        assertEquals(LocalDate.now(), s.park().getSystemDate());
        assertEquals(new BigDecimal("40.00"), s.park().getBasePrice());
    }

    @Test
    void dateCanMoveForwardAndBackward() {
        assertTrue(s.run("SYSTEM DATE;2026-10-16").startsWith("OK"));
        assertEquals(LocalDate.of(2026, 10, 16), s.park().getSystemDate());
        assertTrue(s.run("SYSTEM DATE;2026-10-01").startsWith("OK"));
        assertEquals(LocalDate.of(2026, 10, 1), s.park().getSystemDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;2026-13-01", "SYSTEM DATE;15/09/2026", "SYSTEM DATE;2026-02-30",
        "SYSTEM DATE;", "SYSTEM DATE"})
    void invalidDateKeepsPrevious(String command) {
        s.run("SYSTEM DATE;2026-09-15");
        assertTrue(s.run(command).startsWith("ERROR"));
        assertEquals(LocalDate.of(2026, 9, 15), s.park().getSystemDate());
    }

    @Test
    void priceCanBeChanged() {
        assertTrue(s.run("SYSTEM PRICE;60.00").startsWith("OK"));
        assertEquals(new BigDecimal("60.00"), s.park().getBasePrice());
        s.run("SYSTEM PRICE;55");
        assertEquals(new BigDecimal("55.00"), s.park().getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-5", "0", "0.00", "1.234", ".5", "5.", "1.2.3", ""})
    void invalidPriceKeepsPrevious(String price) {
        assertTrue(s.run("SYSTEM PRICE;" + price).startsWith("ERROR"));
        assertEquals(new BigDecimal("40.00"), s.park().getBasePrice());
    }

    @Test
    void priceResetsOnRestart() throws IOException {
        s.run("SYSTEM PRICE;60.00");
        s.restart();
        assertEquals(new BigDecimal("40.00"), s.park().getBasePrice());
    }
}
