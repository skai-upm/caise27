package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.ParkRepositories;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.UserRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Singleton controllers and injection of replaceable repositories.
 */
class DependencyInjectionTest {

    private static final List<Class<?>> CONTROLLERS = List.of(ParkController.class, UserService.class,
            GroupService.class, TicketService.class, AttractionService.class, InvoiceService.class);
    private static final List<Class<?>> REPOSITORIES = List.of(UserRepository.class, GroupRepository.class,
            TicketRepository.class, AttractionRepository.class);

    @TempDir
    Path dir;

    @Test
    void controllersAreSingletons() {
        List<Supplier<Object>> accessors = List.of(ParkController::getInstance, UserService::getInstance,
                GroupService::getInstance, TicketService::getInstance, AttractionService::getInstance,
                InvoiceService::getInstance);
        for (Supplier<Object> accessor : accessors) {
            assertSame(accessor.get(), accessor.get());
        }
        for (Class<?> controller : CONTROLLERS) {
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), controller.getSimpleName());
            }
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        for (Class<?> repository : REPOSITORIES) {
            assertTrue(Arrays.stream(repository.getConstructors()).anyMatch(c -> c.getParameterCount() == 0),
                    repository.getSimpleName());
            for (Method method : repository.getDeclaredMethods()) {
                assertFalse(Modifier.isStatic(method.getModifiers()) && method.getReturnType() == repository,
                        repository.getSimpleName() + "." + method.getName());
            }
        }
    }

    @Test
    void controllersWorkOnInjectedInMemoryRepositories() throws Exception {
        ParkRepositories repositories = ParkRepositories.inMemory();
        ParkController.getInstance().initialize(repositories, LocalDate.of(2027, 4, 1));
        UserService.getInstance().createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");
        GroupService.getInstance().createGroup("G01", "Familia");
        assertTrue(repositories.users().findByEmail("ana@correo.com").isPresent());
        assertTrue(repositories.groups().findById("G01").isPresent());
    }

    @Test
    void sameControllersUseDifferentlyConfiguredRepositories() throws Exception {
        Path normal = dir.resolve("data");
        Path fly = dir.resolve("tmp/20270401_100000");
        ParkController park = ParkController.getInstance();

        park.load(normal, LocalDate.of(2027, 4, 1));
        UserService.getInstance().createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!",
                "612345678", "1234567890123456");
        park.load(fly, LocalDate.of(2027, 4, 1));
        assertTrue(park.getUsers().findByEmail("ana@correo.com").isEmpty());
        UserService.getInstance().createUser("Pedro", "Lopez Martin", "87654321X", "pedro@correo.com", "Otra2#",
                "623456789", "9876543210987654");

        assertTrue(Files.readString(normal.resolve("users.txt")).contains("ana@correo.com"));
        assertFalse(Files.readString(normal.resolve("users.txt")).contains("pedro@correo.com"));
        assertTrue(Files.readString(fly.resolve("users.txt")).contains("pedro@correo.com"));
    }
}
