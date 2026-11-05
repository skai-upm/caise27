package es.upm.park.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Result;
import es.upm.park.business.model.User;
import es.upm.park.persistence.AttractionFileStore;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.persistence.GroupTicketFileStore;
import es.upm.park.persistence.PassFileStore;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.persistence.TicketRepository;
import es.upm.park.persistence.UserFileStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CascadeDeletionTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);

    /** Ticket repository on file whose saves can be made to fail. */
    private static final class FlakyTicketRepository implements TicketRepository {
        private final TicketFileStore delegate;
        private boolean fail;

        FlakyTicketRepository(Path file) {
            delegate = new TicketFileStore(file);
        }

        @Override
        public TicketFileStore.Contents load(Function<String, Optional<User>> userByDni) throws IOException {
            return delegate.load(userByDni);
        }

        @Override
        public void save(List<IndividualTicket> tickets, List<String> historicalLines) throws IOException {
            if (fail) {
                throw new IOException("disk full");
            }
            delegate.save(tickets, historicalLines);
        }
    }

    @TempDir
    Path dir;
    private FlakyTicketRepository ticketRepo;
    private ParkController park;

    @BeforeEach
    void setUp() throws IOException {
        configure();
        park.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        park.createTicket("T002", "pedro@correo.com", OCT_1);
        park.buySpeedPasses("SP01", "T001", "10");
        park.buySpecialPass("SPC1", "T001");
        park.buySpeedPasses("SP02", "T002", "1");
    }

    private void configure() throws IOException {
        UserManager users = UserManager.configure(new UserFileStore(dir.resolve("users.txt")), new BCryptPasswordHasher(4));
        GroupManager groups = GroupManager.configure(new GroupFileStore(dir.resolve("groups.txt")), users);
        ticketRepo = new FlakyTicketRepository(dir.resolve("tickets.txt"));
        TicketManager tickets = TicketManager.configure(ticketRepo,
                new GroupTicketFileStore(dir.resolve("grouptickets.txt")), users, groups);
        AttractionManager attractions = AttractionManager.configure(new AttractionFileStore(dir.resolve("attractions.txt")));
        PassManager passes = PassManager.configure(new PassFileStore(dir.resolve("passes.txt")), tickets);
        park = ParkController.configure(users, groups, tickets, attractions, passes, new InvoiceDirector(), OCT_1);
    }

    private List<String> passLines() throws IOException {
        return Files.readAllLines(dir.resolve("passes.txt"));
    }

    @Test
    void individualCascadeRemovesTicketAndPasses() throws IOException {
        Result r = park.deleteTicket("T001");
        assertEquals("OK: Ticket T001 deleted. 11 pass(es) deleted.", r.toString());
        assertTrue(park.passes().getPasses().stream().noneMatch(p -> p.getTicket().getId().equals("T001")));
        configure();
        assertTrue(park.tickets().find("T001").isEmpty());
        assertEquals(List.of("SPEED;SP02-1;T002;8.00;false"), passLines());
    }

    @Test
    void groupCascadeRemovesPassesConstituentsAndGroupTicket() throws IOException {
        park.createGroupTicket("TG01", "G01", LocalDate.of(2026, 10, 20));
        park.buySpeedPasses("SP03", "TG01-12345678Z", "1");
        park.buySpecialPass("SPC3", "TG01-87654321X");
        Result r = park.deleteGroupTicket("TG01");
        assertTrue(r.toString().endsWith("2 pass(es) deleted."), r.toString());
        configure();
        assertTrue(park.tickets().findGroupTicket("TG01").isEmpty());
        assertTrue(park.tickets().findIndividualTicket("TG01-12345678Z").isEmpty());
        assertTrue(park.passes().find("SP03-1").isEmpty());
        assertTrue(park.passes().find("SPC3").isEmpty());
        assertEquals(12, park.passes().getPasses().size());
    }

    @Test
    void rejectedDeletionKeepsTicketAndPasses() throws IOException {
        List<String> before = passLines();
        assertFalse(park.deleteTicket("T002").success(), "ticket dated today");
        assertFalse(park.deleteTicket("T999").success());
        assertEquals(before, passLines());
        assertTrue(park.passes().find("SP02-1").isPresent());
    }

    @Test
    void failedTicketWriteRestoresPasses() throws IOException {
        List<String> before = passLines();
        ticketRepo.fail = true;
        Result r = park.deleteTicket("T001");
        assertFalse(r.success());
        assertTrue(r.message().contains("disk full"));
        assertTrue(park.tickets().find("T001").isPresent());
        assertEquals(12, park.passes().getPasses().size());
        assertEquals(before.size(), passLines().size());
        assertTrue(passLines().containsAll(before));
    }

    @Test
    void deletingTicketWithoutPassesKeepsLegacyMessage() {
        park.createTicket("T003", "pedro@correo.com", LocalDate.of(2026, 10, 15));
        assertEquals("OK: Ticket T003 deleted.", park.deleteTicket("T003").toString());
    }
}
