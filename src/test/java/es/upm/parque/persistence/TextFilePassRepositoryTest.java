package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;

class TextFilePassRepositoryTest {

    @TempDir
    Path dir;

    private IndividualTicket ticket() {
        return new IndividualTicket("T001", LocalDate.of(2026, 10, 15), Fixtures.ana(), new BigDecimal("40.00"));
    }

    @Test
    void missingFileLoadsNothing() throws IOException {
        IndividualTicket t001 = ticket();
        new TextFilePassRepository(dir.resolve("passes.txt")).load(Map.of("T001", t001)::get);
        assertTrue(t001.getPasses().isEmpty());
    }

    @Test
    void savesAndRestoresPassesOntoTickets() throws IOException {
        Path file = dir.resolve("data").resolve("passes.txt");
        IndividualTicket t001 = ticket();
        t001.addSpeedPass(new SpeedPass("T001-S1", new BigDecimal("8.00"), true));
        t001.setSpecialPass(new SpecialPass("T001-SPECIAL", new BigDecimal("40.00"), false));
        new TextFilePassRepository(file).save(List.of(t001));
        assertEquals(List.of("T001;T001-S1;SPEED;8.00;true", "T001;T001-SPECIAL;SPECIAL;40.00;false"),
                Files.readAllLines(file));
        IndividualTicket restored = ticket();
        new TextFilePassRepository(file).load(Map.of("T001", restored)::get);
        assertEquals(1, restored.getSpeedPasses().size());
        assertTrue(restored.getSpeedPasses().get(0).isConsumed());
        assertEquals(new BigDecimal("8.00"), restored.getSpeedPasses().get(0).getPrice());
        assertFalse(restored.getSpecialPass().isConsumed());
    }

    @Test
    void ignoresUnknownTicketsAndMalformedLines() throws IOException {
        Path file = dir.resolve("passes.txt");
        Files.write(file, List.of("roto", "T999;T999-S1;SPEED;8.00;false", "T001;;SPEED;8.00;false",
                "T001;T001-S1;SPEED;gratis;false", "T001;T001-X;OTHER;8.00;false", "T001;T001-S2;SPEED;8.00;false"));
        IndividualTicket t001 = ticket();
        new TextFilePassRepository(file).load(Map.of("T001", t001)::get);
        assertEquals(1, t001.getPasses().size());
        assertEquals("T001-S2", t001.getPasses().get(0).getId());
    }
}
