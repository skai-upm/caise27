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
        tickets = new TicketManager(new TicketFileRepository(ticketsFile), users);
    }

    private User ana() {
        return users.findByEmail("ana@correo.com").orElseThrow();
    }

    @Test
    void createsForTodayOrLaterWithCapturedPrice() throws Exception {
        Ticket t = tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
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
        TicketManager restarted = new TicketManager(new TicketFileRepository(ticketsFile), restartedUsers);
        Ticket t = restarted.find("T001").orElseThrow();
        assertTrue(t.getUser().isEmpty());
        assertEquals("12345678Z", t.getOwnerDni());
        assertEquals(OCT_15, t.getDate());
    }

    @Test
    void restartRestoresTicketsLinkedToSameUser() throws Exception {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE);
        tickets.create("T002", "ana@correo.com", OCT_16, OCT_1, new BigDecimal("60.00"));
        UserManager restartedUsers = new UserManager(new UserFileRepository(usersFile));
        TicketManager restarted = new TicketManager(new TicketFileRepository(ticketsFile), restartedUsers);
        User restartedAna = restartedUsers.findByEmail("ana@correo.com").orElseThrow();
        assertSame(restartedAna, restarted.find("T001").orElseThrow().getUser().orElseThrow());
        assertEquals(new BigDecimal("60.00"), restarted.find("T002").orElseThrow().getPrice());
        assertTrue(restarted.hasTicketOnOrAfter(restartedAna, OCT_16));
    }

    @Test
    void failedSaveRollsBack() throws Exception {
        Path plain = dir.resolve("plain");
        Files.writeString(plain, "x");
        TicketManager broken = new TicketManager(new TicketFileRepository(plain.resolve("tickets.txt")), users);
        assertThrows(UserException.class, () -> broken.create("T001", "ana@correo.com", OCT_15, OCT_1, PRICE));
        assertTrue(broken.getTickets().isEmpty());
    }
}
