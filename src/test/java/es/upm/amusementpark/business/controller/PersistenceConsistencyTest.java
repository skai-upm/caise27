package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.SimpleInvoiceBuilder;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.TicketRepository;
import es.upm.amusementpark.persistence.AttractionFileRepository;
import es.upm.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.GroupFileRepository;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.TicketFileRepository;
import es.upm.amusementpark.persistence.UserFileRepository;
import es.upm.amusementpark.presentation.AppBootstrap;

public class PersistenceConsistencyTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;

    private ParkController park;
    private FailingTicketRepository ticketRepository;

    /** File repository that can be told to fail its next save. */
    private static final class FailingTicketRepository implements TicketRepository {
        private final TicketFileRepository delegate;
        private boolean failNextSave;

        FailingTicketRepository(Path file) {
            delegate = new TicketFileRepository(file);
        }

        @Override
        public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById)
                throws IOException {
            return delegate.load(userByDni, groupById);
        }

        @Override
        public void save(List<Ticket> tickets) throws IOException {
            if (failNextSave) {
                failNextSave = false;
                throw new IOException("disk full");
            }
            delegate.save(tickets);
        }
    }

    @BeforeEach
    public void setUp() throws IOException, OperationException {
        park = start();
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        park.createGroup("G01", "Familia");
        park.addToGroup("G01", "ana@correo.com");
        park.addToGroup("G01", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", "2026-10-15");
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        park.buySpeedPasses("T001", "10");
        park.buySpecialPass("T001");
        park.buySpeedPasses("TG01-1", "1");
        park.buySpecialPass("TG01-2");
    }

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    /** Wires the controllers by hand so a failing ticket repository can be injected. */
    private ParkController start() throws IOException {
        StoragePaths paths = new StoragePaths(dir);
        ticketRepository = new FailingTicketRepository(paths.tickets());
        UserController users = UserController.initialize(new UserFileRepository(paths.users()),
                new BCryptPasswordHasher(4));
        GroupController groups = GroupController.initialize(new GroupFileRepository(paths.groups()), users);
        TicketController tickets = TicketController.initialize(ticketRepository, users, groups);
        AttractionController attractions = AttractionController.initialize(
                new AttractionFileRepository(paths.attractions()));
        InvoiceController invoices = InvoiceController.initialize(tickets, SimpleInvoiceBuilder::new);
        return ParkController.initialize(users, groups, tickets, attractions, invoices, DAY);
    }

    private ParkController restart() throws IOException {
        AppBootstrap.shutdown();
        return start();
    }

    private String ticketsFile() throws IOException {
        return Files.readString(dir.resolve("tickets.txt"));
    }

    @Test
    public void attractionTypeSurvivesRestart() throws Exception {
        park.createAttraction("A01", "Casa", "5", "SPECIAL");
        park.createAttraction("A02", "Rio", "5");
        ParkController second = restart();
        assertEquals(AttractionType.SPECIAL, second.attractionStatus("A01").getType());
        assertEquals(AttractionType.GENERAL, second.attractionStatus("A02").getType());
        assertEquals(List.of("A01;Casa;5;SPECIAL", "A02;Rio;5;GENERAL"),
                Files.readAllLines(dir.resolve("attractions.txt")));
    }

    @Test
    public void deliveryTwoAttractionLinesLoadAsGeneral() throws Exception {
        AppBootstrap.shutdown();
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Tornado;20", "A02;Bad;x", "A03;Odd;5;VIP"));
        ParkController second = start();
        assertEquals(AttractionType.GENERAL, second.attractionStatus("A01").getType());
        assertEquals(1, second.getAttractions().getAttractions().size());
    }

    @Test
    public void ticketDeletionCascadesPasses() throws Exception {
        park.deleteTicket("T001");
        assertNull(park.getTickets().find("T001"));
        assertFalse(ticketsFile().contains("T001"));
        ParkController second = restart();
        assertNull(second.getTickets().find("T001"));
        assertThrows(OperationException.class, () -> second.refundSpeedPass("T001-SP1"));
    }

    @Test
    public void groupTicketDeletionCascadesPassesAndConstituents() throws Exception {
        park.deleteGroupTicket("TG01");
        String file = ticketsFile();
        assertFalse(file.contains("TG01"));
        assertTrue(file.contains("T001-SPECIAL"));
        ParkController second = restart();
        assertNull(second.getTickets().find("TG01-1"));
        assertNotNull(second.getTickets().find("T001"));
    }

    @Test
    public void failedPreconditionDeletesNothing() throws Exception {
        String before = ticketsFile();
        park.setSystemDate("2026-10-15");
        assertThrows(OperationException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(OperationException.class, () -> park.deleteTicket("T001"));
        assertEquals(1, ((IndividualTicket) park.getTickets().find("TG01-1")).getSpeedPasses().size());
        assertEquals(10, ((IndividualTicket) park.getTickets().find("T001")).getSpeedPasses().size());
        assertEquals(before, ticketsFile());
    }

    @Test
    public void failedWriteLeavesMemoryAndFileUnchanged() throws Exception {
        String before = ticketsFile();
        ticketRepository.failNextSave = true;
        assertThrows(OperationException.class, () -> park.deleteGroupTicket("TG01"));
        assertEquals(before, ticketsFile());
        IndividualTicket first = (IndividualTicket) park.getTickets().find("TG01-1");
        IndividualTicket second = (IndividualTicket) park.getTickets().find("TG01-2");
        assertNotNull(park.getTickets().find("TG01"));
        assertEquals(1, first.getSpeedPasses().size());
        assertNotNull(second.getSpecialPass());

        ticketRepository.failNextSave = true;
        assertThrows(OperationException.class, () -> park.deleteTicket("T001"));
        IndividualTicket t001 = (IndividualTicket) park.getTickets().find("T001");
        assertEquals(10, t001.getSpeedPasses().size());
        assertNotNull(t001.getSpecialPass());
        assertEquals(before, ticketsFile());

        park.deleteGroupTicket("TG01");
        assertFalse(ticketsFile().contains("TG01"));
    }

    @Test
    public void failedWriteUndoesPassConsumptionOnEntry() throws Exception {
        park.createAttraction("A01", "Tornado", "5");
        park.setSystemDate("2026-10-15");
        ticketRepository.failNextSave = true;
        assertThrows(OperationException.class, () -> park.enterAttraction("A01", "T001", true));
        assertEquals(0, park.attractionStatus("A01").getOccupancy());
        assertTrue(((IndividualTicket) park.getTickets().find("T001")).getSpeedPasses().stream()
                .noneMatch(p -> p.isConsumed()));
    }

    @Test
    public void atomicWritesLeaveNoTemporaryFiles() throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            assertEquals(List.of("groups.txt", "tickets.txt", "users.txt"),
                    files.map(p -> p.getFileName().toString()).sorted().toList());
        }
    }
}
