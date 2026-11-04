package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.persistence.AttractionFileStorage;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.PassFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CascadeDeletionTest {

    /** Ticket storage that can be told to fail on save. */
    static class FlakyTicketStorage extends TicketFileStorage {
        boolean fail;

        FlakyTicketStorage(Path file) {
            super(file);
        }

        @Override
        public void save(Collection<IndividualTicket> tickets) {
            if (fail) {
                throw new UncheckedIOException(new IOException("disk full"));
            }
            super.save(tickets);
        }
    }

    /** Group-ticket storage that can be told to fail on save. */
    static class FlakyGroupTicketStorage extends GroupTicketFileStorage {
        boolean fail;

        FlakyGroupTicketStorage(Path file) {
            super(file);
        }

        @Override
        public void save(Collection<GroupTicket> tickets) {
            if (fail) {
                throw new UncheckedIOException(new IOException("disk full"));
            }
            super.save(tickets);
        }
    }

    @TempDir
    Path dir;
    FlakyTicketStorage ticketStorage;
    FlakyGroupTicketStorage groupTicketStorage;
    ParkController park;
    PassManager passes;
    TicketManager tickets;

    @BeforeEach
    void setUp() {
        ParkSession.resetControllers();
        open();
        park.createUser("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        park.createUser("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "1", "1234567890123456");
        park.createGroup("G01", "Familia");
        park.addGroupMember("G01", "ana@correo.com");
        park.addGroupMember("G01", "pedro@correo.com");
        park.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        park.createGroupTicket("TG01", "G01", LocalDate.of(2026, 10, 15));
        park.buySpeedPasses("PK", "T001", 10);
        park.buySpecialPass("SX1", "T001");
        park.buySpeedPasses("PA", "TG01-1", 1);
        park.buySpecialPass("SXG", "TG01-2");
        park.buySpeedPasses("KEEP", "T001", 1);
    }

    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    private void open() {
        UserManager users = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")), new BCryptPasswordHasher(4));
        GroupManager groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        ticketStorage = new FlakyTicketStorage(dir.resolve("tickets.txt"));
        groupTicketStorage = new FlakyGroupTicketStorage(dir.resolve("group-tickets.txt"));
        tickets = TicketManager.initialize(ticketStorage, groupTicketStorage, users, groups);
        AttractionManager attractions = AttractionManager.initialize(new AttractionFileStorage(dir.resolve("a.txt")));
        passes = PassManager.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
        park = ParkController.initialize(users, groups, tickets, attractions, passes, new InvoiceDirector(),
                LocalDate.of(2026, 10, 1));
    }

    private void restart() {
        ParkSession.resetControllers();
        open();
    }

    private List<String> passesFile() throws IOException {
        return Files.readAllLines(dir.resolve("passes.txt"));
    }

    @Test
    void ticketDeletionRemovesItsPassesOnly() {
        assertEquals(14, passes.getPasses().size());
        park.deleteTicket("T001");
        assertNull(tickets.find("T001"));
        assertEquals(2, passes.getPasses().size(), "only the group constituents' passes remain");
        restart();
        assertNull(tickets.find("T001"));
        assertEquals(2, passes.getPasses().size());
        assertNotNull(passes.find("PA"));
    }

    @Test
    void groupTicketDeletionRemovesConstituentPassesConstituentsAndGroupTicket() {
        park.deleteGroupTicket("TG01");
        assertNull(tickets.findGroupTicket("TG01"));
        assertNull(tickets.findIndividualTicket("TG01-1"));
        assertNull(passes.find("PA"));
        assertNull(passes.find("SXG"));
        assertEquals(12, passes.getPasses().size());
        restart();
        assertNull(tickets.findGroupTicket("TG01"));
        assertEquals(12, passes.getPasses().size());
    }

    @Test
    void rejectedDeletionKeepsPasses() {
        park.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("T001"));
        assertThrows(IllegalArgumentException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("TG01-1"));
        assertEquals(14, passes.getPasses().size());
    }

    @Test
    void failedTicketSaveLeavesNoPartialDeletion() throws IOException {
        List<String> before = passesFile();
        ticketStorage.fail = true;
        assertThrows(UncheckedIOException.class, () -> park.deleteTicket("T001"));
        assertNotNull(tickets.find("T001"));
        assertEquals(14, passes.getPasses().size());
        assertEquals(before, passesFile(), "passes file restored byte for byte");
        ticketStorage.fail = false;
        restart();
        assertNotNull(tickets.find("T001"));
        assertTrue(passes.find("PK-1").isFor(tickets.find("T001")));
    }

    @Test
    void failedGroupTicketSaveLeavesNoPartialDeletion() throws IOException {
        List<String> before = passesFile();
        groupTicketStorage.fail = true;
        assertThrows(UncheckedIOException.class, () -> park.deleteGroupTicket("TG01"));
        assertNotNull(tickets.findGroupTicket("TG01"));
        assertNotNull(passes.find("PA"));
        assertEquals(before, passesFile());
    }

    @Test
    void passesAndTypesAreReconstructedAfterRestart() {
        park.setCurrentDate(LocalDate.of(2026, 10, 15));
        park.createAttraction("A04", "Especial", 5, AttractionType.SPECIAL);
        park.enterAttraction("A04", "T001", false);
        restart();
        park.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertTrue(passes.find("SX1").isConsumed());
        assertEquals("8.00", passes.find("PK-3").getPrice().toPlainString());
        assertEquals("40.00", passes.find("SX1").getPrice().toPlainString());
        assertTrue(!passes.find("PK-3").isConsumed());
        assertEquals("SPECIAL", passes.find("SXG").kind());
        assertTrue(passes.find("SXG").isFor(tickets.findIndividualTicket("TG01-2")), "constituent linkage restored");
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").type());
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A04", "T001", false),
                "consumed Special Pass stays consumed after restart");
    }
}
