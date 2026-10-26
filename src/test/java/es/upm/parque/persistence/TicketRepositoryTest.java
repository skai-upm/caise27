package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.User;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);
    private static final BigDecimal FORTY = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
    private final User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
    private final Group family = new Group("G01", "Familia");

    private TicketRepository repo() {
        return new TicketRepository(dir.resolve("data").resolve("tickets.txt"), dir.resolve("data").resolve("grouptickets.txt"),
                dir.resolve("data").resolve("passes.txt"));
    }

    @Test
    void missingFilesMeanNoTickets() {
        TicketRepository.TicketData data = repo().load(Map.of(), Map.of());
        assertTrue(data.individual().isEmpty());
        assertTrue(data.group().isEmpty());
    }

    @Test
    void roundTripIndividualAndGroupTickets() throws IOException {
        IndividualTicket single = new IndividualTicket("T001", DAY, ana, FORTY);
        IndividualTicket a = new IndividualTicket("TG01-1", DAY, ana, FORTY);
        IndividualTicket p = new IndividualTicket("TG01-2", DAY, pedro, FORTY);
        GroupTicket group = new GroupTicket("TG01", DAY, family, new BigDecimal("80.00"), List.of(a, p));
        repo().save(List.of(single, a, p), List.of(group));

        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00;", "TG01-1;12345678Z;2026-10-15;40.00;TG01",
                "TG01-2;87654321X;2026-10-15;40.00;TG01"), Files.readAllLines(dir.resolve("data").resolve("tickets.txt")));
        assertEquals(List.of("TG01;G01;2026-10-15;80.00"), Files.readAllLines(dir.resolve("data").resolve("grouptickets.txt")));

        TicketRepository.TicketData data = repo().load(Map.of("12345678Z", ana, "87654321X", pedro), Map.of("G01", family));
        assertEquals(3, data.individual().size());
        GroupTicket loaded = data.group().get(0);
        assertSame(family, loaded.getGroup());
        assertEquals(new BigDecimal("80.00"), loaded.getPrice());
        assertEquals(List.of(ana, pedro), loaded.getUsers());
        assertSame(data.individual().get(1), loaded.getTickets().get(0));
        assertSame(ana, data.individual().get(0).getUser());
    }

    @Test
    void legacyAndInvalidLinesAreHandled() throws IOException {
        Path individual = dir.resolve("tickets.txt");
        Path group = dir.resolve("grouptickets.txt");
        Files.write(individual, List.of("T1;00000000T;2026-10-15;40.00", "T2;12345678Z;2026-13-01;40.00",
                "T3;12345678Z;2026-10-15;cuarenta", "bad", "T4;12345678Z;2026-10-15;60.00",
                "T5;12345678Z;2026-10-15;40.00;TG99"));
        Files.write(group, List.of("TG98;G99;2026-10-15;80.00", "TG97;G01;fecha;80.00", "x"));
        TicketRepository.TicketData data = new TicketRepository(individual, group, dir.resolve("passes.txt")).load(Map.of("12345678Z", ana),
                Map.of("G01", family));
        assertEquals(1, data.individual().size());
        assertEquals("T4", data.individual().get(0).getId());
        assertTrue(data.group().isEmpty());
    }

    @Test
    void passesRoundTripWithTheirTickets() throws IOException {
        IndividualTicket single = new IndividualTicket("T001", DAY, ana, FORTY);
        single.addSpeedPass(new SpeedPass("SP1", new BigDecimal("8.00"), true));
        single.addSpeedPass(new SpeedPass("SP2", new BigDecimal("8.00"), false));
        single.setSpecialPass(new SpecialPass("XP1", FORTY, false));
        IndividualTicket internal = new IndividualTicket("TG01-1", DAY, pedro, FORTY);
        internal.addSpeedPass(new SpeedPass("SP3", new BigDecimal("8.00"), false));
        GroupTicket group = new GroupTicket("TG01", DAY, family, FORTY, List.of(internal));
        repo().save(List.of(single, internal), List.of(group));
        assertEquals(List.of("SP1;SPEED;T001;8.00;true", "SP2;SPEED;T001;8.00;false", "XP1;SPECIAL;T001;40.00;false",
                "SP3;SPEED;TG01-1;8.00;false"), Files.readAllLines(dir.resolve("data").resolve("passes.txt")));

        TicketRepository.TicketData data = repo().load(Map.of("12345678Z", ana, "87654321X", pedro), Map.of("G01", family));
        IndividualTicket t = data.individual().get(0);
        assertEquals(2, t.getSpeedPasses().size());
        assertTrue(t.getSpeedPasses().get(0).isConsumed());
        assertEquals(FORTY, t.getSpecialPass().getPrice());
        assertEquals("SP3", data.individual().get(1).getPasses().get(0).getId());
    }

    @Test
    void passesOfUnknownTicketsOrMalformedAreSkipped() throws IOException {
        repo().save(List.of(new IndividualTicket("T001", DAY, ana, FORTY)), List.of());
        Files.write(dir.resolve("data").resolve("passes.txt"), List.of("SP1;SPEED;T999;8.00;false",
                "SP2;SPEED;T001;ocho;false", "SP3;OTHER;T001;8.00;false", "bad", "SP4;SPEED;T001;8.00;false"));
        IndividualTicket t = repo().load(Map.of("12345678Z", ana), Map.of()).individual().get(0);
        assertEquals(List.of("SP4"), t.getPasses().stream().map(Pass::getId).toList());
    }

    @Test
    void ioErrorsAreUnchecked() throws IOException {
        TicketRepository unreadable = new TicketRepository(dir, dir, dir);
        Map<String, User> users = Map.of();
        Map<String, Group> groups = Map.of();
        assertThrows(UncheckedIOException.class, () -> unreadable.load(users, groups));

        Path blocker = Files.writeString(dir.resolve("blocker"), "not a directory");
        TicketRepository unwritable = new TicketRepository(blocker.resolve("t.txt"), blocker.resolve("g.txt"),
                blocker.resolve("p.txt"));
        List<IndividualTicket> none = List.of();
        List<GroupTicket> noGroups = List.of();
        assertThrows(UncheckedIOException.class, () -> unwritable.save(none, noGroups));
    }

    @Test
    void failedSaveLeavesEveryFileUnchanged() throws IOException {
        IndividualTicket a = new IndividualTicket("TG01-1", DAY, ana, FORTY);
        a.addSpeedPass(new SpeedPass("SP1", new BigDecimal("8.00"), false));
        GroupTicket group = new GroupTicket("TG01", DAY, family, FORTY, List.of(a));
        repo().save(List.of(a), List.of(group));
        Path data = dir.resolve("data");
        List<String> groupsBefore = Files.readAllLines(data.resolve("grouptickets.txt"));
        List<String> passesBefore = Files.readAllLines(data.resolve("passes.txt"));
        assertTrue(data.resolve("tickets.txt").toFile().setWritable(false));
        try {
            List<IndividualTicket> none = List.of();
            List<GroupTicket> noGroups = List.of();
            assertThrows(UncheckedIOException.class, () -> repo().save(none, noGroups));
        } finally {
            assertTrue(data.resolve("tickets.txt").toFile().setWritable(true));
        }
        assertEquals(groupsBefore, Files.readAllLines(data.resolve("grouptickets.txt")));
        assertEquals(passesBefore, Files.readAllLines(data.resolve("passes.txt")));
        assertEquals(1, Files.readAllLines(data.resolve("tickets.txt")).size());
        try (Stream<Path> files = Files.list(data)) {
            assertTrue(files.noneMatch(f -> f.toString().endsWith(".tmp")));
        }
    }
}
