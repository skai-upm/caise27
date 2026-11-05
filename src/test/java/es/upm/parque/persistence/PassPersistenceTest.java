package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.BCryptPasswordProtector;
import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.business.ParkController;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.ParkSession;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.presentation.CommandProcessor;

class PassPersistenceTest {

    @TempDir
    Path dir;

    private CommandProcessor start() {
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ParkConfiguration.initialize(ParkRepositories.fileBacked(users, new ParkFileStore(dir)),
                new BCryptPasswordProtector(4));
        return new CommandProcessor();
    }

    private static void run(CommandProcessor p, String... lines) {
        for (String line : lines) {
            p.process(line);
        }
    }

    private static Pass pass(String id) {
        return ParkController.getInstance().getPasses().findById(id);
    }

    private void buildPark() {
        run(start(),
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457",
                "SYSTEM DATE;2027-04-01",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "TICKET CREATE;T003;ana@correo.com;2027-04-20",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "SPEEDPASS CREATE;SP02;T001;10",
                "SPECIALPASS CREATE;PE01;T001",
                "SPEEDPASS CREATE;SP05;TG01:87654321X;1",
                "SPEEDPASS CREATE;SP07;T003;1",
                "SPEEDPASS CREATE;SP08;T003;1",
                "SPECIALPASS CREATE;PE03;T003",
                "SPEEDPASS DELETE;SP02:10",
                "SYSTEM PRICE;60",
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION ENTER;A01;T001;SPEED",
                "ATTRACTION ENTER;A04;T001",
                "SYSTEM DATE;2027-04-01",
                "TICKET DELETE;T003");
    }

    @Test
    void passesAreWrittenWithKindTicketPriceAndState() throws IOException {
        buildPark();
        List<String> lines = Files.readAllLines(dir.resolve(ParkFileStore.PASSES_FILE), StandardCharsets.UTF_8);
        assertEquals("S;SP02:1;T001;8.00;true", lines.get(0));
        assertTrue(lines.contains("S;SP02:2;T001;8.00;false"));
        assertTrue(lines.contains("P;PE01;T001;40.00;true"));
        assertTrue(lines.contains("S;SP05:1;TG01:87654321X;8.00;false"), lines.toString());
        assertEquals(11, lines.size(), "9 of SP02 + PE01 + SP05:1; refunded and cascaded passes are gone");
    }

    @Test
    void passesAreRestoredAfterRestart() {
        buildPark();
        CommandProcessor p = start();
        Pass sp1 = pass("SP02:1");
        assertTrue(sp1 instanceof SpeedPass && sp1.isConsumed());
        assertEquals("T001", sp1.getTicket().getId());
        assertEquals(new BigDecimal("8.00"), sp1.getPrice());
        assertFalse(pass("SP02:2").isConsumed());
        Pass pe1 = pass("PE01");
        assertTrue(pe1 instanceof SpecialPass && pe1.isConsumed());
        assertEquals(new BigDecimal("40.00"), pe1.getPrice());
        assertEquals(new BigDecimal("8.00"), pass("SP05:1").getPrice(), "2 members: no group discount");
        assertEquals("TG01:87654321X", pass("SP05:1").getTicket().getId());
        assertNull(pass("SP02:10"), "refunded");
        assertNull(pass("SP07:1"), "cascaded with T003");
        assertNull(pass("PE03"), "cascaded with T003");
        assertTrue(p.process("SPECIALPASS CREATE;PE09;T001").startsWith("ERROR"), "consumed Special Pass kept");
        assertTrue(p.process("SPEEDPASS CREATE;SP02;T001;1").contains("SP02:1 is already in use"));
        p.process("SYSTEM DATE;2027-04-15");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001;SPEED").endsWith("SpeedPass consumed: SP02:2"));
        assertTrue(p.process("ATTRACTION ENTER;A04;T001").contains("already been consumed"));
        assertTrue(ParkController.getInstance().getAttractions().findById("A01").getOccupants().size() == 1);
    }

    @Test
    void consumedStateChangesAreSaved() {
        buildPark();
        CommandProcessor p = start();
        p.process("SYSTEM DATE;2027-04-15");
        p.process("ATTRACTION ENTER;A01;T001;SPEED");
        start();
        assertTrue(pass("SP02:2").isConsumed());
        assertTrue(ParkController.getInstance().getAttractions().findById("A01").getOccupants().isEmpty(),
                "occupants are not persisted");
    }

    @Test
    void malformedPassLinesAreIgnored() throws IOException {
        buildPark();
        Path file = dir.resolve(ParkFileStore.PASSES_FILE);
        List<String> lines = new java.util.ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        lines.addAll(List.of(
                "S;X1;T999;8.00;false",
                "S;X2;TG01;8.00;false",
                "S;X3;T001;abc;false",
                "S;X4;T001;8.00;maybe",
                "Q;X5;T001;8.00;false",
                "P;X6;T001;40.00;false",
                "S;;T001;8.00;false",
                "S;SP02:1;T001;8.00;false",
                "S;X7;T001;8.00",
                "S;X8;TG01:12345678Z;7.20;false"));
        Files.write(file, lines, StandardCharsets.UTF_8);
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ParkFileStore store = new ParkFileStore(dir);
        ParkConfiguration.initialize(ParkRepositories.fileBacked(users, store), new BCryptPasswordProtector(4));
        assertEquals(9, store.getMalformedLineCount());
        assertNotNull(pass("X8"));
        assertNull(pass("X6"), "second Special Pass for T001");
        assertTrue(pass("SP02:1").isConsumed(), "the first line with that identifier wins");
    }

    @Test
    void flyModeKeepsPassesInTheTemporaryDirectoryOnly() throws ParkException, IOException {
        ParkSession session = ParkSession.open(new String[] {"--fly"}, dir);
        CommandProcessor p = new CommandProcessor();
        run(p, "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "SYSTEM DATE;2027-04-01", "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "SPEEDPASS CREATE;SP01;T001;1");
        Path tmp = Files.list(dir.resolve("tmp")).findFirst().orElseThrow();
        assertEquals(List.of("S;SP01:1;T001;8.00;false"),
                Files.readAllLines(tmp.resolve(ParkFileStore.PASSES_FILE), StandardCharsets.UTF_8));
        assertFalse(Files.exists(dir.resolve(ParkFileStore.PASSES_FILE)));
        session.close();
        assertFalse(Files.exists(tmp));
    }
}
