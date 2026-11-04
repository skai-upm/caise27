package es.upm.etsisi.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.service.AttractionManager;
import es.upm.etsisi.amusementpark.business.service.GroupManager;
import es.upm.etsisi.amusementpark.business.service.TicketManager;
import es.upm.etsisi.amusementpark.business.service.UserManager;
import es.upm.etsisi.amusementpark.persistence.AttractionFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupFileRepository;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.TicketFileRepository;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    @TempDir
    Path dir;

    private ParkController controller;

    @BeforeEach
    void setUp() throws Exception {
        UserManager users = new UserManager(new UserFileRepository(dir.resolve("users.txt")));
        GroupManager groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users);
        TicketManager tickets = new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), users, groups);
        AttractionManager attractions = new AttractionManager(new AttractionFileRepository(dir.resolve("attractions.txt")));
        controller = new ParkController(users, groups, tickets, attractions, LocalDate.of(2026, 10, 1));
    }

    @Test
    void startsWithDefaultPriceAndInjectedDate() {
        assertEquals(ParkController.DEFAULT_TICKET_PRICE, controller.getTicketPrice());
        assertEquals(LocalDate.of(2026, 10, 1), controller.getCurrentDate());
    }

    @Test
    void rejectsNonPositivePrices() throws Exception {
        assertThrows(UserException.class, () -> controller.setTicketPrice(BigDecimal.ZERO));
        assertThrows(UserException.class, () -> controller.setTicketPrice(null));
        controller.setTicketPrice(new BigDecimal("60.00"));
        assertEquals(new BigDecimal("60.00"), controller.getTicketPrice());
    }

    @Test
    void reportsUnknownGroupsAndAttractions() {
        assertThrows(UserException.class, () -> controller.requireGroup("G99"));
        assertThrows(UserException.class, () -> controller.getAttraction("A99"));
        assertThrows(UserException.class, () -> controller.enterAttraction("A99", "T001"));
    }
}
