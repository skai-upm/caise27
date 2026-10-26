package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.persistence.FileGroupRepository;
import es.upm.parque.persistence.FileTicketRepository;
import es.upm.parque.persistence.FileUserRepository;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupTicketControllerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;
    private SystemDate date;
    private UserController users;
    private GroupController groups;
    private TicketController tickets;

    @BeforeEach
    void setUp() {
        date = new SystemDate();
        date.set(OCT_1);
        users = UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
        users.create("Luis", "Perez", "11111111H", "luis@correo.com", "Luis3$", "634567890", "1111222233334444");
        restart();
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
    }

    private void restart() {
        users = UserController.getInstance().configure(new FileUserRepository(dir.resolve("users.txt")));
        groups = GroupController.getInstance().configure(new FileGroupRepository(dir.resolve("groups.txt")), users);
        tickets = TicketController.getInstance().configure(new FileTicketRepository(dir.resolve("tickets.txt"), dir.resolve("grouptickets.txt")),
                users, groups, date);
    }

    @Test
    void purchaseGeneratesOneTicketPerMember() {
        GroupTicket gt = tickets.createGroupTicket("TG01", "G01", OCT_15);
        assertEquals("80.00", gt.getPrice().toPlainString());
        assertEquals(2, gt.getTickets().size());
        IndividualTicket first = (IndividualTicket) tickets.findById("TG01-1");
        assertSame(users.findByEmail("ana@correo.com"), first.getUser());
        assertEquals("40.00", first.getPrice().toPlainString());
        assertEquals(OCT_15, first.getDate());
        assertSame(groups.findById("G01"), gt.getGroup());
    }

    @Test
    void rejectsInvalidPurchases() {
        groups.create("G02", "Vacio");
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G99", OCT_15));
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG01", "G02", OCT_15));
        assertThrows(IllegalArgumentException.class,
                () -> tickets.createGroupTicket("TG01", "G01", OCT_1.minusDays(1)));
        tickets.create("T001", "luis@correo.com", OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("T001", "G01", OCT_15));
        tickets.create("TG05-1", "luis@correo.com", OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.createGroupTicket("TG05", "G01", OCT_15));
        assertNull(tickets.findById("TG05"));
    }

    @Test
    void groupIsLockedUntilTheTicketDatePasses() {
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        date.set(OCT_15);
        assertThrows(IllegalArgumentException.class, () -> groups.addMember("G01", "luis@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.removeMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
        assertThrows(IllegalArgumentException.class, () -> users.delete("ana@correo.com", "Clave1!"));
        date.set(OCT_15.plusDays(1));
        groups.addMember("G01", "luis@correo.com");
        assertEquals(2, ((GroupTicket) tickets.findById("TG01")).getTickets().size());
        groups.removeMember("G01", "ana@correo.com");
        users.delete("ana@correo.com", "Clave1!");
        groups.delete("G01");
        assertNotNull(tickets.findById("TG01"));
    }

    @Test
    void deletionCascadesOnlyForFutureGroupTickets() {
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.delete("TG01-1"));
        date.set(OCT_15);
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG01"));
        assertNotNull(tickets.findById("TG01-2"));
        date.set(OCT_1);
        tickets.deleteGroupTicket("TG01");
        assertNull(tickets.findById("TG01"));
        assertNull(tickets.findById("TG01-1"));
        assertNull(tickets.findById("TG01-2"));
        assertThrows(IllegalArgumentException.class, () -> tickets.deleteGroupTicket("TG01"));
        groups.addMember("G01", "luis@correo.com");
    }

    @Test
    void groupTicketsAreRestored() {
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        restart();
        GroupTicket gt = (GroupTicket) tickets.findById("TG01");
        assertEquals("80.00", gt.getPrice().toPlainString());
        assertSame(groups.findById("G01"), gt.getGroup());
        assertSame(tickets.findById("TG01-1"), gt.getTickets().get(0));
        assertSame(users.findByEmail("pedro@correo.com"), gt.getUsers().get(1));
        assertTrue(tickets.hasCurrentOrFutureGroupTickets(groups.findById("G01")));
        assertThrows(IllegalArgumentException.class, () -> groups.delete("G01"));
    }

    @Test
    void deletedGroupIsRestoredAsDetached() {
        tickets.createGroupTicket("TG01", "G01", OCT_15);
        date.set(OCT_15.plusDays(1));
        groups.delete("G01");
        restart();
        GroupTicket gt = (GroupTicket) tickets.findById("TG01");
        assertEquals("G01", gt.getGroup().getId());
        assertEquals(FileTicketRepository.DELETED_GROUP_NAME, gt.getGroup().getName());
    }
}
