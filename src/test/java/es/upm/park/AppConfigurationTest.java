package es.upm.park;

import es.upm.park.business.controller.InvoiceController;
import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.model.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigurationTest {

    @TempDir
    Path base;

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    @Test
    void eachConfigurationGetsItsOwnRepositories() {
        PrintStream warnings = new PrintStream(new ByteArrayOutputStream());
        AppConfiguration.Configured first = AppConfiguration.configure(base.resolve("one"), warnings);
        AppConfiguration.Configured second = AppConfiguration.configure(base.resolve("two"), warnings);
        assertNotSame(first.groups(), second.groups());
        assertNotSame(first.tickets(), second.tickets());
        assertNotSame(first.attractions(), second.attractions());
        assertNotSame(first.storage(), second.storage());

        first.groups().saveAll(List.of(new Group("G01", "Uno")));
        assertTrue(Files.exists(base.resolve("one").resolve("groups.txt")));
        assertFalse(Files.exists(base.resolve("two").resolve("groups.txt")));
        assertEquals(0, second.groups().loadAll().size());
    }

    @Test
    void passwordServiceIsConfiguredWithBCrypt() {
        AppConfiguration.Configured configured = AppConfiguration.configure(base.resolve("data"),
                new PrintStream(new ByteArrayOutputStream()));
        String stored = configured.passwords().protect("Clave1!");
        assertTrue(stored.startsWith("$2a$"));
        assertTrue(configured.passwords().verify("Clave1!", stored));
    }

    @Test
    void storedUsersAreVisibleToParkManagement() {
        AppConfiguration.Configured configured = AppConfiguration.configure(base.resolve("data"),
                new PrintStream(new ByteArrayOutputStream()));
        configured.users().save(new es.upm.park.business.model.User("Ana", "Garcia Lopez", "12345678Z",
                "ana@correo.com", configured.passwords().protect("Clave1!"), "612345678", "1234567890123456"));
        String today = ParkController.getInstance().getCurrentDate().toString();
        assertEquals("TICKET CREATED. PRICE: 40.00",
                configured.processor().process("TICKET CREATE;T001;ana@correo.com;" + today).output());
    }

    @Test
    void controllersAreWiredByTheConfiguration() {
        InvoiceController.getInstance().setParkController(null);
        AppConfiguration.Configured configured = AppConfiguration.configure(base.resolve("data"),
                new PrintStream(new ByteArrayOutputStream()));
        ParkController.getInstance().createGroup("G01", "Familia");
        assertTrue(configured.processor().process("INVOICE CREATE;GROUP;G01").output().startsWith("INVOICE - GROUP"));
        assertTrue(Files.exists(base.resolve("data").resolve("groups.txt")), "storage injected into the controller");
    }
}
