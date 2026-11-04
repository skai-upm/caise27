package es.upm.park.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void roundTripKeepsKindPriceAndConsumedState() throws IOException {
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "x", "1", "1234567890123456");
        IndividualTicket t = new IndividualTicket("T001", ana, LocalDate.of(2026, 10, 15), new BigDecimal("40.00"));
        SpeedPass sp = new SpeedPass("SP1", t, "T001", new BigDecimal("8.00"), false);
        SpecialPass sx = new SpecialPass("SX1", t, "T001", new BigDecimal("40.00"), false);
        sx.markConsumed();
        Path file = dir.resolve("passes.txt");
        PassFileStorage storage = new PassFileStorage(file);
        storage.save(List.of(sp, sx));
        assertEquals(List.of("SPEED;SP1;T001;8.00;false", "SPECIAL;SX1;T001;40.00;true"), Files.readAllLines(file));
        Files.writeString(file, "SPEED;SP9;T999;8.00;false\nbad\nOTHER;X;T001;1;false\nSPEED;Y;T001;x;false\n",
                StandardOpenOption.APPEND);

        List<Pass> loaded = storage.load(Map.of("T001", t)::get);
        assertEquals(3, loaded.size());
        assertSame(t, loaded.get(0).getTicket());
        assertTrue(loaded.get(1) instanceof SpecialPass && loaded.get(1).isConsumed());
        assertNull(loaded.get(2).getTicket());
        assertEquals("T999", loaded.get(2).getTicketId());
    }

    @Test
    void missingFileMeansNoPasses() {
        assertTrue(new PassFileStorage(dir.resolve("none.txt")).load(id -> null).isEmpty());
    }
}
