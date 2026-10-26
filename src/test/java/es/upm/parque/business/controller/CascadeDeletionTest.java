package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.repository.TicketRepository;
import es.upm.parque.business.security.BCryptPasswordEncoder;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FilePassRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CascadeDeletionTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private SystemDate date;
    private TicketController tickets;
    private PassController passes;
    private final List<String> deletionOrder = new ArrayList<>();
    private boolean failSaves;

    /** File repository that records what each save contains and can be told to fail. */
    private class RecordingRepository implements TicketRepository {
        private final FileTicketRepository delegate =
                new FileTicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt"));

        @Override
        public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) {
            return delegate.load(userByDni, groupById);
        }

        @Override
        public void save(List<Ticket> all) {
            if (failSaves) {
                throw new IllegalStateException("disk full");
            }
            delegate.save(all);
        }
    }

    @BeforeEach
    void setUp() {
        date = new SystemDate();
        date.set(LocalDate.of(2026, 10, 1));
        configure(new RecordingRepository());
        UserController users = UserController.getInstance();
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        GroupController groups = GroupController.getInstance();
        groups.create("G01", "Amigos");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        passes.buySpeedPasses("TG01-1", 10);
        passes.buySpecialPass("TG01-2");
    }

    private void configure(TicketRepository ticketRepository) {
        UserController users = UserController.getInstance()
                .configure(new FileUserRepository(dir.resolve("users.txt")), new BCryptPasswordEncoder(4));
        GroupController groups = GroupController.getInstance()
                .configure(new FileGroupRepository(dir.resolve("groups.txt")), users);
        tickets = TicketController.getInstance().configure(ticketRepository, users, groups, date);
        passes = PassController.getInstance()
                .configure(new FilePassRepository(dir.resolve("passes.txt")), tickets, date);
    }

    @Test
    void groupTicketDeletionRemovesPassesThenTicketsThenGroupTicket() throws Exception {
        IndividualTicket first = (IndividualTicket) tickets.findById("TG01-1");
        tickets.addDeletionListener(deleted -> {
            deleted.forEach(t -> deletionOrder.add(t.getId() + " passes=" + t.getPasses().size()));
            deletionOrder.add("TG01 present=" + (tickets.findById("TG01") != null));
        });
        tickets.deleteGroupTicket("TG01");
        assertEquals(List.of("TG01-1 passes=0", "TG01-2 passes=0", "TG01 present=false"), deletionOrder);
        assertEquals(0, first.getPasses().size());
        assertNull(tickets.findById("TG01-1"));
        assertEquals("", Files.readString(dir.resolve("passes.txt")));
        configure(new RecordingRepository());
        assertNull(tickets.findById("TG01"));
        assertNull(tickets.findById("TG01-2"));
    }

    @Test
    void rejectedDeletionChangesNothing() {
        date.set(OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG01"));
        assertEquals(10, ((IndividualTicket) tickets.findById("TG01-1")).getSpeedPasses().size());
        assertNotNull(((IndividualTicket) tickets.findById("TG01-2")).getSpecialPass());
    }

    @Test
    void storageFailureIsRolledBack() throws Exception {
        String passesBefore = Files.readString(dir.resolve("passes.txt"));
        failSaves = true;
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> tickets.deleteGroupTicket("TG01"));
        assertTrue(e.getMessage().contains("undone"), e.getMessage());
        assertNotNull(tickets.findById("TG01"));
        assertEquals(10, ((IndividualTicket) tickets.findById("TG01-1")).getSpeedPasses().size());
        assertNotNull(((IndividualTicket) tickets.findById("TG01-2")).getSpecialPass());
        assertEquals(passesBefore, Files.readString(dir.resolve("passes.txt")));
        failSaves = false;
        tickets.deleteGroupTicket("TG01");
        assertNull(tickets.findById("TG01"));
    }
}
