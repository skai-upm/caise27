package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import es.upm.parque.TestContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ControllerLifecycleTest {

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(GroupController.getInstance(), GroupController.getInstance());
        assertSame(TicketController.getInstance(), TicketController.getInstance());
        assertSame(PassController.getInstance(), PassController.getInstance());
        assertSame(AttractionController.getInstance(), AttractionController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
    }

    @Test
    void sameControllersWorkOnRepositoriesConfiguredWithDifferentPaths() throws IOException {
        UserController before = UserController.getInstance();
        TestContext normal = TestContext.start(dir.resolve("data"));
        normal.user(1);
        TestContext fly = TestContext.start(dir.resolve("tmp"));
        assertSame(before, fly.users());
        assertNull(fly.users().findByEmail(TestContext.email(1)));
        fly.user(2);
        assertTrue(Files.exists(dir.resolve("tmp").resolve("users.txt")));
        TestContext again = TestContext.start(dir.resolve("data"));
        assertNotNull(again.users().findByEmail(TestContext.email(1)));
        assertNull(again.users().findByEmail(TestContext.email(2)));
        assertTrue(again.run("HELP").contains("Available commands"));
    }
}
