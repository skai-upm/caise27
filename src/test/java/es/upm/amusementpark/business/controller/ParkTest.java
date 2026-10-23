package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.ApplicationContext;
import es.upm.amusementpark.ApplicationContext.Repositories;
import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.persistence.FileUserRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkTest {

    private static final List<Class<?>> CONTROLLERS = List.of(Park.class, SystemController.class,
            UserController.class, GroupController.class, TicketController.class, AttractionController.class);

    @TempDir
    Path dir;

    private static Repositories repositories(Path directory) {
        return ApplicationContext.fileRepositories(directory,
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }

    @Test
    void controllersAreSingletons() throws ReflectiveOperationException {
        for (Class<?> controller : CONTROLLERS) {
            assertTrue(Modifier.isFinal(controller.getModifiers()), controller.getSimpleName());
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), controller.getSimpleName());
            }
            Method getInstance = controller.getMethod("getInstance");
            assertTrue(Modifier.isStatic(getInstance.getModifiers()));
            assertSame(getInstance.invoke(null), getInstance.invoke(null), controller.getSimpleName());
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        for (Method method : FileUserRepository.class.getMethods()) {
            assertTrue(!method.getName().equals("getInstance"));
        }
        Repositories first = repositories(dir.resolve("a"));
        Repositories second = repositories(dir.resolve("b"));
        assertNotSame(first.users(), second.users());
        assertNotSame(first.tickets(), second.tickets());
        assertNotSame(first.transactions(), second.transactions());
    }

    @Test
    void controllersReceiveTheirCollaboratorsByInjection() throws ReflectiveOperationException, ParkException {
        Park park = TestParks.open(dir);
        assertSame(UserController.getInstance(), park.users());
        assertSame(TicketController.getInstance(), park.tickets());
        park.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.tickets().create("T001", "ana@correo.com", "2027-04-15");
        assertTrue(park.tickets().hasActiveTicket(park.users().findByEmail("ana@correo.com")),
                "the ticket controller resolves users through the injected user controller");
        Field repository = UserController.class.getDeclaredField("repository");
        assertTrue(repository.getType().isInterface(), "controllers depend on repository interfaces");
    }

    @Test
    void startSetsInitialDateAndPrice() {
        LocalDate date = LocalDate.of(2027, 4, 15);
        Park park = TestParks.open(dir, date);
        assertEquals(date, park.getDate());
        assertEquals(new BigDecimal("40.00"), park.getPrice());
    }

    @Test
    void sameControllersWorkWithDifferentlyConfiguredRepositories() throws ParkException {
        Park park = TestParks.start(repositories(dir.resolve("normal")), TestParks.TODAY);
        park.createUser("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        assertTrue(Files.exists(dir.resolve("normal/users.txt")));

        Park fly = TestParks.start(repositories(dir.resolve("tmp/20270415_120000")), TestParks.TODAY);
        assertSame(park, fly);
        assertNull(fly.findUserByEmail("ana@correo.com"));
        fly.createUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        assertTrue(Files.exists(dir.resolve("tmp/20270415_120000/users.txt")));

        Park again = TestParks.start(repositories(dir.resolve("normal")), TestParks.TODAY);
        assertNotNull(again.findUserByEmail("ana@correo.com"));
        assertNull(again.findUserByEmail("pedro@correo.com"));
    }
}
