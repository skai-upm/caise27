package es.upm.park.business.service;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 1);
    static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;
    Path usersFile;
    Path ticketsFile;
    UserManager users;
    TicketManager tickets;
    User ana;

    @BeforeEach
    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @BeforeEach
    void setUp() {
        usersFile = dir.resolve("users.txt");
        ticketsFile = dir.resolve("tickets.txt");
        users = UserManager.initialize(new UserFileStorage(usersFile));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        ana = users.findByEmail("ana@correo.com");
        tickets = newManager();
    }

    private TicketManager newManager() {
        GroupManager groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        return TicketManager.initialize(new TicketFileStorage(ticketsFile),
                new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), users, groups);
    }

    private TicketManager restart() {
        ParkSession.resetControllers();
        users = UserManager.initialize(new UserFileStorage(usersFile));
        return newManager();
    }

    @Test
    void createStoresAndPersists() throws IOException {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        assertEquals(List.of("T001;12345678Z;2026-10-15;40.00"), Files.readAllLines(ticketsFile));
    }

    @Test
    void rejectsPastDateAndDuplicateId() throws IOException {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        List<String> before = Files.readAllLines(ticketsFile);
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T002", ana, LocalDate.of(2026, 9, 30), PRICE, TODAY));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create("T001", ana, LocalDate.of(2026, 10, 20), PRICE, TODAY));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.create(" ", ana, LocalDate.of(2026, 10, 20), PRICE, TODAY));
        assertEquals(before, Files.readAllLines(ticketsFile));
    }

    @Test
    void deleteOnlyFutureTickets() {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", LocalDate.of(2026, 10, 15)));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T001", LocalDate.of(2026, 10, 20)));
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("T999", TODAY));
        tickets.delete("T001", LocalDate.of(2026, 10, 14));
        assertNull(tickets.find("T001"));
        assertTrue(restart().getTickets().isEmpty());
    }

    @Test
    void activeTicketBoundaries() {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        assertTrue(tickets.hasTicketOnOrAfter(ana, LocalDate.of(2026, 10, 1)));
        assertTrue(tickets.hasTicketOnOrAfter(ana, LocalDate.of(2026, 10, 15)));
        assertFalse(tickets.hasTicketOnOrAfter(ana, LocalDate.of(2026, 10, 16)));
    }

    @Test
    void restartLinksSameUserObject() {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        TicketManager again = restart();
        IndividualTicket t = again.find("T001");
        assertSame(users.findByDni("12345678Z"), t.getOwner());
        assertEquals(PRICE, t.getPrice());
        assertTrue(again.hasTicketOnOrAfter(users.findByDni("12345678Z"), TODAY));
    }

    @Test
    void historyKeptAfterOwnerDeletion() {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        users.delete("ana@correo.com", "Clave1!");
        TicketManager again = restart();
        IndividualTicket t = again.find("T001");
        assertNull(t.getOwner());
        assertEquals("12345678Z", t.getOwnerDni());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
    }

    @Test
    void commonAbstractionListsAllTickets() {
        tickets.create("T001", ana, LocalDate.of(2026, 10, 15), PRICE, TODAY);
        TicketManager.reset();
        GroupManager.reset();
        GroupManager groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        TicketManager manager = TicketManager.initialize(new TicketFileStorage(ticketsFile),
                new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), users, groups);
        manager.createGroupTicket("TG01", groups.find("G01"), LocalDate.of(2026, 10, 20), PRICE, TODAY);
        List<Ticket> all = manager.getAllTickets();
        assertEquals(2, all.size());
        assertEquals(new BigDecimal("40.00"), all.get(1).getPrice());
        assertTrue(manager.hasGroupTicketOnOrAfter(groups.find("G01"), LocalDate.of(2026, 10, 20)));
        assertFalse(manager.hasGroupTicketOnOrAfter(groups.find("G01"), LocalDate.of(2026, 10, 21)));
    }
}
