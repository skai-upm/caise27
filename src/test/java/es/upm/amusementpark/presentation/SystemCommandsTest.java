package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SystemCommandsTest {

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir, LocalDate.of(2026, 1, 1));
        processor = new CommandProcessor(park);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void setsDateAnyNumberOfTimes() {
        assertEquals("SYSTEM DATE SET TO 2027-04-15", run("SYSTEM DATE;2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getDate());
        assertEquals("SYSTEM DATE SET TO 2025-12-31", run("system date; 2025-12-31 "));
        assertEquals(LocalDate.of(2025, 12, 31), park.getDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "tomorrow", "2027-13-01"})
    void rejectsInvalidDates(String date) {
        assertTrue(run("SYSTEM DATE;" + date).startsWith("ERROR"));
        assertEquals(LocalDate.of(2026, 1, 1), park.getDate());
    }

    @Test
    void setsPrice() {
        assertEquals("SYSTEM PRICE SET TO 45.50 EUR", run("SYSTEM PRICE;45.50"));
        assertEquals(new BigDecimal("45.50"), park.getPrice());
        assertEquals("SYSTEM PRICE SET TO 40.00 EUR", run("SYSTEM PRICE;40"));
        assertEquals("SYSTEM PRICE SET TO 0.50 EUR", run("SYSTEM PRICE;0.5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-3", "abc", "10.555", "1e3", ".5", "5.", "4,50", "1.2.3", ""})
    void rejectsInvalidPrices(String price) {
        assertTrue(run("SYSTEM PRICE;" + price).startsWith("ERROR"));
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void priceResetsWhenTheParkIsOpenedAgain() {
        run("SYSTEM PRICE;45.50");
        Park reopened = TestParks.open(dir);
        assertEquals(new BigDecimal("40.00"), reopened.getPrice());
    }

    @Test
    void wrongArgumentCountShowsSyntax() {
        assertTrue(run("SYSTEM DATE").contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(run("SYSTEM PRICE;1;2").contains("SYSTEM PRICE;<price>"));
    }
}
