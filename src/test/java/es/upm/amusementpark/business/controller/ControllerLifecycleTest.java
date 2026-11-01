package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.UserRepository;
import es.upm.amusementpark.persistence.BCryptPasswordHasher;
import es.upm.amusementpark.persistence.StoragePaths;
import es.upm.amusementpark.persistence.UserFileRepository;
import es.upm.amusementpark.presentation.AppBootstrap;

public class ControllerLifecycleTest {

    @TempDir
    Path dir;

    @AfterEach
    public void tearDown() {
        AppBootstrap.shutdown();
    }

    /** Repository kept in memory, to show controllers accept any implementation. */
    private static final class InMemoryUserRepository implements UserRepository {
        private List<User> stored = new ArrayList<>();
        private int saves;

        @Override
        public List<User> load() {
            return new ArrayList<>(stored);
        }

        @Override
        public void save(List<User> users) {
            stored = new ArrayList<>(users);
            saves++;
        }
    }

    @Test
    public void controllersAreSingleInstancesPerExecution() throws IOException {
        ParkController park = AppBootstrap.start(new StoragePaths(dir));
        assertSame(park, ParkController.getInstance());
        assertSame(ParkController.getInstance(), ParkController.getInstance());
        assertSame(park.getUsers(), UserController.getInstance());
        assertSame(park.getGroups(), GroupController.getInstance());
        assertSame(park.getTickets(), TicketController.getInstance());
        assertSame(park.getAttractions(), AttractionController.getInstance());
    }

    @Test
    public void secondInitializationInSameExecutionFails() throws IOException {
        AppBootstrap.start(new StoragePaths(dir));
        assertThrows(IllegalStateException.class,
                () -> UserController.initialize(new UserFileRepository(dir.resolve("other.txt")), new BCryptPasswordHasher(4)));
        assertThrows(IllegalStateException.class, () -> AppBootstrap.start(new StoragePaths(dir)));
    }

    @Test
    public void instanceUnavailableOutsideAnExecution() {
        assertThrows(IllegalStateException.class, ParkController::getInstance);
        assertThrows(IllegalStateException.class, UserController::getInstance);
    }

    @Test
    public void newExecutionGetsNewInstance() throws IOException {
        ParkController first = AppBootstrap.start(new StoragePaths(dir));
        AppBootstrap.shutdown();
        ParkController second = AppBootstrap.start(new StoragePaths(dir));
        assertSame(second, ParkController.getInstance());
        assertEquals(false, first == second);
    }

    @Test
    public void alternativeRepositoryCanBeInjected() throws Exception {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        UserController users = UserController.initialize(repository, new BCryptPasswordHasher(4));
        users.create("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        assertEquals(1, repository.stored.size());
        assertEquals(1, repository.saves);
    }
}
