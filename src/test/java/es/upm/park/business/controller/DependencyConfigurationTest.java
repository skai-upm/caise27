package es.upm.park.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.business.service.AttractionService;
import es.upm.park.business.service.GroupService;
import es.upm.park.business.service.InvoiceService;
import es.upm.park.business.service.ParkException;
import es.upm.park.business.service.TicketService;
import es.upm.park.business.service.UserService;
import es.upm.park.persistence.repository.ParkRepositories;

class DependencyConfigurationTest {

    private static final LocalDate DAY = LocalDate.of(2027, 4, 15);
    private static final List<Class<?>> CONTROLLERS = List.of(ParkController.class, UserService.class,
            GroupService.class, TicketService.class, AttractionService.class, InvoiceService.class);

    @TempDir
    Path d1;

    @TempDir
    Path d2;

    @Test
    void controllersAreSingletons() throws Exception {
        for (Class<?> c : CONTROLLERS) {
            for (Constructor<?> k : c.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(k.getModifiers()), c.getSimpleName());
            }
            Method getInstance = c.getMethod("getInstance");
            assertSame(getInstance.invoke(null), getInstance.invoke(null), c.getSimpleName());
        }
        ParkController park = ParkController.getInstance();
        park.start(ParkRepositories.inMemory(), DAY);
        assertSame(UserService.getInstance(), park.getUserService());
        assertSame(GroupService.getInstance(), park.getGroupService());
        assertSame(TicketService.getInstance(), park.getTicketService());
        assertSame(AttractionService.getInstance(), park.getAttractionService());
        assertSame(InvoiceService.getInstance(), park.getInvoiceService());
    }

    @Test
    void repositoriesAreReplaceableInstancesPerConfiguration() {
        ParkRepositories first = ParkRepositories.load(d1);
        ParkRepositories second = ParkRepositories.load(d2);
        assertNotSame(first.users(), second.users());
        assertNotSame(first.tickets(), second.tickets());
        ParkController park = ParkController.getInstance();
        park.start(first, DAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getGroupService().create("G01", "F");
        park.start(second, DAY);
        assertThrows(ParkException.class, () -> park.getGroupService().find("G01"));
        assertEquals(0, second.users().findAll().size());
        park.start(ParkRepositories.load(d1), DAY);
        assertEquals("F", park.getGroupService().find("G01").getName());
    }

    @Test
    void groupLockingUsesTheTicketControllerWiredAtStart() {
        ParkController park = ParkController.getInstance();
        park.start(ParkRepositories.inMemory(), DAY);
        park.getUserService().create("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.getGroupService().create("G01", "F");
        park.getGroupService().addUser("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", DAY);
        assertTrue(park.getGroupService().isLocked(park.getGroupService().find("G01")));
    }
}
