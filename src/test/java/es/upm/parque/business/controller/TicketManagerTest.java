package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestSupport;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.PassStatus;
import es.upm.parque.business.model.SpecialPass;
import es.upm.parque.business.model.SpeedPass;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.GroupStorage;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.TicketStorage;
import es.upm.parque.persistence.UserStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private User ana;
    private User pedro;

    @BeforeEach
    void setUp() {
        users = UserManager.getInstance().configure(new UserStorage(dir.resolve("users.txt")), TestSupport.FAST_HASHER);
        ana = users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
            "1234567890123456");
        pedro = users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
            "9876543210987654");
        groups = GroupManager.getInstance().configure(users, new GroupStorage(dir.resolve("groups.txt")));
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        tickets = reload(users, groups);
    }

    private TicketManager reload(UserManager u, GroupManager g) {
        return TicketManager.getInstance().configure(u, g,
            new TicketStorage(dir.resolve("tickets.txt"), dir.resolve("group_tickets.txt"),
                dir.resolve("passes.txt")));
    }

    @Test
    void createStoresTicketWithUserReference() {
        IndividualTicket t = tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertSame(ana, t.getOwner());
        assertEquals(PRICE, t.getPrice());
        assertEquals(OCT_15, t.getDate());
    }

    @Test
    void createRejectsInvalidRequests() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class,
            () -> tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class,
            () -> tickets.create("T002", "nadie@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class,
            () -> tickets.create("T003", "ana@correo.com", OCT_1.minusDays(1), OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class, () -> tickets.create(" ", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertEquals(1, tickets.getTickets().size());
        assertNotNull(tickets.create("T004", "ana@correo.com", OCT_1, OCT_1, PRICE));
    }

    @Test
    void deleteOnlyFutureTickets() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", OCT_15.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T999", OCT_1));
        tickets.delete("T001", OCT_15.minusDays(1));
        assertNull(tickets.find("T001"));
    }

    @Test
    void currentOrFutureTicketsAreDetected() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_1));
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_15));
        assertFalse(tickets.hasTicketOnOrAfter(ana, OCT_15.plusDays(1)));
    }

    @Test
    void restartRestoresTicketsAndKeepsHistory() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        UserManager reloadedUsers = UserManager.getInstance().configure(new UserStorage(dir.resolve("users.txt")), TestSupport.FAST_HASHER);
        GroupManager reloadedGroups = GroupManager.getInstance().configure(reloadedUsers, new GroupStorage(dir.resolve("groups.txt")));
        TicketManager reloaded = reload(reloadedUsers, reloadedGroups);
        IndividualTicket t = (IndividualTicket) reloaded.find("T001");
        assertSame(reloadedUsers.findByDni("12345678Z"), t.getOwner());

        User deleted = reloadedUsers.delete("ana@correo.com", "Clave1!");
        reloaded.detachOwner(deleted);
        assertNull(t.getOwner());
        IndividualTicket again = (IndividualTicket) reload(reloadedUsers, reloadedGroups).find("T001");
        assertEquals("12345678Z", again.getOwnerDni());
        assertNull(again.getOwner());
    }

    @Test
    void groupTicketGeneratesInternalTicketsThatProtectUsers() {
        GroupTicket gt = tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        assertEquals(new BigDecimal("80.00"), gt.getPrice());
        assertEquals(2, gt.getComponents().size());
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_15));
        assertTrue(tickets.hasTicketOnOrAfter(pedro, OCT_1));
        assertFalse(tickets.hasTicketOnOrAfter(pedro, OCT_15.plusDays(1)));
    }

    @Test
    void groupTicketValidation() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.create("TG09-2", "ana@correo.com", OCT_15, OCT_1, PRICE);
        groups.create("G02", "Vacio");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("T001", "G01", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG09", "G01", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G99", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G02", OCT_15, OCT_1, PRICE));
        assertThrows(IllegalArgumentException.class,
            () -> tickets.createGroupTicket("TG01", "G01", OCT_1.minusDays(1), OCT_1, PRICE));
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class, () -> tickets.create("TG01-1", "ana@correo.com", OCT_15, OCT_1, PRICE));
    }

    @Test
    void groupLockFollowsTheDate() {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        Group g01 = groups.find("G01");
        assertTrue(tickets.isGroupLocked(g01, OCT_1));
        assertTrue(tickets.isGroupLocked(g01, OCT_15));
        assertFalse(tickets.isGroupLocked(g01, OCT_15.plusDays(1)));
    }

    @Test
    void compositionIsFixedAtPurchase() {
        GroupTicket gt = tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        groups.removeMember("G01", "pedro@correo.com");
        assertEquals(2, gt.getComponents().size());
    }

    @Test
    void deleteGroupTicketRemovesInternalTickets() {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("TG01-1", OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("TG01", OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG01", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG99", OCT_1));
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("T001", OCT_1));
        tickets.deleteGroupTicket("TG01", OCT_1);
        assertNull(tickets.find("TG01"));
        assertFalse(tickets.hasTicketOnOrAfter(pedro, OCT_1));
        assertNotNull(tickets.create("TG01-1", "pedro@correo.com", OCT_15, OCT_1, PRICE));
    }

    @Test
    void groupTicketsSurviveRestartAndGroupDeletion() {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        UserManager reloadedUsers = UserManager.getInstance().configure(new UserStorage(dir.resolve("users.txt")), TestSupport.FAST_HASHER);
        GroupManager reloadedGroups = GroupManager.getInstance().configure(reloadedUsers, new GroupStorage(dir.resolve("groups.txt")));
        TicketManager reloaded = reload(reloadedUsers, reloadedGroups);
        GroupTicket gt = (GroupTicket) reloaded.find("TG01");
        assertSame(reloadedGroups.find("G01"), gt.getGroup());
        assertSame(reloadedUsers.findByDni("87654321X"), gt.getComponents().get(1).getOwner());
        assertEquals(new BigDecimal("80.00"), gt.getPrice());

        Group g01 = reloadedGroups.find("G01");
        reloadedGroups.delete("G01");
        reloaded.detachGroup(g01);
        assertNull(gt.getGroup());
        assertEquals("G01", ((GroupTicket) reload(reloadedUsers, reloadedGroups).find("TG01")).getGroupId());
    }

    @Test
    void passesAreSoldForIndividualAndInternalTickets() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        List<SpeedPass> pack = tickets.buySpeedPasses("T001", 10, OCT_1);
        assertEquals(10, pack.size());
        assertEquals(new BigDecimal("8.00"), pack.get(0).getPrice());
        assertEquals("TG01-2-S1", tickets.buySpeedPasses("TG01-2", 1, OCT_1).get(0).getId());
        SpecialPass special = tickets.buySpecialPass("TG01-1", OCT_15);
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertThrows(IllegalArgumentException.class, () -> tickets.buySpecialPass("TG01-1", OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.buySpeedPasses("T001", 5, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.buySpeedPasses("TG01", 1, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.buySpeedPasses("T999", 1, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.buySpeedPasses("T001", 1, OCT_15.plusDays(1)));
    }

    @Test
    void refundsFollowTheTicketDate() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.buySpeedPasses("T001", 1, OCT_1);
        tickets.buySpecialPass("T001", OCT_1);
        assertThrows(IllegalArgumentException.class, () -> tickets.refundPass("T001-SPECIAL", SpeedPass.class, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.refundPass("T001-S1", SpeedPass.class, OCT_15));
        assertEquals(PassStatus.REFUNDED, tickets.refundPass("T001-S1", SpeedPass.class, OCT_1).getStatus());
        assertThrows(IllegalArgumentException.class, () -> tickets.refundPass("T001-S1", SpeedPass.class, OCT_1));
        assertThrows(IllegalArgumentException.class, () -> tickets.refundPass("X-S9", SpeedPass.class, OCT_1));
        assertEquals(PassStatus.REFUNDED, tickets.refundPass("T001-SPECIAL", SpecialPass.class, OCT_1).getStatus());
    }

    @Test
    void passesSurviveRestartAndDieWithTheirTicket() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        tickets.buySpeedPasses("T001", 1, OCT_1).get(0).consume();
        tickets.buySpecialPass("T001", OCT_1);
        tickets.refundPass("T001-SPECIAL", SpecialPass.class, OCT_1);
        tickets.buySpeedPasses("TG01-1", 1, OCT_1);

        TicketManager reloaded = reload(users, groups);
        IndividualTicket t = (IndividualTicket) reloaded.find("T001");
        assertEquals(PassStatus.CONSUMED, t.getSpeedPasses().get(0).getStatus());
        assertEquals(PassStatus.REFUNDED, t.getSpecialPass().getStatus());
        assertThrows(IllegalArgumentException.class, () -> reloaded.buySpecialPass("T001", OCT_1));
        assertEquals(1, ((IndividualTicket) reloaded.findAdmission("TG01-1")).getSpeedPasses().size());

        reloaded.delete("T001", OCT_1);
        reloaded.deleteGroupTicket("TG01", OCT_1);
        assertTrue(reload(users, groups).getTickets().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> reloaded.refundPass("TG01-1-S1", SpeedPass.class, OCT_1));
    }

    /** Repository that delegates to the file storage but can be told to fail on save. */
    private static final class FailingRepository implements TicketRepository {
        private final TicketRepository delegate;
        private boolean failing;

        FailingRepository(TicketRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Ticket> load(Function<String, User> userByDni, Function<String, Group> groupById) {
            return delegate.load(userByDni, groupById);
        }

        @Override
        public void save(Collection<Ticket> all) {
            if (failing) {
                throw new UncheckedIOException(new IOException("disk full"));
            }
            delegate.save(all);
        }
    }

    @Test
    void groupTicketDeletionCascadesAndFailedSaveLeavesEverything() {
        FailingRepository repository = new FailingRepository(
            new TicketStorage(dir.resolve("tickets.txt"), dir.resolve("group_tickets.txt"), dir.resolve("passes.txt")));
        TicketManager manager = TicketManager.getInstance().configure(users, groups, repository);
        manager.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        manager.buySpeedPasses("T001", 1, OCT_1);
        manager.buySpecialPass("T001", OCT_1);
        GroupTicket gt = manager.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        manager.buySpeedPasses("TG01-1", 1, OCT_1);

        assertThrows(IllegalArgumentException.class, () -> manager.deleteGroupTicket("TG01", OCT_15));
        assertEquals(1, gt.getComponents().get(0).getSpeedPasses().size());

        repository.failing = true;
        assertThrows(UncheckedIOException.class, () -> manager.deleteGroupTicket("TG01", OCT_1));
        assertThrows(UncheckedIOException.class, () -> manager.delete("T001", OCT_1));
        assertEquals(List.of("T001", "TG01"), manager.getTickets().stream().map(Ticket::getId).toList());
        assertEquals(2, gt.getComponents().size());
        assertEquals(1, gt.getComponents().get(0).getSpeedPasses().size());
        assertEquals(2, ((IndividualTicket) manager.find("T001")).getPasses().size());
        assertTrue(manager.hasTicketOnOrAfter(ana, OCT_1));

        repository.failing = false;
        manager.deleteGroupTicket("TG01", OCT_1);
        assertTrue(gt.getComponents().isEmpty());
        manager.delete("T001", OCT_1);
        assertTrue(manager.getTickets().isEmpty());
        assertTrue(reload(users, groups).getTickets().isEmpty());
    }

    @Test
    void internalTicketPassesAndPricesAreReconstructed() throws IOException {
        tickets.createGroupTicket("TG01", "G01", OCT_15, OCT_1, PRICE);
        tickets.buySpeedPasses("TG01-1", 1, OCT_1).get(0).consume();
        tickets.recordPassUsage();
        IndividualTicket internal = (IndividualTicket) reload(users, groups).findAdmission("TG01-1");
        assertEquals(new BigDecimal("8.00"), internal.getSpeedPasses().get(0).getPrice());
        assertEquals(PassStatus.CONSUMED, internal.getSpeedPasses().get(0).getStatus());
        assertEquals(List.of("SPEED;TG01-1-S1;TG01-1;8.00;CONSUMED"),
            Files.readAllLines(dir.resolve("passes.txt")));
    }
}
