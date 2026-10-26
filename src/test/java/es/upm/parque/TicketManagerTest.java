package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    @TempDir
    Path dir;

    private final SystemDate date = new SystemDate(LocalDate.of(2026, 10, 1));
    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private User ana;

    @BeforeEach
    void setUp() throws IOException {
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        ana = users.findByEmail("ana@correo.com");
        groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
        tickets = newTicketManager();
    }

    private TicketManager newTicketManager() throws IOException {
        return new TicketManager(new TicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")),
                users, groups, date);
    }

    @Test
    void createsTicketAtCurrentPrice() {
        IndividualTicket ticket = tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(new BigDecimal("40.00"), ticket.getPrice());
        assertSame(ana, ticket.getUser());
        assertEquals(LocalDate.of(2026, 10, 15), ticket.getDate());
    }

    @Test
    void createValidations() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertThrows(UserException.class, () -> tickets.create("T001", "ana@correo.com", "2026-10-16"));
        assertThrows(UserException.class, () -> tickets.create("", "ana@correo.com", "2026-10-16"));
        assertThrows(UserException.class, () -> tickets.create("T002", "nadie@correo.com", "2026-10-16"));
        assertThrows(UserException.class, () -> tickets.create("T002", "ana@correo.com", "2026-09-30"));
        assertThrows(IllegalArgumentException.class, () -> tickets.create("T002", "ana@correo.com", "15-10-2026"));
        tickets.create("T002", "ana@correo.com", "2026-10-01");
        assertEquals(2, tickets.getTickets().size());
    }

    @Test
    void priceSettingIsValidatedAndNotPersisted() throws IOException {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        tickets.setCurrentPrice("60.00");
        assertEquals(new BigDecimal("60.00"), tickets.create("T002", "ana@correo.com", "2026-10-15").getPrice());
        assertThrows(UserException.class, () -> tickets.setCurrentPrice("0"));
        assertThrows(UserException.class, () -> tickets.setCurrentPrice("12.345"));
        assertThrows(UserException.class, () -> tickets.setCurrentPrice("abc"));
        assertEquals(new BigDecimal("60.00"), tickets.getCurrentPrice());
        assertEquals(new BigDecimal("40.00"), tickets.find("T001").getPrice());

        TicketManager restarted = newTicketManager();
        assertEquals(new BigDecimal("40.00"), restarted.getCurrentPrice());
        assertEquals(new BigDecimal("60.00"), restarted.find("T002").getPrice());
        assertSame(ana, ((IndividualTicket) restarted.find("T002")).getUser());
    }

    @Test
    void deleteOnlyStrictlyFutureTickets() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        tickets.create("T002", "ana@correo.com", "2026-10-15");
        tickets.delete("T001");
        assertNull(tickets.find("T001"));
        date.set(LocalDate.of(2026, 10, 15));
        assertThrows(UserException.class, () -> tickets.delete("T002"));
        assertThrows(UserException.class, () -> tickets.delete("T999"));
    }

    private Group familyOf(int extraMembers) {
        groups.create("G01", "Familia");
        groups.addMember("G01", "ana@correo.com");
        for (int i = 0; i < extraMembers; i++) {
            String dni = String.format("%08d", i);
            dni += "TRWAGMYFPDXBNJZSQVHLCKE".charAt(i % 23);
            users.create("U" + i, "S", dni, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
            groups.addMember("G01", "u" + i + "@correo.com");
        }
        return groups.find("G01");
    }

    @Test
    void groupTicketGeneratesOneTicketPerMember() {
        Group family = familyOf(2);
        GroupTicket groupTicket = tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(new BigDecimal("108.00"), groupTicket.getPrice());
        assertEquals(3, groupTicket.getTickets().size());
        assertSame(ana, groupTicket.getTickets().get(0).getUser());
        assertSame(family, groupTicket.getGroup());
        assertSame(groupTicket, tickets.findOwner((IndividualTicket) tickets.find("TG01-1")));
        assertTrue(tickets.hasCurrentOrFutureTicket(ana));
    }

    @Test
    void groupTicketValidations() {
        groups.create("G02", "Vacio");
        assertThrows(UserException.class, () -> tickets.createGroupTicket("TG01", "G02", "2026-10-15"));
        assertThrows(UserException.class, () -> tickets.createGroupTicket("TG01", "G99", "2026-10-15"));
        familyOf(0);
        tickets.create("TG01-1", "ana@correo.com", "2026-10-15");
        assertThrows(UserException.class, () -> tickets.createGroupTicket("TG01", "G01", "2026-10-15"));
        assertThrows(UserException.class, () -> tickets.createGroupTicket("TG02", "G01", "2026-09-01"));
        assertEquals(1, tickets.getTickets().size());
    }

    @Test
    void groupLockFollowsTheSystemDate() {
        Group family = familyOf(1);
        assertFalse(tickets.isGroupLocked(family));
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertTrue(tickets.isGroupLocked(family));
        date.set(LocalDate.of(2026, 10, 15));
        assertTrue(tickets.isGroupLocked(family));
        date.set(LocalDate.of(2026, 10, 16));
        assertFalse(tickets.isGroupLocked(family));
    }

    @Test
    void deleteGroupTicketRemovesItsTickets() {
        familyOf(1);
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertThrows(UserException.class, () -> tickets.delete("TG01-1"));
        assertThrows(UserException.class, () -> tickets.delete("TG01"));
        assertThrows(UserException.class, () -> tickets.deleteGroupTicket("TG01-1"));
        date.set(LocalDate.of(2026, 10, 15));
        assertThrows(UserException.class, () -> tickets.deleteGroupTicket("TG01"));
        date.set(LocalDate.of(2026, 10, 14));
        tickets.deleteGroupTicket("TG01");
        assertTrue(tickets.getTickets().isEmpty());
        assertFalse(tickets.isGroupLocked(groups.find("G01")));
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        familyOf(1);
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        TicketManager restarted = newTicketManager();
        GroupTicket restored = (GroupTicket) restarted.find("TG01");
        assertEquals(new BigDecimal("80.00"), restored.getPrice());
        assertSame(restarted.find("TG01-1"), restored.getTickets().get(0));
        assertTrue(restarted.isGroupLocked(groups.find("G01")));
    }

    @Test
    void currentOrFutureTicketsFollowTheSystemDate() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertTrue(tickets.hasCurrentOrFutureTicket(ana));
        date.set(LocalDate.of(2026, 10, 15));
        assertTrue(tickets.hasCurrentOrFutureTicket(ana));
        date.set(LocalDate.of(2026, 10, 16));
        assertFalse(tickets.hasCurrentOrFutureTicket(ana));
    }
}
