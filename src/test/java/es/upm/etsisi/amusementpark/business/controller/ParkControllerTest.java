package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.persistence.Repositories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ParkControllerTest {

    @TempDir
    Path dir;

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void startsWithTodayAndFortyEuros() {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dir));
        assertEquals(LocalDate.now(), park.today());
        assertEquals(new BigDecimal("40.00"), park.basePrice());
    }

    @Test
    void dateCanBeChangedRepeatedly() {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dir));
        park.setDate("2027-04-15");
        assertEquals(LocalDate.of(2027, 4, 15), park.today());
        park.setDate("2026-01-01");
        assertEquals(LocalDate.of(2026, 1, 1), park.today());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-13-40", "15/04/2027", "", "2027-4-15"})
    void invalidDateKeepsCurrent(String text) {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dir));
        park.setDate("2027-04-15");
        assertThrows(ParkException.class, () -> park.setDate(text));
        assertEquals(LocalDate.of(2027, 4, 15), park.today());
    }

    @Test
    void priceCanBeChanged() {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dir));
        park.setPrice("45.50");
        assertEquals(new BigDecimal("45.50"), park.basePrice());
        park.setPrice("30");
        assertEquals(new BigDecimal("30.00"), park.basePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-3", "0", "abc", "1.234", ""})
    void invalidPriceKeepsCurrent(String text) {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dir));
        assertThrows(ParkException.class, () -> park.setPrice(text));
        assertEquals(new BigDecimal("40.00"), park.basePrice());
    }

    @Test
    void priceResetsOnNewExecution() {
        ParkController park = ParkController.getInstance();
        park.configure(Repositories.fileBased(dir));
        park.setPrice("45.50");
        park.configure(Repositories.fileBased(dir));
        assertEquals(new BigDecimal("40.00"), park.basePrice());
    }
}
