package es.upm.park.business;

import es.upm.park.business.model.Result;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.persistence.GroupTicketFileStore;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.business.model.User;
import es.upm.park.persistence.UserFileStore;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class TicketManagerTest {

    private static final LocalDate OCT_1 = LocalDate.of(2026, 10, 1);
    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final BigDecimal P40 = new BigDecimal("40.00");

    @TempDir
    Path dir;
    private Path usersFile;
    private Path ticketsFile;
    private UserManager users;
    private TicketManager tickets;

    @BeforeEach
    void setUp() throws IOException {
        usersFile = dir.resolve("users.txt");
        ticketsFile = dir.resolve("tickets.txt");
        users = UserManager.configure(new UserFileStore(usersFile), new BCryptPasswordHasher(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        tickets = newTickets(users);
    }

    private TicketManager newTickets(UserManager u) throws IOException {
        GroupManager g = GroupManager.configure(new GroupFileStore(dir.resolve("groups.txt")), u);
        return TicketManager.configure(new TicketFileStore(ticketsFile),
                new GroupTicketFileStore(dir.resolve("grouptickets.txt")), u, g);
    }

    private List<String> lines() throws IOException {
        return Files.exists(ticketsFile) ? Files.readAllLines(ticketsFile) : List.of();
    }

    @Test
    void createReportsPriceAndLinksUser() throws IOException {
        Result r = tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertEquals("OK TICKET CREATED -> 40.00", r.toString());
        IndividualTicket t = tickets.find("T001").orElseThrow();
        assertSame(users.findByEmail("ana@correo.com").orElseThrow(), t.getUser());
        assertEquals(List.of("T001;2026-10-15;40.00;12345678Z"), lines());
    }

    @Test
    void ticketForTodayAllowed() {
        assertTrue(tickets.create("T002", "ana@correo.com", OCT_15, OCT_15, P40).success());
    }

    @Test
    void unknownUserPastDateDuplicateOrBlankRejected() throws IOException {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        List<String> before = lines();
        assertFalse(tickets.create("T003", "nadie@correo.com", OCT_15, OCT_1, P40).success());
        assertFalse(tickets.create("T004", "ana@correo.com", OCT_1.minusDays(1), OCT_1, P40).success());
        assertFalse(tickets.create("T001", "ana@correo.com", OCT_15.plusDays(5), OCT_1, P40).success());
        assertFalse(tickets.create(" ", "ana@correo.com", OCT_15, OCT_1, P40).success());
        assertEquals(1, tickets.getTickets().size());
        assertEquals(before, lines());
    }

    @Test
    void priceIsCapturedPerTicket() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        Result r = tickets.create("T002", "ana@correo.com", OCT_15, OCT_1, new BigDecimal("60.00"));
        assertEquals("OK TICKET CREATED -> 60.00", r.toString());
        assertEquals(P40, tickets.find("T001").orElseThrow().getPrice());
    }

    @Test
    void deleteOnlyFutureTickets() throws IOException {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertFalse(tickets.delete("T001", OCT_15).success(), "today does not count");
        assertFalse(tickets.delete("T001", OCT_15.plusDays(1)).success(), "past ticket");
        assertFalse(tickets.delete("T999", OCT_1).success());
        assertEquals(1, lines().size());
        assertTrue(tickets.delete("T001", OCT_15.minusDays(1)).success());
        assertTrue(tickets.getTickets().isEmpty());
        assertTrue(lines().isEmpty());
    }

    @Test
    void activeTicketDetection() {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        User ana = users.findByEmail("ana@correo.com").orElseThrow();
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_1));
        assertTrue(tickets.hasTicketOnOrAfter(ana, OCT_15));
        assertFalse(tickets.hasTicketOnOrAfter(ana, OCT_15.plusDays(1)));
    }

    @Test
    void restartRestoresTicketsAndKeepsHistoryOfDeletedUsers() throws IOException {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        assertTrue(users.delete("ana@correo.com", "Clave1!").success());

        UserManager users2 = UserManager.configure(new UserFileStore(usersFile), new BCryptPasswordHasher(4));
        TicketManager tickets2 = newTickets(users2);
        assertTrue(tickets2.getTickets().isEmpty());
        users2.create("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#", "623456789", "9876543210987654");
        assertFalse(tickets2.create("T001", "pedro@correo.com", OCT_15, OCT_1, P40).success(), "id reserved by history");
        assertTrue(tickets2.create("T002", "pedro@correo.com", OCT_15, OCT_1, P40).success());
        assertEquals(List.of("T002;2026-10-15;40.00;87654321X", "T001;2026-10-15;40.00;12345678Z"), lines());
    }

    @Test
    void restartRestoresLinkedTickets() throws IOException {
        tickets.create("T001", "ana@correo.com", OCT_15, OCT_1, P40);
        UserManager users2 = UserManager.configure(new UserFileStore(usersFile), new BCryptPasswordHasher(4));
        TicketManager tickets2 = newTickets(users2);
        IndividualTicket t = tickets2.find("T001").orElseThrow();
        assertSame(users2.findByEmail("ana@correo.com").orElseThrow(), t.getUser());
        assertEquals(P40, t.getPrice());
    }
}
