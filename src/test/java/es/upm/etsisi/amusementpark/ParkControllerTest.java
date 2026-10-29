package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.business.controller.*;
import es.upm.etsisi.amusementpark.business.exception.*;
import es.upm.etsisi.amusementpark.business.invoice.*;
import es.upm.etsisi.amusementpark.business.model.*;
import es.upm.etsisi.amusementpark.business.model.attraction.*;
import es.upm.etsisi.amusementpark.business.security.*;
import es.upm.etsisi.amusementpark.persistence.*;
import es.upm.etsisi.amusementpark.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkControllerTest extends ParkFixture {

    @Test
    void singleInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        for (Constructor<?> c : ParkController.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
    }

    @Test
    void initialDateAndPrice() {
        assertEquals(TODAY, park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
        App.bootstrap(new Storage(dir), LocalDate.now(), FAST_HASHER);
        assertEquals(LocalDate.now(), park.getCurrentDate());
    }

    @Test
    void systemDateCanChangeRepeatedly() throws Exception {
        park.setCurrentDate("2027-05-01");
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
        park.setCurrentDate("2027-04-01");
        assertEquals(LocalDate.of(2027, 4, 1), park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"15/04/2027", "2027-02-30", "", "2027-4-1"})
    void invalidDateRejected(String text) {
        assertThrows(ParkException.class, () -> park.setCurrentDate(text));
        assertEquals(TODAY, park.getCurrentDate());
    }

    @Test
    void validPrices() throws Exception {
        park.setCurrentPrice("45.50");
        assertEquals(new BigDecimal("45.50"), park.getCurrentPrice());
        park.setCurrentPrice("12");
        assertEquals(new BigDecimal("12.00"), park.getCurrentPrice());
        park.setCurrentPrice("0.5");
        assertEquals(new BigDecimal("0.50"), park.getCurrentPrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-3", "abc", "10.999", "1e3", ".5", "5.", "", "1.2.3", "4,5"})
    void invalidPriceRejected(String text) {
        assertThrows(ParkException.class, () -> park.setCurrentPrice(text));
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
    }

    @Test
    void priceAffectsOnlyNewTicketsAndResetsOnRestart() throws Exception {
        Ticket before = park.createTicket("T1", ANA, "2027-04-15");
        park.setCurrentPrice("45.50");
        Ticket after = park.createTicket("T2", ANA, "2027-04-15");
        assertEquals(new BigDecimal("40.00"), before.getPrice());
        assertEquals(new BigDecimal("45.50"), after.getPrice());
        restart();
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
        assertEquals(new BigDecimal("45.50"), park.findTicket("T2").getPrice());
    }
}
