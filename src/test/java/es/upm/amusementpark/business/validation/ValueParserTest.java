package es.upm.amusementpark.business.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.amusementpark.business.exception.ParkException;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueParserTest {

    @Test
    void parsesDates() throws Exception {
        assertEquals(LocalDate.of(2027, 4, 15), ValueParser.parseDate("2027-04-15"));
        assertEquals(LocalDate.of(2028, 2, 29), ValueParser.parseDate("2028-02-29"));
        assertEquals("2027-04-15", ValueParser.formatDate(LocalDate.of(2027, 4, 15)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-29", "2027-13-01", "2027-4-15", "27-04-15", "2027/04/15", " 2027-04-15"})
    void rejectsBadDates(String value) {
        assertThrows(ParkException.class, () -> ValueParser.parseDate(value));
    }

    @Test
    void parsesPrices() throws Exception {
        assertEquals(new BigDecimal("45.50"), ValueParser.parsePrice("45.50"));
        assertEquals(new BigDecimal("45.50"), ValueParser.parsePrice("45.5"));
        assertEquals(new BigDecimal("0.01"), ValueParser.parsePrice("0.01"));
        assertEquals(new BigDecimal("40.00"), ValueParser.parsePrice("40"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "+1", "1e2", "1.234", "1.2.3", "abc", "", ".", "1,5"})
    void rejectsBadPrices(String value) {
        assertThrows(ParkException.class, () -> ValueParser.parsePrice(value));
    }

    @Test
    void parsesPositiveInts() throws Exception {
        assertEquals(20, ValueParser.parsePositiveInt("20"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-3", "3.5", "x", "", "99999999999"})
    void rejectsBadInts(String value) {
        assertThrows(ParkException.class, () -> ValueParser.parsePositiveInt(value));
    }
}
