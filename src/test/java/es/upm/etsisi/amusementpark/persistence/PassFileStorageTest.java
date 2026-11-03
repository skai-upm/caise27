package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassFileStorageTest {

    @TempDir
    Path dir;

    final Ticket t001 = new Ticket("T001", TestData.user("12345678Z", "ana@correo.com"), LocalDate.of(2026, 10, 15),
            new BigDecimal("40.00"));

    private Optional<Ticket> lookup(String id) {
        return id.equals("T001") ? Optional.of(t001) : Optional.empty();
    }

    @Test
    void missingFileMeansNoPasses() {
        assertTrue(new PassFileStorage(dir.resolve("none.txt")).load(this::lookup).isEmpty());
    }

    @Test
    void roundTripKeepsPriceTicketAndStatus() throws IOException {
        Path file = dir.resolve("passes.txt");
        PassFileStorage storage = new PassFileStorage(file);
        Pass consumed = new SpeedPass("S1-1", t001);
        consumed.markConsumed();
        storage.save(List.of(consumed, new SpecialPass("SP1", t001)));
        assertEquals(List.of("S1-1;SPEED;T001;8.00;true", "SP1;SPECIAL;T001;40.00;false"), Files.readAllLines(file));
        Files.write(file, List.of("S1-1;SPEED;T001;8.00;true", "SP1;SPECIAL;T001;40.00;false", "",
                "X1;SPEED;T999;8.00;false", "bad", "X2;OTHER;T001;1.00;false", "X3;SPEED;T001;abc;false",
                "X4;SPEED;T001;1.00;maybe"));
        List<Pass> loaded = storage.load(this::lookup);
        assertEquals(2, loaded.size());
        assertTrue(loaded.get(0).isConsumed());
        assertSame(t001, loaded.get(1).getTicket());
        assertEquals("40.00", loaded.get(1).getPrice().toPlainString());
    }
}
