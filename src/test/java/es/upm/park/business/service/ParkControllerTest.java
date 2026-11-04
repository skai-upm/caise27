package es.upm.park.business.service;

import es.upm.park.business.model.AttractionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.park.business.model.Attraction;
import es.upm.park.persistence.AttractionFileStorage;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.PassFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    @TempDir
    Path dir;
    ParkController park;
    UserManager users;
    GroupManager groups;

    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @BeforeEach
    void setUp() {
        ParkSession.resetControllers();
        users = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")), FAST_HASHER);
        groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        TicketManager tickets = TicketManager.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), users, groups);
        AttractionManager attractions = AttractionManager.initialize(new AttractionFileStorage(dir.resolve("attractions.txt")));
        PassManager passes = PassManager.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
        park = ParkController.initialize(users, groups, tickets, attractions, passes, new InvoiceDirector(),
                LocalDate.of(2026, 10, 1));
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        park.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "1234567890123456");
    }

    @Test
    void ownsDateAndDefaultPrice() {
        assertEquals(LocalDate.of(2026, 10, 1), park.getCurrentDate());
        assertEquals(new BigDecimal("40.00"), park.getCurrentPrice());
        park.setCurrentDate(LocalDate.of(2030, 1, 1));
        assertEquals(LocalDate.of(2030, 1, 1), park.getCurrentDate());
    }

    @Test
    void priceMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentPrice(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentPrice(new BigDecimal("-1.00")));
        park.setCurrentPrice(new BigDecimal("60.00"));
        assertEquals(new BigDecimal("60.00"), park.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 2)).getPrice());
    }

    @Test
    void activeTicketBlocksUserDeletion() {
        park.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        assertThrows(IllegalArgumentException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        park.setCurrentDate(LocalDate.of(2026, 10, 16));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertEquals(null, users.findByEmail("ana@correo.com"));
    }

    @Test
    void groupTicketLocksGroup() {
        park.createGroup("G01", "Familia");
        park.addGroupMember("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", LocalDate.of(2026, 10, 15));
        assertThrows(IllegalArgumentException.class, () -> park.addGroupMember("G01", "pedro@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> park.removeGroupMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> park.deleteGroup("G01"));
        park.setCurrentDate(LocalDate.of(2026, 10, 16));
        park.deleteGroup("G01");
    }

    @Test
    void unknownReferencesRejected() {
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("T1", "nadie@correo.com", LocalDate.of(2026, 10, 2)));
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("TG1", "G99", LocalDate.of(2026, 10, 2)));
        assertThrows(IllegalArgumentException.class, () -> park.findAttraction("A99"));
        assertThrows(IllegalArgumentException.class, () -> park.findTicket("T99"));
    }

    @Test
    void attractionEntryUsesSystemDate() {
        park.createAttraction("A01", "Tornado", 2, AttractionType.GENERAL);
        park.createTicket("T001", "ana@correo.com", LocalDate.of(2026, 10, 15));
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A01", "T001", false));
        park.setCurrentDate(LocalDate.of(2026, 10, 15));
        Attraction<?> a = park.enterAttraction("A01", "T001", false).attraction();
        assertEquals(1, a.getPresentCount());
        assertEquals(1, park.exitAttraction("A01", "T001"));
        assertNotNull(park.findAttraction("A01"));
    }
}
