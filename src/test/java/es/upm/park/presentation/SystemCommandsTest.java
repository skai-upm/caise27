package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemCommandsTest {

    private final CommandProcessor processor = new CommandProcessor();
    private final ParkController park = ParkController.getInstance();

    @BeforeEach
    void reset() {
        ParkControllerTestSupport.reset();
    }

    @Test
    void systemDateSetsTheCurrentDate() {
        String out = processor.process("SYSTEM DATE;2027-04-15").output();
        assertEquals("SYSTEM DATE UPDATED: 2027-04-15", out);
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        processor.process("SYSTEM DATE;2027-05-01");
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
    }

    @Test
    void invalidDatesAreRejectedAndDateUnchanged() {
        processor.process("SYSTEM DATE;2027-04-15");
        for (String line : new String[] {"SYSTEM DATE;2027-02-30", "SYSTEM DATE;15/04/2027", "SYSTEM DATE", "SYSTEM DATE;"}) {
            assertTrue(processor.process(line).output().startsWith("ERROR"), line);
        }
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
    }

    @Test
    void systemPriceSetsTheBasePrice() {
        assertEquals("SYSTEM PRICE UPDATED: 45.50", processor.process("SYSTEM PRICE;45.50").output());
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
    }

    @Test
    void invalidPricesAreRejectedAndPriceUnchanged() {
        for (String line : new String[] {"SYSTEM PRICE;0", "SYSTEM PRICE;-5", "SYSTEM PRICE;abc",
                "SYSTEM PRICE;10.999", "SYSTEM PRICE", "SYSTEM PRICE;"}) {
            assertTrue(processor.process(line).output().startsWith("ERROR"), line);
        }
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void helpListsSystemCommands() {
        String help = processor.process("HELP").output();
        assertTrue(help.contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(help.contains("SYSTEM PRICE;<Price>"));
    }
}
