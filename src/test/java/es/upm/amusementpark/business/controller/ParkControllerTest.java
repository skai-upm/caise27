package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.OperationException;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.AttractionFileRepository;
import es.upm.amusementpark.persistence.GroupFileRepository;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.TicketFileRepository;
import es.upm.amusementpark.persistence.UserFileRepository;
import es.upm.amusementpark.presentation.AppBootstrap;

public class ParkControllerTest {

    private static final String[] DNIS = {
        "12345678Z", "87654321X", "11111111H", "22222222J", "33333333P", "44444444A",
        "55555555K", "66666666Q", "77777777B", "88888888Y", "99999999R", "10000000Z"
    };

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    public void setUp() throws IOException {
        park = AppBootstrap.create(new StoragePaths(dir), LocalDate.of(2026, 10, 1));
    }

    private ParkController restart() throws IOException {
        return AppBootstrap.create(new StoragePaths(dir), park.getSystemDate());
    }

    private static String email(int i) {
        return "user" + i + "@correo.com";
    }

    private void createUsers(int n) throws OperationException {
        for (int i = 0; i < n; i++) {
            park.createUser("Name" + i, "Surname" + i, DNIS[i], email(i), "Clave1!", "600000000", "1234567890123456");
        }
    }

    private void createGroupOf(String groupId, int n) throws OperationException {
        createUsers(n);
        park.createGroup(groupId, "Group " + groupId);
        for (int i = 0; i < n; i++) {
            park.addToGroup(groupId, email(i));
        }
    }

    // --- system date and price

    @Test
    public void defaultStartDateIsToday() throws IOException {
        StoragePaths paths = new StoragePaths(dir);
        assertEquals(LocalDate.now(), new ParkController(new UserFileRepository(paths.users()),
                new GroupFileRepository(paths.groups()), new TicketFileRepository(paths.tickets()),
                new AttractionFileRepository(paths.attractions())).getSystemDate());
    }

    @Test
    public void systemDateCanMoveBothWays() throws OperationException {
        park.setSystemDate("2026-09-15");
        assertEquals(LocalDate.of(2026, 9, 15), park.getSystemDate());
        park.setSystemDate("2026-01-01");
        assertEquals(LocalDate.of(2026, 1, 1), park.getSystemDate());
    }

    @Test
    public void invalidSystemDateKeepsPrevious() {
        for (String bad : new String[] {"15/09/2026", "2026-02-30", "2026-9-15", "", "+02026-09-15"}) {
            assertThrows(OperationException.class, () -> park.setSystemDate(bad));
        }
        assertEquals(LocalDate.of(2026, 10, 1), park.getSystemDate());
    }

    @Test
    public void priceDefaultsToFortyAndCanChange() throws OperationException {
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        park.setBasePrice("60");
        assertEquals("60.00", park.getBasePrice().toPlainString());
        assertThrows(OperationException.class, () -> park.setBasePrice("abc"));
        assertThrows(OperationException.class, () -> park.setBasePrice("-5"));
        assertEquals("60.00", park.getBasePrice().toPlainString());
    }

    // --- groups

    @Test
    public void groupLifecycle() throws Exception {
        createUsers(2);
        park.createGroup("G01", "Familia Garcia");
        assertThrows(OperationException.class, () -> park.createGroup("G01", "Otro"));
        assertThrows(OperationException.class, () -> park.createGroup("G02", ""));
        park.addToGroup("G01", email(0));
        assertThrows(OperationException.class, () -> park.addToGroup("G01", email(0)));
        assertThrows(OperationException.class, () -> park.addToGroup("G01", "nadie@correo.com"));
        assertThrows(OperationException.class, () -> park.addToGroup("G99", email(0)));
        assertThrows(OperationException.class, () -> park.removeFromGroup("G01", email(1)));
        park.addToGroup("G01", email(1));
        park.removeFromGroup("G01", email(1));
        Group g = park.getGroups().find("G01");
        assertEquals(1, g.getMembers().size());
        assertSame(park.getUsers().findByEmail(email(0)), g.getMembers().get(0));
        park.deleteGroup("G01");
        assertNull(park.getGroups().find("G01"));
        assertNotNull(park.getUsers().findByEmail(email(0)));
    }

    @Test
    public void groupMembershipRestoredWithSameUserObjects() throws Exception {
        createGroupOf("G01", 2);
        ParkController second = restart();
        Group g = second.getGroups().find("G01");
        assertEquals("Group G01", g.getName());
        assertEquals(2, g.getMembers().size());
        assertSame(second.getUsers().findByEmail(email(0)), g.getMembers().get(0));
        assertEquals(2, second.getUsers().getUsers().size());
    }

    @Test
    public void groupReflectsUserUpdates() throws Exception {
        createGroupOf("G01", 1);
        park.updateUser(email(0), "Clave1!", "NAME", "Renamed");
        assertEquals("Renamed", park.getGroups().find("G01").getMembers().get(0).getName());
    }

    // --- individual tickets

    @Test
    public void createTicketUsesCurrentPrice() throws Exception {
        createUsers(1);
        Ticket t1 = park.createTicket("T001", email(0), "2026-10-15");
        park.setBasePrice("60.00");
        Ticket t2 = park.createTicket("T002", email(0), "2026-10-15");
        assertEquals("40.00", t1.getPrice().toPlainString());
        assertEquals("60.00", t2.getPrice().toPlainString());
        assertSame(park.getUsers().findByEmail(email(0)), ((IndividualTicket) t1).getUser());
    }

    @Test
    public void createTicketRejections() throws Exception {
        createUsers(1);
        park.createTicket("T001", email(0), "2026-10-01");
        assertThrows(OperationException.class, () -> park.createTicket("T001", email(0), "2026-10-15"));
        assertThrows(OperationException.class, () -> park.createTicket("T002", "nadie@correo.com", "2026-10-15"));
        assertThrows(OperationException.class, () -> park.createTicket("T002", email(0), "15-10-2026"));
        assertThrows(OperationException.class, () -> park.createTicket("T002", email(0), "2026-09-30"));
        assertEquals(1, park.getTickets().getTickets().size());
    }

    @Test
    public void ticketsPersistWithRecordedPriceAndPriceResets() throws Exception {
        createUsers(1);
        park.setBasePrice("60.00");
        park.createTicket("T001", email(0), "2026-10-15");
        ParkController second = restart();
        Ticket t = second.getTickets().find("T001");
        assertEquals("60.00", t.getPrice().toPlainString());
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertSame(second.getUsers().findByEmail(email(0)), t.getUsers().get(0));
        assertEquals("40.00", second.getBasePrice().toPlainString());
    }

    @Test
    public void onlyFutureTicketsCanBeDeleted() throws Exception {
        createUsers(1);
        park.createTicket("PAST", email(0), "2026-10-09");
        park.createTicket("TODAY", email(0), "2026-10-10");
        park.createTicket("FUTURE", email(0), "2026-10-11");
        park.setSystemDate("2026-10-10");
        assertThrows(OperationException.class, () -> park.deleteTicket("PAST"));
        assertThrows(OperationException.class, () -> park.deleteTicket("TODAY"));
        park.deleteTicket("FUTURE");
        assertThrows(OperationException.class, () -> park.deleteTicket("NOPE"));
        assertNull(park.getTickets().find("FUTURE"));
        assertEquals(2, restart().getTickets().getTickets().size());
    }

    // --- user deletion rules

    @Test
    public void futureAndTodayTicketsBlockUserDeletion() throws Exception {
        createUsers(1);
        park.createTicket("T001", email(0), "2026-10-15");
        assertThrows(OperationException.class, () -> park.deleteUser(email(0), "Clave1!"));
        park.setSystemDate("2026-10-15");
        assertThrows(OperationException.class, () -> park.deleteUser(email(0), "Clave1!"));
        assertNotNull(park.getUsers().findByEmail(email(0)));
        assertNotNull(park.getTickets().find("T001"));
    }

    @Test
    public void historicalTicketDoesNotBlockDeletionAndIsKept() throws Exception {
        createUsers(1);
        park.createTicket("T001", email(0), "2026-10-15");
        park.setSystemDate("2026-10-16");
        park.deleteUser(email(0), "Clave1!");
        assertNull(park.getUsers().findByEmail(email(0)));

        ParkController second = restart();
        Ticket t = second.getTickets().find("T001");
        assertNotNull(t);
        User holder = t.getUsers().get(0);
        assertEquals(DNIS[0], holder.getDni());
        assertEquals(email(0), holder.getEmail());
        assertNull(second.getUsers().findByEmail(email(0)));
    }

    @Test
    public void deletedUserLeavesGroups() throws Exception {
        createGroupOf("G01", 2);
        park.deleteUser(email(0), "Clave1!");
        assertEquals(1, park.getGroups().find("G01").getMembers().size());
        assertEquals(1, restart().getGroups().find("G01").getMembers().size());
    }

    // --- group tickets

    @ParameterizedTest
    @CsvSource({"2,80.00", "3,108.00", "5,180.00", "6,192.00", "10,320.00", "11,220.00"})
    public void groupDiscountBrackets(int members, String expected) throws Exception {
        createGroupOf("G01", members);
        GroupTicket gt = park.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(expected, gt.getPrice().toPlainString());
        assertEquals(members, gt.getTickets().size());
    }

    @Test
    public void groupTicketComposition() throws Exception {
        createGroupOf("G01", 3);
        GroupTicket gt = park.createGroupTicket("TG01", "G01", "2026-10-15");
        assertSame(park.getGroups().find("G01"), gt.getGroup());
        assertEquals("TG01-1", gt.getTickets().get(0).getId());
        assertSame(park.getUsers().findByEmail(email(2)), gt.getTickets().get(2).getUser());
        assertEquals("36.00", gt.getTickets().get(0).getPrice().toPlainString());
        assertSame(gt.getTickets().get(1), park.getTickets().find("TG01-2"));

        park.setSystemDate("2026-10-16");
        park.removeFromGroup("G01", email(0));
        assertEquals(3, gt.getUsers().size());
    }

    @Test
    public void failedGroupTicketCreatesNothing() throws Exception {
        createGroupOf("G01", 2);
        park.createTicket("TG01-2", email(0), "2026-10-15");
        park.createGroup("EMPTY", "Empty");
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG01", "G01", "2026-10-15"));
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG02", "EMPTY", "2026-10-15"));
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG03", "G99", "2026-10-15"));
        assertThrows(OperationException.class, () -> park.createGroupTicket("TG04", "G01", "2026-09-01"));
        assertNull(park.getTickets().find("TG01"));
        assertNull(park.getTickets().find("TG01-1"));
        assertEquals(1, park.getTickets().getTickets().size());
    }

    @Test
    public void groupLockFollowsSystemDate() throws Exception {
        createGroupOf("G01", 2);
        createUsersFrom(2, 1);
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        park.setSystemDate("2026-10-15");
        assertThrows(OperationException.class, () -> park.addToGroup("G01", email(2)));
        assertThrows(OperationException.class, () -> park.removeFromGroup("G01", email(0)));
        assertThrows(OperationException.class, () -> park.deleteGroup("G01"));
        assertThrows(OperationException.class, () -> park.deleteUser(email(0), "Clave1!"));

        park.setSystemDate("2026-10-16");
        park.addToGroup("G01", email(2));
        park.removeFromGroup("G01", email(0));
        park.deleteUser(email(0), "Clave1!");
        park.deleteGroup("G01");
        assertNull(park.getGroups().find("G01"));
        assertNotNull(park.getTickets().find("TG01"));
    }

    private void createUsersFrom(int start, int n) throws OperationException {
        for (int i = start; i < start + n; i++) {
            park.createUser("Name" + i, "Surname" + i, DNIS[i], email(i), "Clave1!", "600000000", "1234567890123456");
        }
    }

    @Test
    public void groupTicketDeletion() throws Exception {
        createGroupOf("G01", 3);
        park.createGroupTicket("TG01", "G01", "2026-10-01");
        park.createGroupTicket("TG02", "G01", "2026-10-15");
        assertThrows(OperationException.class, () -> park.deleteGroupTicket("TG01"));
        assertThrows(OperationException.class, () -> park.deleteTicket("TG02"));
        assertThrows(OperationException.class, () -> park.deleteTicket("TG02-1"));
        assertThrows(OperationException.class, () -> park.deleteGroupTicket("TG02-1"));
        assertNotNull(park.getTickets().find("TG01-3"));

        park.deleteGroupTicket("TG02");
        assertNull(park.getTickets().find("TG02"));
        assertNull(park.getTickets().find("TG02-1"));
        assertNull(park.getTickets().find("TG02-3"));
    }

    @Test
    public void groupTicketsSurviveRestart() throws Exception {
        createGroupOf("G01", 3);
        park.createGroupTicket("TG01", "G01", "2026-10-15");
        ParkController second = restart();
        Ticket t = second.getTickets().find("TG01");
        GroupTicket gt = assertInstanceOf(GroupTicket.class, t);
        assertEquals("108.00", gt.getPrice().toPlainString());
        assertSame(second.getGroups().find("G01"), gt.getGroup());
        assertEquals(3, gt.getTickets().size());
        assertSame(second.getUsers().findByEmail(email(1)), gt.getTickets().get(1).getUser());
        assertTrue(second.getTickets().isGroupLocked(gt.getGroup(), second.getSystemDate()));
    }

    // --- attractions

    @Test
    public void attractionEntryExitAndStatus() throws Exception {
        createGroupOf("G01", 3);
        createUsersFrom(3, 1);
        park.createAttraction("A01", "Tornado", "20");
        park.createTicket("T001", email(3), "2026-10-01");
        park.createGroupTicket("TG01", "G01", "2026-10-01");
        park.enterAttraction("A01", "T001");
        park.enterAttraction("A01", "TG01");
        Attraction a = park.attractionStatus("A01");
        assertEquals(4, a.getOccupancy());
        assertEquals(16, a.getAvailable());
        assertEquals(1, park.exitAttraction("A01", "T001"));
        assertEquals(3, a.getOccupancy());
        assertEquals(17, a.getAvailable());
        assertThrows(OperationException.class, () -> park.exitAttraction("A01", "T001"));
    }

    @Test
    public void groupEntryIsAtomic() throws Exception {
        createGroupOf("G01", 4);
        park.createAttraction("A01", "Mini", "3");
        park.createGroupTicket("TG01", "G01", "2026-10-01");
        assertThrows(OperationException.class, () -> park.enterAttraction("A01", "TG01"));
        assertEquals(0, park.attractionStatus("A01").getOccupancy());
    }

    @Test
    public void entryRequiresTicketForToday() throws Exception {
        createUsers(1);
        park.createAttraction("A01", "Tornado", "20");
        park.createTicket("T001", email(0), "2026-10-02");
        assertThrows(OperationException.class, () -> park.enterAttraction("A01", "T001"));
        park.setSystemDate("2026-10-02");
        park.enterAttraction("A01", "T001");
        assertThrows(OperationException.class, () -> park.enterAttraction("A01", "T001"));
        park.setSystemDate("2026-10-05");
        assertEquals(1, park.exitAttraction("A01", "T001"));
    }

    @Test
    public void invalidAttractions() throws Exception {
        park.createAttraction("A01", "Tornado", "20");
        assertThrows(OperationException.class, () -> park.createAttraction("A01", "Otra", "5"));
        assertThrows(OperationException.class, () -> park.createAttraction("A02", "Otra", "0"));
        assertThrows(OperationException.class, () -> park.createAttraction("A02", "Otra", "x"));
        assertThrows(OperationException.class, () -> park.createAttraction("A02", "", "5"));
        assertThrows(OperationException.class, () -> park.attractionStatus("A99"));
        assertThrows(OperationException.class, () -> park.enterAttraction("A01", "NOPE"));
    }

    @Test
    public void attractionsRestartEmpty() throws Exception {
        createUsers(1);
        park.createAttraction("A01", "Tornado", "20");
        park.createTicket("T001", email(0), "2026-10-01");
        park.enterAttraction("A01", "T001");
        Attraction a = restart().getAttractions().find("A01");
        assertEquals("Tornado", a.getName());
        assertEquals(20, a.getCapacity());
        assertEquals(0, a.getOccupancy());
    }

    @Test
    public void deletedUserLeavesAttractions() throws Exception {
        createUsers(1);
        park.createAttraction("A01", "Tornado", "20");
        park.createTicket("T001", email(0), "2026-10-01");
        park.enterAttraction("A01", "T001");
        park.setSystemDate("2026-10-02");
        park.deleteUser(email(0), "Clave1!");
        assertFalse(park.attractionStatus("A01").getPresentUsers().stream()
                .anyMatch(u -> u.getDni().equals(DNIS[0])));
    }

    @Test
    public void discountBrackets() {
        assertEquals(BigDecimal.ZERO, TicketController.discountFor(2));
        assertEquals(new BigDecimal("0.10"), TicketController.discountFor(3));
        assertEquals(new BigDecimal("0.10"), TicketController.discountFor(5));
        assertEquals(new BigDecimal("0.20"), TicketController.discountFor(6));
        assertEquals(new BigDecimal("0.20"), TicketController.discountFor(10));
        assertEquals(new BigDecimal("0.50"), TicketController.discountFor(11));
    }
}
