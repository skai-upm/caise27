package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final LocalDate OCT_16 = LocalDate.of(2026, 10, 16);
    private static final BigDecimal PRICE = new BigDecimal("40.00");

    @TempDir
    Path dir;

    private Path usersFile;
    private Path ticketsFile;
    private UserManager users;
    private TicketManager tickets;

    @BeforeEach
    void setUp() throws Exception {
        usersFile = dir.resolve("users.txt");
        ticketsFile = dir.resolve("tickets.txt");
        users = new UserManager(new UserFileRepository(usersFile));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        tickets = newManager(users);
    }

    private TicketManager newManager(UserManager u) throws Exception {
        return new TicketManager(new TicketFileRepository(ticketsFile),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), u,
                new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), u));
    }

    private User ana() {
        return users.findByEmail("ana@correo.com").orElseThrow();
    }

    @Test
    void createsForTodayOrLaterWithCapturedPrice() throws Exception {
        IndividualTicket t = tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertSame(ana(), t.getUser().orElseThrow());
        assertEquals(PRICE, t.getPrice());
        tickets.create("T002", "ana@correo.com", OCT_1, OCT_1, new BigDecimal("60.00"));
        assertEquals(PRICE, tickets.find("T001").orElseThrow().getPrice());
    }

    @Test
    void rejectsUnknownUserPastDateDuplicateAndBlankId() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        String before = Files.readString(ticketsFile);
        assertThrows(UserException.class, () -> tickets.create("T002", "nadie@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.create("T002", "ana@correo.com", OCT_1.minusDays(1), OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.create(" ", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertEquals(1, tickets.getTickets().size());
        assertEquals(before, Files.readString(ticketsFile));
    }

    @Test
    void deleteOnlyFutureTickets() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertThrows(UserException.class, () -> tickets.delete("T001", OCT_15));
        assertThrows(UserException.class, () -> tickets.delete("T001", OCT_16));
        assertThrows(UserException.class, () -> tickets.delete("T999", OCT_1));
        assertTrue(tickets.find("T001").isPresent());
        tickets.delete("T001", OCT_15.minusDays(1));
        assertFalse(tickets.find("T001").isPresent());
    }

    @Test
    void activeTicketBoundary() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        assertTrue(tickets.hasTicketOnOrAfter(ana(), OCT_1));
        assertTrue(tickets.hasTicketOnOrAfter(ana(), OCT_15));
        assertFalse(tickets.hasTicketOnOrAfter(ana(), OCT_16));
    }

    @Test
    void detachedTicketsSurviveAsHistory() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        User ana = users.delete("ana@correo.com", "Clave1!");
        tickets.detachUser(ana);
        assertFalse(tickets.hasTicketOnOrAfter(ana, OCT_1));

        UserManager restartedUsers = new UserManager(new UserFileRepository(usersFile));
        TicketManager restarted = newManager(restartedUsers);
        IndividualTicket t = restarted.find("T001").orElseThrow();
        assertTrue(t.getUser().isEmpty());
        assertEquals("12345678Z", t.getOwnerDni());
        assertEquals(OCT_15, t.getDate());
    }

    @Test
    void restartRestoresTicketsLinkedToSameUser() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.create("T002", "ana@correo.com", OCT_16, OCT_1, new BigDecimal("60.00"));
        UserManager restartedUsers = new UserManager(new UserFileRepository(usersFile));
        TicketManager restarted = newManager(restartedUsers);
        User restartedAna = restartedUsers.findByEmail("ana@correo.com").orElseThrow();
        assertSame(restartedAna, restarted.find("T001").orElseThrow().getUser().orElseThrow());
        assertEquals(new BigDecimal("60.00"), restarted.find("T002").orElseThrow().getPrice());
        assertTrue(restarted.hasTicketOnOrAfter(restartedAna, OCT_16));
    }

    private Group groupWithAnaAndPedro() throws Exception {
        users.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        GroupManager groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users);
        groups.create("G01", "Familia Garcia");
        groups.addMember("G01", "ana@correo.com");
        groups.addMember("G01", "pedro@correo.com");
        return groups.find("G01").orElseThrow();
    }

    @Test
    void groupTicketRulesAndSharedIds() throws Exception {
        Group g = groupWithAnaAndPedro();
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        GroupTicket gt = tickets.createGroupTicket("TG01", g, OCT_15, OCT_1, PRICE);
        assertEquals(new BigDecimal("80.00"), gt.getPrice());
        assertEquals(2, tickets.allTickets().size());

        assertThrows(UserException.class, () -> tickets.createGroupTicket("T001", g, OCT_15, OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.create("TG01", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.create("TG01-12345678Z", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.createGroupTicket("TG02", g, OCT_1.minusDays(1), OCT_1, PRICE));
        assertThrows(UserException.class, () -> tickets.createGroupTicket("TG02", new Group("G02", "Vacio"), OCT_15, OCT_1, PRICE));

        assertTrue(tickets.groupHasTicketOnOrAfter(g, OCT_15));
        assertFalse(tickets.groupHasTicketOnOrAfter(g, OCT_16));
        User pedro = users.findByEmail("pedro@correo.com").orElseThrow();
        assertTrue(tickets.hasTicketOnOrAfter(pedro, OCT_15));
        assertFalse(tickets.hasTicketOnOrAfter(pedro, OCT_16));

        assertThrows(UserException.class, () -> tickets.delete("TG01", OCT_1));
        assertThrows(UserException.class, () -> tickets.delete("TG01-87654321X", OCT_1));
        assertThrows(UserException.class, () -> tickets.deleteGroupTicket("T001", OCT_1));
        assertThrows(UserException.class, () -> tickets.deleteGroupTicket("TG01", OCT_15));
        assertThrows(UserException.class, () -> tickets.deleteGroupTicket("TG99", OCT_1));
        tickets.deleteGroupTicket("TG01", OCT_1);
        assertFalse(tickets.hasTicketOnOrAfter(pedro, OCT_1));
        assertTrue(tickets.findGroupTicket("TG01").isEmpty());
    }

    @Test
    void findAnyTicketResolvesEveryKind() throws Exception {
        Group g = groupWithAnaAndPedro();
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.createGroupTicket("TG01", g, OCT_15, OCT_1, PRICE);
        assertTrue(tickets.findAnyTicket("T001").orElseThrow() instanceof IndividualTicket);
        assertTrue(tickets.findAnyTicket("TG01").orElseThrow() instanceof GroupTicket);
        assertEquals("TG01-87654321X", tickets.findAnyTicket("TG01-87654321X").orElseThrow().getId());
        assertTrue(tickets.findAnyTicket("T999").isEmpty());
    }

    @Test
    void groupTicketsSurviveRestartLinkedToSameObjects() throws Exception {
        Group g = groupWithAnaAndPedro();
        tickets.createGroupTicket("TG01", g, OCT_15, OCT_1, PRICE);
        UserManager u2 = new UserManager(new UserFileRepository(usersFile));
        GroupManager g2 = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), u2);
        TicketManager t2 = new TicketManager(new TicketFileRepository(ticketsFile),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), u2, g2);
        Group restoredGroup = g2.find("G01").orElseThrow();
        GroupTicket restored = t2.findGroupTicket("TG01").orElseThrow();
        assertSame(restoredGroup, restored.getGroup().orElseThrow());
        assertSame(u2.findByEmail("pedro@correo.com").orElseThrow(), restored.getParts().get(1).getUser().orElseThrow());
        assertTrue(t2.groupHasTicketOnOrAfter(restoredGroup, OCT_15));
        assertEquals(new BigDecimal("80.00"), restored.getPrice());
    }

    @Test
    void detachGroupKeepsHistory() throws Exception {
        Group g = groupWithAnaAndPedro();
        tickets.createGroupTicket("TG01", g, OCT_15, OCT_1, PRICE);
        tickets.detachGroup(g);
        assertFalse(tickets.groupHasTicketOnOrAfter(g, OCT_1));
        assertEquals("G01", tickets.findGroupTicket("TG01").orElseThrow().getGroupId());
    }

    @Test
    void failedSaveRollsBack() throws Exception {
        Path plain = dir.resolve("plain");
        Files.writeString(plain, "x");
        TicketManager broken = new TicketManager(new TicketFileRepository(plain.resolve("tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("gt.txt")), users, new GroupManager(new GroupFileRepository(dir.resolve("g.txt")), users));
        assertThrows(UserException.class, () -> broken.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertTrue(broken.getTickets().isEmpty());
    }
}
