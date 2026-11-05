package es.upm.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassFileStoreTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia", "12345678Z", "ana@correo.com", "h", "6", "1234567890123456");
    private final IndividualTicket t001 = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), new BigDecimal("40.00"), ana);

    private Optional<IndividualTicket> lookup(String id) {
        return id.equals("T001") ? Optional.of(t001) : Optional.empty();
    }

    @Test
    void roundTripKeepsKindPriceTicketAndConsumption() throws IOException {
        Path file = dir.resolve("passes.txt");
        Pass speed = new SpeedPass("SP01-1", new BigDecimal("8.00"), t001, false);
        Pass special = new SpecialPass("SPC1", new BigDecimal("40.00"), t001, true);
        PassFileStore store = new PassFileStore(file);
        store.save(List.of(speed, special), List.of("SPEED;OLD-1;T999;8.00;false"));
        assertEquals(List.of("SPEED;SP01-1;T001;8.00;false", "SPECIAL;SPC1;T001;40.00;true",
                "SPEED;OLD-1;T999;8.00;false"), Files.readAllLines(file));
        PassFileStore.Contents c = store.load(this::lookup);
        assertEquals(2, c.passes().size());
        assertTrue(c.passes().get(0) instanceof SpeedPass);
        assertSame(t001, c.passes().get(1).getTicket());
        assertTrue(c.passes().get(1).isConsumed());
        assertEquals(List.of("SPEED;OLD-1;T999;8.00;false"), c.historicalLines());
        assertEquals("OLD-1", PassFileStore.idOf(c.historicalLines().get(0)));
    }

    @Test
    void malformedLinesBecomeHistoryAndMissingFileIsEmpty() throws IOException {
        Path file = dir.resolve("passes.txt");
        assertTrue(new PassFileStore(file).load(this::lookup).passes().isEmpty());
        Files.write(file, List.of("OTHER;X;T001;1;false", "SPEED;Y;T001;abc;false", "SPEED;Z;T001;1;maybe", ""));
        PassFileStore.Contents c = new PassFileStore(file).load(this::lookup);
        assertTrue(c.passes().isEmpty());
        assertEquals(3, c.historicalLines().size());
    }
}
