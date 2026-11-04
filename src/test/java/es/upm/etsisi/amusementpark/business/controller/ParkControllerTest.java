package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.TestSupport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.upm.etsisi.amusementpark.business.invoice.InvoiceDirector;
import es.upm.etsisi.amusementpark.business.service.PassManager;
import es.upm.etsisi.amusementpark.persistence.PassFileRepository;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkControllerTest {

    @TempDir
    Path dir;

    private ParkController controller;
    private UserManager users;
    private GroupManager groups;
    private TicketManager tickets;
    private AttractionManager attractions;
    private ParkServices services;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserManager(new UserFileRepository(dir.resolve("users.txt")), TestSupport.HASHER);
        groups = new GroupManager(new GroupFileRepository(dir.resolve("groups.txt")), users);
        tickets = new TicketManager(new TicketFileRepository(dir.resolve("tickets.txt")),
                new GroupTicketFileRepository(dir.resolve("grouptickets.txt")), users, groups);
        attractions = new AttractionManager(new AttractionFileRepository(dir.resolve("attractions.txt")));
        ParkController.shutdown();
        services = new ParkServices(users, groups, tickets, attractions,
                new PassManager(new PassFileRepository(dir.resolve("passes.txt")), tickets));
        controller = ParkController.initialize(services, new InvoiceDirector(), LocalDate.of(2026, 10, 1));
    }

    @AfterEach
    void tearDown() {
        ParkController.shutdown();
    }

    @Test
    void everyRequesterGetsTheSameInstance() {
        assertSame(controller, ParkController.getInstance());
        assertSame(ParkController.getInstance(), ParkController.getInstance());
    }

    @Test
    void secondInitializationIsRefused() {
        assertThrows(IllegalStateException.class, () -> ParkController.initialize(services,
                new InvoiceDirector(), LocalDate.of(2027, 1, 1)));
        assertSame(controller, ParkController.getInstance());
        assertEquals(LocalDate.of(2026, 10, 1), ParkController.getInstance().getCurrentDate());
    }

    @Test
    void notAvailableBeforeInitializationAndRenewedAfterShutdown() {
        ParkController.shutdown();
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        ParkController next = ParkController.initialize(services, new InvoiceDirector(), LocalDate.of(2026, 10, 2));
        assertNotSame(controller, next);
        assertSame(next, ParkController.getInstance());
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
