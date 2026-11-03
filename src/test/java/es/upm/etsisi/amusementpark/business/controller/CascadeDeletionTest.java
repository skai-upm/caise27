package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.persistence.AttractionFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.PassFileStorage;
import es.upm.etsisi.amusementpark.persistence.TicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.TicketRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;
import es.upm.etsisi.amusementpark.presentation.CommandProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CascadeDeletionTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        p.process(TestData.CREATE_PEDRO);
        p.process("GROUP CREATE;G01;Amigos");
        p.process("GROUP ADD;G01;ana@correo.com");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("SYSTEM DATE;2026-10-01");
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("TICKET CREATE;T002;ana@correo.com;2026-10-15");
        p.process("SPEEDPASS CREATE;S1;T001;10");
        p.process("SPECIALPASS CREATE;SP1;T001");
        p.process("SPEEDPASS CREATE;S2;T002;1");
        p.process("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        p.process("SPEEDPASS CREATE;S3;TG001-1;1");
        p.process("SPECIALPASS CREATE;SP3;TG001-2");
    }

    private List<String> passIds() {
        return PassController.getInstance().getPasses().stream().map(Pass::getId).toList();
    }

    @Test
    void ticketDeletionRemovesItsPasses() throws IOException {
        assertEquals("OK: Ticket T001 deleted", p.process("TICKET DELETE;T001"));
        assertEquals(List.of("S2-1", "S3-1", "SP3"), passIds());
        App.createProcessor(dir);
        assertEquals(List.of("S2-1", "S3-1", "SP3"), passIds());
        assertTrue(Files.readString(dir.resolve("passes.txt")).lines().noneMatch(l -> l.contains(";T001;")));
    }

    @Test
    void groupTicketDeletionRemovesPassesConstituentsAndGroupTicket() {
        assertEquals("OK: Group ticket TG001 deleted", p.process("GROUPTICKET DELETE;TG001"));
        assertEquals(12, passIds().size());
        assertTrue(passIds().stream().noneMatch(id -> id.startsWith("S3") || id.equals("SP3")));
        assertTrue(TicketController.getInstance().findIndividualTicket("TG001-1").isEmpty());
        assertTrue(TicketController.getInstance().findGroupTicket("TG001").isEmpty());
        App.createProcessor(dir);
        assertEquals(12, passIds().size());
    }

    @Test
    void rejectedDeletionsKeepEverything() {
        p.process("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("TICKET DELETE;T001").startsWith("ERROR:"));
        assertTrue(p.process("GROUPTICKET DELETE;TG001").startsWith("ERROR:"));
        assertTrue(p.process("TICKET DELETE;T999").startsWith("ERROR:"));
        assertTrue(p.process("TICKET DELETE;TG001-1").startsWith("ERROR:"));
        assertEquals(14, passIds().size());
    }

    /** Ticket repository that fails when saving, to interrupt the cascade after the passes were removed. */
    static class FailingTicketRepository implements TicketRepository {
        private final TicketFileStorage delegate;
        boolean fail;

        FailingTicketRepository(Path file) {
            delegate = new TicketFileStorage(file);
        }

        @Override
        public List<Ticket> load(List<User> users) {
            return delegate.load(users);
        }

        @Override
        public void save(List<Ticket> tickets) {
            if (fail) {
                throw new IllegalStateException("disk full");
            }
            delegate.save(tickets);
        }
    }

    @Test
    void failureDuringTheCascadeRestoresThePasses() throws IOException {
        List<String> passesFile = Files.readAllLines(dir.resolve("passes.txt"));
        UserController users = UserController.initialize(new UserFileStorage(dir.resolve("users.txt")), TestData.HASHER);
        GroupController groups = GroupController.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        FailingTicketRepository failing = new FailingTicketRepository(dir.resolve("tickets.txt"));
        TicketController tickets = TicketController.initialize(failing,
                new GroupTicketFileStorage(dir.resolve("grouptickets.txt")), users, groups);
        PassController passes = PassController.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
        ParkController park = ParkController.initialize(users, groups, tickets,
                AttractionController.initialize(new AttractionFileStorage(dir.resolve("a.txt"))), passes,
                LocalDate.of(2026, 10, 1));
        failing.fail = true;
        assertThrows(IllegalStateException.class, () -> park.deleteTicket("T001"));
        assertEquals(14, passIds().size());
        assertTrue(tickets.find("T001").isPresent());
        assertEquals(passesFile, Files.readAllLines(dir.resolve("passes.txt")));
        failing.fail = false;
        park.deleteTicket("T001");
        assertEquals(3, passIds().size());
    }

    @Test
    void consumedStatusAndAttractionTypesSurviveRestart() {
        p.process("SYSTEM DATE;2026-10-15");
        p.process("ATTRACTION CREATE;A04;Vip;5;SPECIAL");
        p.process("ATTRACTION ENTER;A04;T001");
        CommandProcessor restarted = App.createProcessor(dir);
        Pass sp1 = PassController.getInstance().find("SP1").orElseThrow();
        assertTrue(sp1.isConsumed());
        assertEquals("40.00", sp1.getPrice().toPlainString());
        assertEquals("T001", sp1.getTicket().getId());
        assertEquals("SPECIAL", AttractionController.getInstance().get("A04").getType().name());
        restarted.process("SYSTEM DATE;2026-10-15");
        assertEquals("ERROR: Ticket T001 has no unused Special Pass", restarted.process("ATTRACTION ENTER;A04;T001"));
    }
}
