package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryThreePersistenceTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, "SYSTEM DATE;2026-10-01", "TICKET CREATE;T001;ana@correo.com;2026-10-15");
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    private IndividualTicket ticket(String id) {
        return (IndividualTicket) TicketController.getInstance().findById(id);
    }

    private String passesFile() throws IOException {
        Path file = dir.resolve("passes.txt");
        return Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8) : "";
    }

    @Test
    void passesAreRestoredWithPriceTicketAndStatus() throws IOException {
        s.run("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001", "SPEEDPASS CREATE;TG01-2;1",
                "ATTRACTION CREATE;GEN;General;20", "SYSTEM DATE;2026-10-15", "ATTRACTION ENTER;GEN;T001;SPEED");
        s.restart();
        IndividualTicket t = ticket("T001");
        assertEquals(10, t.getSpeedPasses().size());
        SpeedPass first = t.getSpeedPasses().get(0);
        assertEquals("T001-SP1", first.getId());
        assertEquals(new BigDecimal("8.00"), first.getPrice());
        assertTrue(first.isConsumed());
        assertFalse(t.getSpeedPasses().get(1).isConsumed());
        assertEquals("T001-SPECIAL", t.getSpecialPass().getId());
        assertEquals(new BigDecimal("40.00"), t.getSpecialPass().getPrice());
        assertFalse(t.getSpecialPass().isConsumed());
        assertEquals(new BigDecimal("7.20"), ticket("TG01-2").getSpeedPasses().get(0).getPrice());
        assertEquals("OK SPEEDPASS CREATED -> 8.00 [T001-SP11]", s.run("SYSTEM DATE;2026-10-01", "SPEEDPASS CREATE;T001;1"));
    }

    @Test
    void attractionTypesAreRestoredAndLegacyRecordsAreGeneral() throws IOException {
        s.run("ATTRACTION CREATE;SPE;Especial;5;SPECIAL", "ATTRACTION CREATE;GRP;Grupo;5;GROUP");
        Files.writeString(dir.resolve("attractions.txt"),
                Files.readString(dir.resolve("attractions.txt")) + "OLD;Antigua;7\n");
        s.restart();
        AttractionController attractions = AttractionController.getInstance();
        assertEquals(AttractionType.SPECIAL, attractions.findById("SPE").getType());
        assertEquals(AttractionType.GROUP, attractions.findById("GRP").getType());
        assertEquals(AttractionType.GENERAL, attractions.findById("OLD").getType());
        assertEquals(7, attractions.findById("OLD").getCapacity());
    }

    @Test
    void ticketDeletionCascadesToPasses() throws IOException {
        s.run("SPEEDPASS CREATE;T001;10", "SPECIALPASS CREATE;T001");
        assertTrue(passesFile().contains("T001-SP1;"));
        assertEquals("OK: ticket T001 deleted with its 11 pass(es)", s.run("TICKET DELETE;T001"));
        assertFalse(passesFile().contains("T001"));
        s.restart();
        assertNull(ticket("T001"));
        assertTrue(s.run("SPEEDPASS DELETE;T001-SP2").startsWith("ERROR"));
    }

    @Test
    void groupTicketDeletionCascadesToConstituentPasses() throws IOException {
        s.run("SPEEDPASS CREATE;TG01-1;10", "SPECIALPASS CREATE;TG01-3", "SPEEDPASS CREATE;T001;1");
        assertEquals("OK: group ticket TG01 deleted with its 3 tickets and 11 pass(es)", s.run("GROUPTICKET DELETE;TG01"));
        String passes = passesFile();
        assertFalse(passes.contains("TG01"));
        assertTrue(passes.contains("T001-SP1"));
        s.restart();
        assertNull(TicketController.getInstance().findById("TG01-1"));
        assertEquals(1, ticket("T001").getSpeedPasses().size());
    }

    @Test
    void rejectedDeletionDeletesNothing() throws IOException {
        s.run("SPEEDPASS CREATE;TG01-1;1", "SPEEDPASS CREATE;T001;1", "SYSTEM DATE;2026-10-15");
        String before = passesFile();
        assertTrue(s.run("GROUPTICKET DELETE;TG01").startsWith("ERROR"));
        assertTrue(s.run("TICKET DELETE;T001").startsWith("ERROR"));
        assertEquals(before, passesFile());
        assertNotNull(ticket("TG01-1").findUnusedSpeedPass());
        assertNotNull(ticket("T001").findUnusedSpeedPass());
    }

    @Test
    void saveFailureLeavesNoPartialDeletion() throws IOException {
        User ana = UserController.getInstance().findByEmail("ana@correo.com");
        IndividualTicket t = new IndividualTicket("T9", LocalDate.of(2026, 10, 20), ana, new BigDecimal("40.00"));
        t.addSpeedPass();
        FailingRepositories repos = new FailingRepositories(List.of(t));
        TicketController tickets = TicketController.getInstance();
        tickets.configure(repos, repos, UserController.getInstance(), GroupController.getInstance());
        repos.failTickets = true;
        assertTrue(tickets.deleteIndividual("T9", LocalDate.of(2026, 10, 1)).startsWith("ERROR"));
        assertSame(t, tickets.findById("T9"));
        assertEquals(1, t.getSpeedPasses().size());
        assertEquals(List.of("T9-SP1"), repos.savedPassIds);
    }

    @Test
    void reloadedGroupTicketKeepsGroupReference() throws IOException {
        s.restart();
        Group group = GroupController.getInstance().findById("G01");
        GroupTicket gt = (GroupTicket) TicketController.getInstance().findById("TG01");
        assertSame(group, gt.getGroup());
    }

    /** In-memory repositories whose ticket save can be made to fail. */
    private static final class FailingRepositories implements TicketRepository, PassRepository {

        private final List<IndividualTicket> initial;
        private boolean failTickets;
        private List<String> savedPassIds = new ArrayList<>();

        FailingRepositories(List<IndividualTicket> initial) {
            this.initial = initial;
        }

        @Override
        public List<IndividualTicket> loadTickets(List<User> users) {
            return initial;
        }

        @Override
        public List<GroupTicket> loadGroupTickets(List<User> users, List<Group> groups) {
            return List.of();
        }

        @Override
        public void save(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) throws IOException {
            if (failTickets) {
                failTickets = false;
                throw new IOException("disk full");
            }
        }

        @Override
        public void load(List<IndividualTicket> tickets) {
            // passes are already attached to the in-memory tickets
        }

        @Override
        public void save(List<IndividualTicket> tickets) {
            savedPassIds = new ArrayList<>();
            for (IndividualTicket t : tickets) {
                t.getPasses().forEach(p -> savedPassIds.add(p.getId()));
            }
        }
    }
}
