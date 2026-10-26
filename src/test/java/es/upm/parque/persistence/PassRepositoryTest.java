package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void roundTripKeepsTypePriceAndStatus() throws IOException {
        User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "x", "6", "1234567890123456");
        IndividualTicket ticket = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));
        SpeedPass speed = new SpeedPass("T001-SP1", ticket);
        speed.consume();
        SpecialPass special = new SpecialPass("T001-SPECIAL", ticket);
        PassRepository repository = new PassRepository(dir.resolve("passes.txt"));
        repository.saveAll(List.of(speed, special));

        assertEquals(List.of("SPEEDPASS;T001-SP1;T001;8.00;true", "SPECIALPASS;T001-SPECIAL;T001;40.00;false"),
                Files.readAllLines(dir.resolve("passes.txt")));

        List<Pass> loaded = repository.loadAll(Map.of("T001", ticket)::get);
        assertEquals(2, loaded.size());
        assertTrue(loaded.get(0) instanceof SpeedPass);
        assertTrue(loaded.get(0).isConsumed());
        assertEquals(new BigDecimal("8.00"), loaded.get(0).getPrice());
        assertTrue(loaded.get(1) instanceof SpecialPass);
        assertSame(ticket, loaded.get(1).getTicket());
    }

    @Test
    void unknownTicketsAndBadLinesAreSkipped() throws IOException {
        Files.writeString(dir.resolve("passes.txt"), "SPEEDPASS;T9-SP1;T9;8.00;false\nOTHER;x;T1;1;false\nbad\n");
        assertTrue(new PassRepository(dir.resolve("passes.txt")).loadAll(id -> null).isEmpty());
    }
}
