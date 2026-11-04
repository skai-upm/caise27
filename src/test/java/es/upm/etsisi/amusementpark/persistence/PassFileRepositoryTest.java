package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.SpecialPass;
import es.upm.etsisi.amusementpark.business.model.SpeedPass;
import es.upm.etsisi.amusementpark.business.model.User;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassFileRepositoryTest {

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "h", "1", "1234567890123456");
    private final IndividualTicket t001 = new IndividualTicket("T001", LocalDate.of(2026, 10, 15), ana, new BigDecimal("40.00"));

    @Test
    void savesExactLinesAndRestoresLinks() throws Exception {
        Path file = dir.resolve("passes.txt");
        SpeedPass consumed = new SpeedPass("SP1-2", t001);
        consumed.markConsumed();
        new PassFileRepository(file).save(List.of(new SpeedPass("SP1-1", t001), consumed, new SpecialPass("VIP1", t001)));
        assertEquals(List.of("SPEED;SP1-1;T001;8.00;false", "SPEED;SP1-2;T001;8.00;true", "SPECIAL;VIP1;T001;40.00;false"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        List<Pass> loaded = new PassFileRepository(file).load(Map.of("T001", t001)::get);
        assertEquals(3, loaded.size());
        assertSame(t001, loaded.get(0).getTicket());
        assertTrue(loaded.get(1).isConsumed());
        assertTrue(loaded.get(2) instanceof SpecialPass);
    }

    @Test
    void skipsMalformedOrOrphanLines() throws Exception {
        Path file = dir.resolve("passes.txt");
        Files.write(file, List.of("SPEED;SP1-1;T001;8.00;false", "SPEED;SP1-2;T999;8.00;false", "GOLD;G1;T001;8.00;false",
                "SPEED;SP1-3;T001;x;false", "SPEED;SP1-4;T001;8.00;maybe", "SPEED;SP1-5", ""), StandardCharsets.UTF_8);
        assertEquals(1, new PassFileRepository(file).load(Map.of("T001", t001)::get).size());
        assertTrue(new PassFileRepository(dir.resolve("none.txt")).load(id -> null).isEmpty());
    }
}
