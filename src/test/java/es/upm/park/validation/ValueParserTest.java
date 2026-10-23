package es.upm.park.validation;

import es.upm.park.service.OperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueParserTest {

    @Test
    void parsesIsoDate() throws OperationException {
        assertEquals(LocalDate.of(2027, 4, 15), ValueParser.parseDate("2027-04-15"));
        assertEquals("2027-04-15", ValueParser.formatDate(LocalDate.of(2027, 4, 15)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2027-02-30", "15/04/2027", "2027-4-15", "", "tomorrow"})
    void rejectsInvalidDates(String text) {
        assertThrows(OperationException.class, () -> ValueParser.parseDate(text));
    }

    @Test
    void parsesPricesWithTwoDecimals() throws OperationException {
        assertEquals(new BigDecimal("45.50"), ValueParser.parsePrice("45.50"));
        assertEquals(new BigDecimal("45.50"), ValueParser.parsePrice("45.5"));
        assertEquals(new BigDecimal("40.00"), ValueParser.parsePrice("40"));
        assertEquals("40.00", ValueParser.parsePrice("40").toPlainString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "0.00", "-3", "abc", "1.234", "1e3", ".5", "5.", "4,5", "+4"})
    void rejectsInvalidPrices(String text) {
        assertThrows(OperationException.class, () -> ValueParser.parsePrice(text));
    }

    @Test
    void parsesCapacity() throws OperationException {
        assertEquals(20, ValueParser.parseCapacity("20"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "-1", "diez", "2.5", "9999999999"})
    void rejectsInvalidCapacity(String text) {
        assertThrows(OperationException.class, () -> ValueParser.parseCapacity(text));
    }
}
