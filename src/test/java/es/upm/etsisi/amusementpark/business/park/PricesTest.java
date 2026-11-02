package es.upm.etsisi.amusementpark.business.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PricesTest {

    @ParameterizedTest
    @CsvSource({"45.50,45.50", "40,40.00", "0.5,0.50", "007.1,7.10"})
    void validAmounts(String text, String expected) {
        assertEquals(new BigDecimal(expected), Prices.parsePositive(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "0.00", "-5", "abc", "10.999", "1e3", "10,5", "1.", ".5", "1.2.3", "+5"})
    void invalidAmounts(String text) {
        assertNull(Prices.parsePositive(text));
    }

    @ParameterizedTest
    @CsvSource({"192,192.00 EUR", "45.5,45.50 EUR"})
    void format(String amount, String expected) {
        assertEquals(expected, Prices.format(new BigDecimal(amount)));
    }
}
