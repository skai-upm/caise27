package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.persistence.AttractionFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.TicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @TempDir
    Path dir;
    ParkController park;

    @BeforeEach
    void setUp() {
        UserController users = UserController.initialize(new UserFileStorage(dir.resolve("users.txt")));
        GroupController groups = GroupController.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        TicketController tickets = TicketController.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                new GroupTicketFileStorage(dir.resolve("grouptickets.txt")), users, groups);
        AttractionController attractions = AttractionController.initialize(new AttractionFileStorage(dir.resolve("a.txt")));
        park = ParkController.initialize(users, groups, tickets, attractions, TODAY);
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        groups.create("G01", "Familia");
    }

    @Test
    void holdsDateAndPrice() {
        assertEquals(TODAY, park.getCurrentDate());
        assertEquals(ParkController.DEFAULT_PRICE, park.getCurrentPrice());
        park.setCurrentDate(TODAY.plusDays(3));
        assertEquals(TODAY.plusDays(3), park.getCurrentDate());
        park.setCurrentPrice(new BigDecimal("55.5"), "55.5");
        assertEquals("55.50", park.getCurrentPrice().toPlainString());
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentPrice(new BigDecimal("-1"), "-1"));
        assertThrows(IllegalArgumentException.class, () -> park.setCurrentPrice(new BigDecimal("1.234"), "1.234"));
        assertEquals("55.50", park.getCurrentPrice().toPlainString());
    }

    @Test
    void ticketsUseTheControllerDateAndPrice() {
        assertEquals("40.00", park.createTicket("T001", "ana@correo.com", TODAY).getPrice().toPlainString());
        assertThrows(IllegalArgumentException.class, () -> park.createTicket("T002", "ana@correo.com", TODAY.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> park.deleteTicket("T001"));
    }

    @Test
    void userDeletionAndGroupLockRules() {
        park.addMember("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY.plusDays(1));
        assertThrows(IllegalArgumentException.class, () -> park.deleteUser("ana@correo.com", "Clave1!"));
        assertThrows(IllegalArgumentException.class, () -> park.removeMember("G01", "ana@correo.com"));
        assertThrows(IllegalArgumentException.class, () -> park.deleteGroup("G01"));
        assertThrows(IllegalArgumentException.class, () -> park.createGroupTicket("TG02", "G99", TODAY));
        park.setCurrentDate(TODAY.plusDays(2));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertTrue(park.groups().find("G01").orElseThrow().getMembers().isEmpty());
        park.deleteGroup("G01");
        assertTrue(park.groups().find("G01").isEmpty());
    }

    @Test
    void attractionAccessThroughTheController() {
        park.attractions().create("A01", "Tornado", "2");
        park.createTicket("T001", "ana@correo.com", TODAY);
        park.enter("A01", "T001");
        assertEquals(1, park.attractions().get("A01").getPresent().size());
        assertThrows(IllegalArgumentException.class, () -> park.enter("A01", "T999"));
        assertEquals(2, park.exit("A01", "ana@correo.com").getAvailable());
        assertThrows(IllegalArgumentException.class, () -> park.exit("A01", "nadie"));
    }
}
