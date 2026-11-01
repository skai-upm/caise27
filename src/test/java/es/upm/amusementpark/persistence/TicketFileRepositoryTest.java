package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

public class TicketFileRepositoryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;

    private final User ana = new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
            "612345678", "1234567890123456");
    private final User pedro = new User("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
            "623456789", "9876543210987654");

    @Test
    public void storageRoundTripResolvesAndRebuildsReferences() throws IOException {
        Group g = new Group("G01", "Familia");
        g.addMember(ana);
        g.addMember(pedro);
        List<Ticket> tickets = List.of(
                new IndividualTicket("T001", DAY, new BigDecimal("40.00"), ana),
                new GroupTicket("TG01", DAY, new BigDecimal("80.00"), g, List.of(
                        new IndividualTicket("TG01-1", DAY, new BigDecimal("40.00"), ana),
                        new IndividualTicket("TG01-2", DAY, new BigDecimal("40.00"), pedro))));
        Path file = dir.resolve("tickets.txt");
        TicketFileRepository storage = new TicketFileRepository(file);
        storage.save(tickets);
        assertEquals(List.of(
                "I;T001;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez;0",
                "G;TG01;2026-10-15;80.00;G01;Familia",
                "C;TG01;TG01-1;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez;0",
                "C;TG01;TG01-2;2026-10-15;40.00;87654321X;pedro@correo.com;Pedro;Lopez Martin;0"),
                Files.readAllLines(file));

        // Ana is still registered, Pedro was deleted and the group no longer exists
        List<Ticket> loaded = storage.load(dni -> dni.equals("12345678Z") ? ana : null, id -> null);
        assertEquals(2, loaded.size());
        assertSame(ana, ((IndividualTicket) loaded.get(0)).getUser());
        GroupTicket gt = (GroupTicket) loaded.get(1);
        assertEquals("Familia", gt.getGroup().getName());
        assertTrue(gt.getGroup().getMembers().isEmpty());
        User detached = gt.getTickets().get(1).getUser();
        assertEquals("87654321X", detached.getDni());
        assertEquals("pedro@correo.com", detached.getEmail());
        assertEquals("", detached.getPassword());
    }

    @Test
    public void malformedTicketLinesAreSkipped() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of("", "I;T1;notadate;40.00;1;a;b;c", "X;foo", "C;MISSING;T2;2026-10-15;1;1;a;b;c"));
        assertTrue(new TicketFileRepository(file).load(dni -> null, id -> null).isEmpty());
    }

    @Test
    public void passesAndFlagsRoundTrip() throws IOException {
        IndividualTicket t = new IndividualTicket("T001", DAY, new BigDecimal("40.00"), ana);
        t.addSpeedPass(new SpeedPass("T001-SP1", new BigDecimal("8.00"), true));
        t.addSpeedPass(new SpeedPass("T001-SP2", new BigDecimal("8.00"), false));
        t.setSpecialPass(new SpecialPass("T001-SPECIAL", new BigDecimal("40.00"), false));
        Path file = dir.resolve("tickets.txt");
        TicketFileRepository repository = new TicketFileRepository(file);
        repository.save(List.of(t));
        assertEquals(List.of(
                "I;T001;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez;1",
                "S;T001;T001-SP1;8.00;1",
                "S;T001;T001-SP2;8.00;0",
                "X;T001;T001-SPECIAL;40.00;0"), Files.readAllLines(file));

        IndividualTicket loaded = (IndividualTicket) repository.load(dni -> ana, id -> null).get(0);
        assertEquals(2, loaded.getSpeedPasses().size());
        assertTrue(loaded.getSpeedPasses().get(0).isConsumed());
        assertEquals("T001-SPECIAL", loaded.getSpecialPass().getId());
        assertTrue(loaded.hasAcquiredSpecialPass());
    }

    @Test
    public void deliveryTwoLinesStillLoad() throws IOException {
        Path file = dir.resolve("tickets.txt");
        Files.write(file, List.of(
                "I;T001;2026-10-15;40.00;12345678Z;ana@correo.com;Ana;Garcia Lopez",
                "G;TG01;2026-10-15;80.00;G01;Familia",
                "C;TG01;TG01-1;2026-10-15;40.00;87654321X;pedro@correo.com;Pedro;Lopez Martin"));
        List<Ticket> loaded = new TicketFileRepository(file).load(dni -> null, id -> null);
        assertEquals(2, loaded.size());
        assertFalse(((IndividualTicket) loaded.get(0)).hasAcquiredSpecialPass());
        assertEquals(1, ((GroupTicket) loaded.get(1)).getTickets().size());
    }
}
