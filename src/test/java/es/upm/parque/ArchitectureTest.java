package es.upm.parque;

import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.ParkController;
import es.upm.parque.business.controller.PassController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.persistence.TextAttractionRepository;
import es.upm.parque.persistence.TextGroupRepository;
import es.upm.parque.persistence.TextPassRepository;
import es.upm.parque.persistence.TextTicketRepository;
import es.upm.parque.persistence.TextUserRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the layered package structure and the Singleton/injection rules on the real sources.
 */
public class ArchitectureTest {

    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "parque");

    private static List<Path> sourcesUnder(String layer) throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES.resolve(layer))) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static void assertNoImport(String layer, String... forbidden) throws IOException {
        for (Path file : sourcesUnder(layer)) {
            String text = Files.readString(file);
            for (String prefix : forbidden) {
                assertFalse(text.contains("import " + prefix), file + " must not import " + prefix);
            }
        }
    }

    @Test
    public void everyClassDeclaresAPackageInsideItsLayer() throws IOException {
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertTrue(Files.isDirectory(SOURCES.resolve(layer)), layer + " package missing");
        }
        try (Stream<Path> files = Files.walk(Path.of("src", "main", "java"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String expected = "package " + SOURCES.getParent().getParent().getParent()
                        .relativize(file.getParent()).toString().replace(java.io.File.separatorChar, '.') + ";";
                assertTrue(Files.readString(file).contains(expected), file + " must declare " + expected);
            }
        }
    }

    @Test
    public void presentationDoesNotTouchPersistenceOrFiles() throws IOException {
        assertNoImport("presentation", "es.upm.parque.persistence", "java.nio.file", "java.io.File");
    }

    @Test
    public void businessDoesNotDependOnOuterLayers() throws IOException {
        assertNoImport("business", "es.upm.parque.persistence", "es.upm.parque.presentation", "es.upm.parque.App",
                "es.upm.parque.ParkApplication");
    }

    @Test
    public void domainModelDoesNoIo() throws IOException {
        assertNoImport("business/model", "java.io", "java.nio", "es.upm.parque.business.repository");
    }

    @Test
    public void persistenceDoesNotDependOnPresentation() throws IOException {
        assertNoImport("persistence", "es.upm.parque.presentation");
    }

    @Test
    public void controllersAreSingletons() throws Exception {
        for (Class<?> controller : List.of(ParkController.class, UserController.class, GroupController.class,
                TicketController.class, PassController.class, AttractionController.class, InvoiceController.class)) {
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(constructor.getModifiers()), controller + " constructor must be private");
            }
            Method getInstance = controller.getMethod("getInstance");
            assertTrue(Modifier.isStatic(getInstance.getModifiers()));
            assertSame(getInstance.invoke(null), getInstance.invoke(null), controller + " must return one instance");
        }
    }

    @Test
    public void repositoriesAreNotSingletons() {
        for (Class<?> repository : List.of(TextUserRepository.class, TextGroupRepository.class,
                TextTicketRepository.class, TextPassRepository.class, TextAttractionRepository.class)) {
            assertTrue(Stream.of(repository.getConstructors()).anyMatch(c -> Modifier.isPublic(c.getModifiers())),
                    repository + " must be instantiable for injection");
            assertTrue(Stream.of(repository.getDeclaredMethods()).noneMatch(m -> m.getName().equals("getInstance")));
        }
    }
}
