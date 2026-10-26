package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.Park;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.TextFileGroupTicketRepository;
import es.upm.parque.persistence.TextFilePassRepository;
import es.upm.parque.persistence.TextFileTicketRepository;

class TicketManagerTest {

    @TempDir
    Path dir;

    private Park park;
    private UserManager users;
    private SystemDate today;
    private TicketManager tickets;

    @BeforeEach
    void setUp() {
        park = new Park(dir, LocalDate.of(2026, 10, 1));
        users = park.users;
        today = park.date;
        tickets = park.tickets;
        users.create(Fixtures.ana());
        users.create(Fixtures.pedro());
    }

    private Group familyWithAnaAndPedro() {
        park.groups.create("G01", "Familia Garcia");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G01", "pedro@correo.com");
        return park.groups.find("G01");
    }

    @Test
    void createsTicketAtCurrentPrice() {
        assertEquals("OK TICKET CREATED -> 40.00", tickets.create("T001", "ana@correo.com", "2026-10-15"));
        IndividualTicket t001 = tickets.findIndividual("T001");
        assertSame(users.findByEmail("ana@correo.com"), t001.getHolder());
        assertEquals(LocalDate.of(2026, 10, 15), t001.getDate());
        assertEquals(new BigDecimal("40.00"), t001.getPrice());
        assertSame(t001, tickets.find("T001"));
        assertEquals("OK TICKET CREATED -> 40.00", tickets.create("T002", "ana@correo.com", "2026-10-01"));
    }

    @Test
    void rejectsInvalidPurchases() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(UserManager.EMPTY_FIELD, tickets.create(" ", "ana@correo.com", "2026-10-15"));
        assertEquals(TicketManager.DUPLICATE_TICKET, tickets.create("T001", "ana@correo.com", "2026-10-16"));
        assertEquals(GroupManager.USER_NOT_FOUND, tickets.create("T002", "nadie@correo.com", "2026-10-15"));
        assertEquals(TicketManager.INVALID_DATE, tickets.create("T002", "ana@correo.com", "15/10/2026"));
        assertEquals(TicketManager.PAST_DATE, tickets.create("T002", "ana@correo.com", "2026-09-30"));
        assertNull(tickets.find("T002"));
    }

    @Test
    void priceChangesOnlyAffectNewTickets() {
        assertEquals(TicketManager.INITIAL_PRICE, tickets.getPrice());
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals("OK SYSTEM PRICE -> 60.00", tickets.setPrice("60"));
        assertEquals("OK TICKET CREATED -> 60.00", tickets.create("T002", "ana@correo.com", "2026-10-15"));
        assertEquals(new BigDecimal("40.00"), tickets.find("T001").getPrice());
        assertEquals(TicketManager.INVALID_PRICE, tickets.setPrice("gratis"));
        assertEquals(TicketManager.INVALID_PRICE, tickets.setPrice("0"));
        assertEquals(TicketManager.INVALID_PRICE, tickets.setPrice("-5"));
        assertEquals(new BigDecimal("60.00"), tickets.getPrice());
    }

    @Test
    void refundsOnlyFutureTickets() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(TicketManager.TICKET_NOT_FOUND, tickets.delete("T999"));
        today.set(LocalDate.of(2026, 10, 15));
        assertEquals(TicketManager.NOT_REFUNDABLE, tickets.delete("T001"));
        today.set(LocalDate.of(2026, 10, 16));
        assertEquals(TicketManager.NOT_REFUNDABLE, tickets.delete("T001"));
        assertNotNull(tickets.find("T001"));
        today.set(LocalDate.of(2026, 10, 14));
        assertEquals(TicketManager.TICKET_DELETED, tickets.delete("T001"));
        assertNull(tickets.find("T001"));
    }

    @Test
    void activeTicketsDependOnSystemDate() {
        User ana = users.findByEmail("ana@correo.com");
        assertFalse(tickets.hasActiveTickets(ana));
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertTrue(tickets.hasActiveTickets(ana));
        today.set(LocalDate.of(2026, 10, 15));
        assertTrue(tickets.hasActiveTickets(ana));
        today.set(LocalDate.of(2026, 10, 16));
        assertFalse(tickets.hasActiveTickets(ana));
    }

    @Test
    void ticketsSurviveRestart() {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        Park restarted = new Park(dir, LocalDate.of(2026, 11, 1));
        IndividualTicket t001 = restarted.tickets.findIndividual("T001");
        assertSame(restarted.users.findByEmail("ana@correo.com"), t001.getHolder());
        assertEquals(TicketManager.INITIAL_PRICE, restarted.tickets.getPrice());
    }

    @Test
    void createsGroupTicketWithOneTicketPerMember() {
        Group group = familyWithAnaAndPedro();
        assertEquals("OK GROUP TICKET CREATED -> 80.00", tickets.createGroupTicket("TG01", "G01", "2026-10-15"));
        GroupTicket tg01 = tickets.findGroupTicket("TG01");
        assertSame(group, tg01.getGroup());
        assertSame(tg01, tickets.find("TG01"));
        assertEquals(List.of(users.findByEmail("ana@correo.com"), users.findByEmail("pedro@correo.com")),
                tg01.getVisitors());
        assertNull(tickets.findIndividual("TG01-1"));
    }

    @Test
    void groupTicketUsesCurrentPriceAndDiscount() {
        park.groups.create("G01", "Grande");
        for (int i = 0; i < 3; i++) {
            users.create(Fixtures.numbered(i));
            park.groups.addMember("G01", "u" + i + "@correo.com");
        }
        tickets.setPrice("50.00");
        assertEquals("OK GROUP TICKET CREATED -> 135.00", tickets.createGroupTicket("TG01", "G01", "2026-10-15"));
    }

    @Test
    void rejectsInvalidGroupTickets() {
        familyWithAnaAndPedro();
        park.groups.create("G02", "Vacio");
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(UserManager.EMPTY_FIELD, tickets.createGroupTicket("", "G01", "2026-10-15"));
        assertEquals(TicketManager.DUPLICATE_TICKET, tickets.createGroupTicket("T001", "G01", "2026-10-15"));
        assertEquals(GroupManager.GROUP_NOT_FOUND, tickets.createGroupTicket("TG01", "G99", "2026-10-15"));
        assertEquals(TicketManager.EMPTY_GROUP, tickets.createGroupTicket("TG01", "G02", "2026-10-15"));
        assertEquals(TicketManager.INVALID_DATE, tickets.createGroupTicket("TG01", "G01", "mañana"));
        assertEquals(TicketManager.PAST_DATE, tickets.createGroupTicket("TG01", "G01", "2026-09-01"));
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(TicketManager.DUPLICATE_TICKET, tickets.create("TG01", "ana@correo.com", "2026-10-15"));
        assertEquals(TicketManager.DUPLICATE_TICKET, tickets.create("TG01-1", "ana@correo.com", "2026-10-15"));
    }

    @Test
    void groupLockedWhileGroupTicketIsCurrent() {
        Group group = familyWithAnaAndPedro();
        assertFalse(tickets.isGroupLocked(group));
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertTrue(tickets.isGroupLocked(group));
        today.set(LocalDate.of(2026, 10, 15));
        assertTrue(tickets.isGroupLocked(group));
        today.set(LocalDate.of(2026, 10, 16));
        assertFalse(tickets.isGroupLocked(group));
    }

    @Test
    void generatedTicketsProtectMembers() {
        familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertTrue(tickets.hasActiveTickets(users.findByEmail("pedro@correo.com")));
        today.set(LocalDate.of(2026, 10, 16));
        assertFalse(tickets.hasActiveTickets(users.findByEmail("pedro@correo.com")));
    }

    @Test
    void compositionIsFrozenAtPurchase() {
        familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        today.set(LocalDate.of(2026, 10, 16));
        users.create(new User("Luis", "Ruiz", "11111111H", "luis@correo.com", "Clave1!", "6", "1111222233334444"));
        park.groups.addMember("G01", "luis@correo.com");
        assertEquals(2, tickets.findGroupTicket("TG01").getTickets().size());
    }

    @Test
    void refundsGroupTicketsOnlyInTheFuture() {
        Group group = familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(TicketManager.TICKET_NOT_FOUND, tickets.deleteGroupTicket("TG99"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, tickets.delete("TG01"));
        assertEquals(TicketManager.TICKET_NOT_FOUND, tickets.delete("TG01-1"));
        today.set(LocalDate.of(2026, 10, 15));
        assertEquals(TicketManager.NOT_REFUNDABLE, tickets.deleteGroupTicket("TG01"));
        today.set(LocalDate.of(2026, 10, 14));
        assertEquals(TicketManager.GROUP_TICKET_DELETED, tickets.deleteGroupTicket("TG01"));
        assertNull(tickets.find("TG01"));
        assertFalse(tickets.isGroupLocked(group));
        assertFalse(tickets.hasActiveTickets(users.findByEmail("ana@correo.com")));
        assertEquals("OK TICKET CREATED -> 40.00", tickets.create("TG01-1", "ana@correo.com", "2026-10-15"));
    }

    @Test
    void groupTicketsSurviveRestart() throws IOException {
        familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(List.of("TG01;2026-10-15;G01;0;12345678Z:40.00,87654321X:40.00"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
        Park restarted = new Park(dir, LocalDate.of(2026, 10, 2));
        GroupTicket tg01 = restarted.tickets.findGroupTicket("TG01");
        assertSame(restarted.groups.find("G01"), tg01.getGroup());
        assertSame(restarted.users.findByEmail("pedro@correo.com"), tg01.getTickets().get(1).getHolder());
        assertEquals(new BigDecimal("80.00"), tg01.getPrice());
        assertTrue(restarted.tickets.isGroupLocked(restarted.groups.find("G01")));
    }

    @Test
    void reportsStorageFailure() throws IOException {
        Path blocked = Files.createFile(dir.resolve("blocked"));
        TicketManager broken = TicketManager.initialize(new TextFileTicketRepository(blocked.resolve("t.txt")),
                new TextFileGroupTicketRepository(blocked.resolve("g.txt")),
                new TextFilePassRepository(blocked.resolve("p.txt")), users, park.groups, today);
        assertEquals(UserManager.STORAGE_FAILURE, broken.create("T001", "ana@correo.com", "2026-10-15"));
    }

    @Test
    void deletingTicketDeletesItsPasses() throws IOException {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.passes.createSpeedPasses("T001", "10");
        park.passes.createSpecialPass("T001");
        assertEquals(11, Files.readAllLines(dir.resolve("passes.txt")).size());
        assertEquals(TicketManager.TICKET_DELETED, tickets.delete("T001"));
        assertTrue(Files.readAllLines(dir.resolve("passes.txt")).isEmpty());
        Park restarted = new Park(dir, LocalDate.of(2026, 10, 1));
        assertNull(restarted.tickets.findAnyIndividual("T001"));
        assertEquals(PassManager.PASS_NOT_FOUND, restarted.passes.deleteSpeedPass("T001-S1"));
    }

    @Test
    void deletingGroupTicketDeletesPassesThenTicketsThenGroupTicket() throws IOException {
        familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.passes.createSpeedPasses("TG01-1", "1");
        park.passes.createSpecialPass("TG01-2");
        IndividualTicket member = tickets.findAnyIndividual("TG01-1");
        assertEquals(TicketManager.GROUP_TICKET_DELETED, tickets.deleteGroupTicket("TG01"));
        assertTrue(member.getPasses().isEmpty());
        assertNull(tickets.findAnyIndividual("TG01-1"));
        assertNull(tickets.findAnyIndividual("TG01-2"));
        assertTrue(Files.readAllLines(dir.resolve("passes.txt")).isEmpty());
        assertTrue(Files.readAllLines(dir.resolve("grouptickets.txt")).isEmpty());
        Park restarted = new Park(dir, LocalDate.of(2026, 10, 1));
        assertNull(restarted.tickets.findGroupTicket("TG01"));
    }

    @Test
    void notRefundableGroupTicketKeepsEverything() {
        familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-01");
        park.passes.createSpecialPass("TG01-2");
        assertEquals(TicketManager.NOT_REFUNDABLE, tickets.deleteGroupTicket("TG01"));
        assertNotNull(tickets.findAnyIndividual("TG01-2").getSpecialPass());
        assertNotNull(tickets.findGroupTicket("TG01"));
    }

    @Test
    void storageFailureRestoresTicketAndPasses() throws IOException {
        tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.passes.createSpeedPasses("T001", "1");
        park.passes.createSpecialPass("T001");
        Files.delete(dir.resolve("passes.txt"));
        Files.createDirectory(dir.resolve("passes.txt"));
        assertEquals(UserManager.STORAGE_FAILURE, tickets.delete("T001"));
        IndividualTicket t001 = tickets.findIndividual("T001");
        assertEquals(2, t001.getPasses().size());
        assertNotNull(t001.getSpecialPass());
    }

    @Test
    void storageFailureRestoresGroupTicketAndPasses() throws IOException {
        familyWithAnaAndPedro();
        tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.passes.createSpeedPasses("TG01-1", "1");
        Files.delete(dir.resolve("passes.txt"));
        Files.createDirectory(dir.resolve("passes.txt"));
        assertEquals(UserManager.STORAGE_FAILURE, tickets.deleteGroupTicket("TG01"));
        assertNotNull(tickets.findGroupTicket("TG01"));
        assertEquals(1, tickets.findAnyIndividual("TG01-1").getSpeedPasses().size());
    }
}
