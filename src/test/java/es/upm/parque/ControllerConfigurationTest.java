package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.business.AttractionManager;
import es.upm.parque.business.GroupManager;
import es.upm.parque.business.InvoiceManager;
import es.upm.parque.business.ParkConfiguration;
import es.upm.parque.business.ParkController;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.SystemManager;
import es.upm.parque.business.TicketManager;
import es.upm.parque.business.UserManager;
import es.upm.parque.persistence.AttractionRepository;
import es.upm.parque.persistence.GroupRepository;
import es.upm.parque.persistence.ParkRepositories;
import es.upm.parque.persistence.TicketRepository;
import es.upm.parque.persistence.UserRepository;

/**
 * Singleton controllers, repositories injected as replaceable dependencies, and collaborations wired at
 * initialization by project code.
 */
class ControllerConfigurationTest {

    private static final List<Class<?>> CONTROLLERS = List.of(ParkController.class, UserManager.class,
            GroupManager.class, TicketManager.class, AttractionManager.class, SystemManager.class,
            InvoiceManager.class);
    private static final List<Class<?>> REPOSITORIES = List.of(UserRepository.class, GroupRepository.class,
            TicketRepository.class, AttractionRepository.class);
    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "parque");

    @AfterEach
    void restoreConfiguration() {
        ParkConfiguration.initialize(ParkRepositories.inMemory());
    }

    @Test
    void controllersAreSingletons() throws ReflectiveOperationException {
        for (Class<?> controller : CONTROLLERS) {
            Method getInstance = controller.getMethod("getInstance");
            assertTrue(Modifier.isStatic(getInstance.getModifiers()), controller.getSimpleName());
            assertSame(getInstance.invoke(null), getInstance.invoke(null), controller.getSimpleName());
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                assertFalse(Modifier.isPublic(constructor.getModifiers()), controller.getSimpleName());
            }
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        for (Class<?> repository : REPOSITORIES) {
            assertTrue(Stream.of(repository.getConstructors()).anyMatch(c -> Modifier.isPublic(c.getModifiers())),
                    repository.getSimpleName());
            assertTrue(Stream.of(repository.getMethods()).noneMatch(m -> m.getName().equals("getInstance")),
                    repository.getSimpleName());
        }
    }

    @Test
    void controllersUseTheInjectedRepositories() throws ParkException {
        ParkRepositories a = ParkRepositories.inMemory();
        ParkRepositories b = ParkRepositories.inMemory();
        ParkConfiguration.initialize(a);
        UserManager.getInstance().create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "600000000",
                "1234567890123456");
        TicketManager.getInstance().createIndividual("T1", "ana@correo.com",
                ParkController.getInstance().getCurrentDate().toString());
        assertNotNull(a.getUsers().findByEmail("ana@correo.com"));
        assertNotNull(a.getTickets().findById("T1"));

        ParkConfiguration.initialize(b);
        ParkController park = ParkController.getInstance();
        assertSame(b.getUsers(), park.getUsers());
        assertSame(b.getTickets(), park.getTickets());
        assertNull(park.getUsers().findByEmail("ana@correo.com"));
        AttractionManager.getInstance().create("A01", "Dragon", "10");
        assertNotNull(b.getAttractions().findById("A01"));
        assertNull(a.getAttractions().findById("A01"));
        assertThrows(ParkException.class, () -> AttractionManager.getInstance().enter("A01", "T1"),
                "the attraction collaborator must resolve tickets in the configured repositories");
    }

    @Test
    void controllersFailClearlyBeforeInitialization() throws ParkException {
        ParkConfiguration.initialize(ParkRepositories.inMemory());
        AttractionManager.getInstance().create("A01", "Dragon", "10");
        UserManager.getInstance().inject(null);
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> UserManager.getInstance().delete("ana@correo.com", "Clave1!"));
        assertEquals("the application has not been initialized", e.getMessage());
        AttractionManager.getInstance().inject(ParkController.getInstance(), null);
        assertThrows(IllegalStateException.class, () -> AttractionManager.getInstance().enter("A01", "T1"),
                "the ticket collaborator is required and was not injected");
    }

    @Test
    void noControllerOrHandlerCreatesItsDependencies() throws IOException {
        Pattern manager = Pattern.compile("new \\w+Manager\\(");
        Pattern repository = Pattern.compile("new \\w*Repositor(y|ies)\\(|ParkRepositories\\.(inMemory|fileBacked)");
        for (Path file : javaFiles("business")) {
            String name = file.getFileName().toString();
            String self = name.substring(0, name.length() - ".java".length());
            String code = Files.readString(file, StandardCharsets.UTF_8).replace("new " + self + "()", "");
            assertFalse(manager.matcher(code).find(), name);
            boolean initialization = name.equals("ParkConfiguration.java") || name.equals("ParkSession.java");
            if (!initialization) {
                assertFalse(repository.matcher(code).find(), name);
            }
        }
        for (Path file : javaFiles("presentation")) {
            assertFalse(manager.matcher(Files.readString(file, StandardCharsets.UTF_8)).find(), file.toString());
        }
    }

    @Test
    void noDependencyInjectionFrameworkIsUsed() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        for (String framework : List.of("springframework", "guice", "dagger", "javax.inject", "jakarta.inject",
                "picocontainer", "weld")) {
            assertFalse(pom.contains(framework), framework);
        }
    }

    private static List<Path> javaFiles(String layer) throws IOException {
        try (Stream<Path> files = Files.list(SOURCES.resolve(layer))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }
}
