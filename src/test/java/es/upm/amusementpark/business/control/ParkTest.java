package es.upm.amusementpark.business.control;

import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.persistence.repository.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.persistence.repository.UserRepository;
import es.upm.amusementpark.business.service.UserService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        Park.getInstance().start(Repositories.inMemory(new UserRepository()));
        processor = ApplicationInitializer.initialize(Repositories.inMemory(Park.getInstance().users()));
    }

    @Test
    void singleInstance() {
        assertSame(Park.getInstance(), Park.getInstance());
        for (Constructor<?> c : Park.class.getDeclaredConstructors()) {
            assertTrue(Modifier.isPrivate(c.getModifiers()));
        }
        assertTrue(Modifier.isFinal(Park.class.getModifiers()));
    }

    @Test
    void initialState() {
        assertEquals(LocalDate.now(), Park.getInstance().getCurrentDate());
        assertEquals(new BigDecimal("40.00"), Park.getInstance().getCurrentPrice());
    }

    @Test
    void systemDate() {
        assertEquals("SYSTEM DATE UPDATED", processor.process("SYSTEM DATE;2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), Park.getInstance().getCurrentDate());
        processor.process("SYSTEM DATE;2026-01-01");
        assertEquals(LocalDate.of(2026, 1, 1), Park.getInstance().getCurrentDate());
        for (String bad : new String[] {"SYSTEM DATE;2027-02-30", "SYSTEM DATE;15/04/2027", "SYSTEM DATE"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(LocalDate.of(2026, 1, 1), Park.getInstance().getCurrentDate());
    }

    @Test
    void systemPrice() {
        assertEquals("SYSTEM PRICE UPDATED", processor.process("SYSTEM PRICE;45.50"));
        assertEquals(new BigDecimal("45.50"), Park.getInstance().getCurrentPrice());
        for (String bad : new String[] {"0", "-5", "abc", "10.999", "0.00", ".5", "5.", "1e3", ""}) {
            assertTrue(processor.process("SYSTEM PRICE;" + bad).startsWith("ERROR"), bad);
        }
        assertEquals(new BigDecimal("45.50"), Park.getInstance().getCurrentPrice());
    }

    @Test
    void newExecutionRevertsPrice() {
        processor.process("SYSTEM PRICE;45.50");
        Park.getInstance().start(Repositories.inMemory(new UserRepository()));
        assertEquals(new BigDecimal("40.00"), Park.getInstance().getCurrentPrice());
    }

    @Test
    void helpListsSystemCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(help.contains("SYSTEM PRICE;<Price>"));
    }
}
