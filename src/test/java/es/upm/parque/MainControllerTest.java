package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MainControllerTest {

    @Test
    void startsWithClockDateAndCanChange() {
        MainController controller = new MainController(Clock.fixed(Instant.parse("2026-08-26T19:03:15Z"), ZoneOffset.UTC));
        assertEquals(LocalDate.of(2026, 8, 26), controller.getCurrentDate());
        controller.setCurrentDate(LocalDate.of(2020, 1, 1));
        assertEquals(LocalDate.of(2020, 1, 1), controller.getCurrentDate());
    }

    @Test
    void parsesAndFormatsIsoDates() {
        LocalDate date = MainController.parseDate("2026-09-15");
        assertEquals(LocalDate.of(2026, 9, 15), date);
        assertEquals("2026-09-15", MainController.formatDate(date));
        assertEquals(LocalDate.of(2028, 2, 29), MainController.parseDate("2028-02-29"));
    }

    @Test
    void priceStartsAt40AndParsesStrictly() {
        MainController controller = new MainController(Clock.systemUTC());
        assertEquals(new BigDecimal("40.00"), controller.getTicketPrice());
        controller.setTicketPrice(MainController.parsePrice("60"));
        assertEquals("60.00", MainController.formatPrice(controller.getTicketPrice()));
        assertEquals(new BigDecimal("60.50"), MainController.parsePrice("60.5"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-5", "0", "1.234", "", "0.00"})
    void rejectsInvalidPrices(String text) {
        assertThrows(ParkException.class, () -> MainController.parsePrice(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"15/09/2026", "2026-9-15", "2026-02-30", "2027-02-29", "26-09-15", "", "hoy"})
    void rejectsInvalidDates(String text) {
        assertThrows(ParkException.class, () -> MainController.parseDate(text));
    }
}
