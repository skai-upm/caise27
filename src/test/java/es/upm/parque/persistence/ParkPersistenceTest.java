package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.ParkController;

class ParkPersistenceTest {

    @TempDir
    Path dir;

    private CommandProcessor startExecution() {
        UserRepository users = new UserRepository(new UserFileStore(dir.resolve("users.txt")));
        ParkController.getInstance().startExecution(users, new ParkFileStore(dir));
        return new CommandProcessor();
    }

    private static void run(CommandProcessor p, String... lines) {
        for (String line : lines) {
            p.process(line);
        }
    }

    private void buildPark() {
        run(startExecution(),
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457",
                "SYSTEM DATE;2027-04-01",
                "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01;2027-04-20",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION ENTER;A01;T001");
    }

    @Test
    void parkIsReconstructedAfterRestart() {
        buildPark();
        ParkController park = ParkController.getInstance();
        CommandProcessor p = startExecution();
        assertEquals(2, park.getGroups().findById("G01").getMembers().size());
        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001");
        assertSame(park.getUsers().findByDni("12345678Z"), t001.getUser());
        assertEquals(LocalDate.of(2027, 4, 15), t001.getDate());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01");
        assertSame(park.getGroups().findById("G01"), tg01.getGroup());
        assertEquals(new BigDecimal("80.00"), tg01.getPrice());
        assertEquals(2, tg01.getComposition().size());
        assertSame(park.getUsers().findByDni("87654321X"), tg01.getComposition().get(1).getUser());
        Attraction a01 = park.getAttractions().findById("A01");
        assertEquals(20, a01.getCapacity());
        assertTrue(a01.getOccupants().isEmpty());
        p.process("SYSTEM DATE;2027-04-01");
        assertTrue(p.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(p.process("TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-30").startsWith("ERROR"));
    }

    @Test
    void historicalTicketsSurviveUserAndGroupDeletion() {
        run(startExecution(),
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "SYSTEM DATE;2027-04-01",
                "GROUP CREATE;G01;Familia",
                "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-02",
                "GROUPTICKET CREATE;TG01;G01;2027-04-02",
                "SYSTEM DATE;2027-04-03",
                "GROUP DELETE;G01",
                "USER DELETE;ana@correo.com;Clave1!");
        ParkController park = ParkController.getInstance();
        startExecution();
        assertFalse(park.getUsers().existsByDni("12345678Z"));
        IndividualTicket t001 = (IndividualTicket) park.getTickets().findById("T001");
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals("ana@correo.com", t001.getUser().getEmail());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01");
        assertNull(tg01.getGroup());
        assertEquals(1, tg01.getComposition().size());
        assertEquals(LocalDate.of(2027, 4, 2), tg01.getDate());
    }

    @Test
    void malformedLinesAreCounted() throws Exception {
        Files.write(dir.resolve(ParkFileStore.GROUPS_FILE), List.of("G01;Familia;", "bad"), StandardCharsets.UTF_8);
        Files.write(dir.resolve(ParkFileStore.TICKETS_FILE),
                List.of("I;T1;not-a-date;40.00;1;a@b.com;A;B", "G;TG;2027-01-01;;missing"), StandardCharsets.UTF_8);
        Files.write(dir.resolve(ParkFileStore.ATTRACTIONS_FILE), List.of("A01;Dragon;20", "A02;Bad;0"),
                StandardCharsets.UTF_8);
        ParkFileStore store = new ParkFileStore(dir);
        ParkController.getInstance().startExecution(new UserRepository(), store);
        assertEquals(4, store.getMalformedLineCount());
        assertEquals(1, ParkController.getInstance().getGroups().findAll().size());
        assertEquals(1, ParkController.getInstance().getAttractions().findAll().size());
    }

    @Test
    void flyLocationIsTemporaryAndCleanedUp() {
        StorageLocation fly = StorageLocation.fromArgs(new String[] {"--fly", "input.txt"}, dir);
        assertTrue(fly.isTemporary());
        assertTrue(Files.isDirectory(fly.getDirectory()));
        assertEquals(dir.resolve("tmp"), fly.getDirectory().getParent());
        assertEquals(List.of("input.txt"), List.of(fly.getCommandArgs()));
        StorageLocation second = StorageLocation.fromArgs(new String[] {"--fly"}, dir);
        assertFalse(second.getDirectory().equals(fly.getDirectory()));
        fly.cleanUp();
        second.cleanUp();
        assertFalse(Files.exists(fly.getDirectory()));
        StorageLocation normal = StorageLocation.fromArgs(new String[] {"input.txt"}, dir);
        assertFalse(normal.isTemporary());
        assertEquals(dir, normal.getDirectory());
        normal.cleanUp();
        assertTrue(Files.exists(dir));
    }
}
