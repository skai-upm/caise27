package es.upm.park.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.park.business.model.Invoice;
import es.upm.park.persistence.AttractionFileStorage;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.PassFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComponentWiringTest {

    static final PasswordHasher FAST_HASHER = new BCryptPasswordHasher(4);

    @TempDir
    Path dir;

    @BeforeEach
    @AfterEach
    void resetControllers() {
        ParkSession.resetControllers();
    }

    @Test
    void sessionWiresSingleInstances() {
        try (ParkSession session = ParkSession.permanent(dir)) {
            assertSame(session.controller(), ParkController.getInstance());
            assertSame(ParkController.getInstance(), ParkController.getInstance());
            assertSame(UserManager.getInstance(), UserManager.getInstance());
            session.controller().createUser("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
            assertEquals(1, UserManager.getInstance().getUsers().size(), "controller and access point share the instance");
        }
    }

    @Test
    void secondInitializationAndEarlyAccessFail() {
        assertThrows(IllegalStateException.class, UserManager::getInstance);
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        UserManager first = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")), FAST_HASHER);
        assertThrows(IllegalStateException.class,
                () -> UserManager.initialize(new UserFileStorage(dir.resolve("other.txt")), FAST_HASHER));
        assertSame(first, UserManager.getInstance());
        assertThrows(IllegalStateException.class, () -> ParkSession.permanent(dir), "controllers already active");
        assertThrows(IllegalStateException.class, GroupManager::getInstance, "failed session left nothing behind");
    }

    @Test
    void newExecutionAfterClose() {
        ParkController first;
        try (ParkSession session = ParkSession.permanent(dir)) {
            first = session.controller();
        }
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        try (ParkSession session = ParkSession.permanent(dir)) {
            assertNotSame(first, session.controller());
        }
    }

    @Test
    void normalAndFlyUseDifferentRepositoriesWithTheSameLogic() {
        try (ParkSession normal = ParkSession.permanent(dir)) {
            normal.controller().createUser("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        }
        try (ParkSession fly = ParkSession.temporary(dir.resolve("tmp"), LocalDateTime.of(2026, 8, 26, 19, 3, 15))) {
            assertEquals(ParkController.class, fly.controller().getClass());
            assertEquals(0, UserManager.getInstance().getUsers().size(), "temporary repositories start empty");
            fly.controller().createUser("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
            assertTrue(Files.exists(fly.temporaryDirectory().resolve("users.txt")));
        }
        assertTrue(Files.exists(dir.resolve("users.txt")));
    }

    @Test
    void injectedInvoiceDirectorIsUsed() {
        int[] calls = {0};
        InvoiceDirector counting = new InvoiceDirector() {
            @Override
            public Invoice construct(InvoiceBuilder builder) {
                calls[0]++;
                return super.construct(builder);
            }
        };
        UserManager users = UserManager.initialize(new UserFileStorage(dir.resolve("users.txt")), FAST_HASHER);
        GroupManager groups = GroupManager.initialize(new GroupFileStorage(dir.resolve("groups.txt")), users);
        TicketManager tickets = TicketManager.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                new GroupTicketFileStorage(dir.resolve("group-tickets.txt")), users, groups);
        AttractionManager attractions = AttractionManager.initialize(new AttractionFileStorage(dir.resolve("a.txt")));
        PassManager passes = PassManager.initialize(new PassFileStorage(dir.resolve("passes.txt")), tickets);
        ParkController park = ParkController.initialize(users, groups, tickets, attractions, passes, counting);
        park.createUser("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        park.createGroup("G01", "Familia");
        park.createUserInvoice("ana@correo.com");
        park.createGroupInvoice("G01");
        assertEquals(2, calls[0]);
    }
}
