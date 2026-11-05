package es.upm.parque.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.TestSupport;
import es.upm.parque.persistence.UserRepository;

class ParkControllerTest {

    @Test
    void everyRequestReturnsTheSameInstance() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void startExecutionResetsDateAndPriceAndSetsUsers() {
        ParkController park = ParkController.getInstance();
        park.setCurrentDate(LocalDate.of(2000, 1, 1));
        park.setBasePrice(new BigDecimal("99.99"));
        UserRepository users = new UserRepository();
        park.startExecution(users);
        assertEquals(LocalDate.now(), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        assertSame(users, park.getUsers());
    }

    @Test
    void createdUserIsReachableThroughCentralManagement() {
        CommandProcessor processor = TestSupport.processor(new UserRepository());
        processor.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456");
        assertTrue(ParkController.getInstance().getUsers().existsByDni("12345678Z"));
        new CommandProcessor().process("USER DELETE;ana@correo.com;Clave1!");
        assertEquals(0, ParkController.getInstance().getUsers().size());
    }

    @Test
    void systemDateCommand() {
        CommandProcessor processor = TestSupport.processor(new UserRepository());
        ParkController park = ParkController.getInstance();
        assertTrue(processor.process("SYSTEM DATE;2027-04-15").startsWith("OK"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertTrue(processor.process("SYSTEM DATE;2027-05-01").startsWith("OK"));
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
        for (String bad : new String[] {"SYSTEM DATE;2027-02-30", "SYSTEM DATE;15/04/2027",
                "SYSTEM DATE;2027-4-15", "SYSTEM DATE", "SYSTEM DATE;2027-04-15;x"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
    }

    @Test
    void systemPriceCommand() {
        CommandProcessor processor = TestSupport.processor(new UserRepository());
        ParkController park = ParkController.getInstance();
        assertTrue(processor.process("SYSTEM PRICE;45.50").startsWith("OK"));
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
        for (String bad : new String[] {"SYSTEM PRICE;0", "SYSTEM PRICE;-5", "SYSTEM PRICE;abc",
                "SYSTEM PRICE;45.555", "SYSTEM PRICE;1E2", "SYSTEM PRICE"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
        TestSupport.processor(new UserRepository());
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void validPricesAreNormalisedToTwoDecimals() {
        assertEquals(new BigDecimal("45.00"), SystemValues.parsePrice("45"));
        assertEquals(new BigDecimal("45.50"), SystemValues.parsePrice("45.5"));
        assertEquals(new BigDecimal("0.01"), SystemValues.parsePrice("0.01"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0.00", ".5", "5.", "1.2.3", "+5", " 5"})
    void invalidPricesAreRejected(String text) {
        assertNull(SystemValues.parsePrice(text));
    }
}
