package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.attraction.AttractionController;
import es.upm.etsisi.amusementpark.business.group.GroupController;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceController;
import es.upm.etsisi.amusementpark.business.park.Park;
import es.upm.etsisi.amusementpark.business.pass.PassController;
import es.upm.etsisi.amusementpark.business.ticket.TicketController;
import es.upm.etsisi.amusementpark.business.user.UserController;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static final Path MAIN = Path.of("src/main/java/es/upm/etsisi/amusementpark");

    private static List<Path> sources(String layer) throws IOException {
        try (Stream<Path> files = Files.walk(MAIN.resolve(layer))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path file : sources("presentation")) {
            String code = Files.readString(file);
            assertFalse(code.contains("amusementpark.persistence"), file.toString());
            assertFalse(code.contains("java.nio.file"), file.toString());
        }
    }

    @Test
    void businessDoesNotDoFileIo() throws IOException {
        for (Path file : sources("business")) {
            String code = Files.readString(file);
            assertFalse(code.contains("java.nio.file") || code.contains("java.io.File"), file.toString());
            assertFalse(code.contains("amusementpark.persistence"), file.toString());
            assertFalse(code.contains("amusementpark.presentation"), file.toString());
        }
    }

    @Test
    void everyClassIsInANamedPackage() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                assertTrue(Files.readString(file).startsWith("package es.upm.etsisi.amusementpark"), file.toString());
            }
        }
    }

    @Test
    void controllersAreSingletons() throws Exception {
        for (Class<?> type : List.of(Park.class, UserController.class, GroupController.class, TicketController.class,
                PassController.class, AttractionController.class, InvoiceController.class)) {
            for (Constructor<?> c : type.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), type.getSimpleName());
            }
            Method getInstance = type.getMethod("getInstance");
            assertSame(getInstance.invoke(null), getInstance.invoke(null), type.getSimpleName());
        }
    }

    @Test
    void repositoriesAreNotSingletons() {
        assertTrue(Stream.of(es.upm.etsisi.amusementpark.persistence.FileParkRepository.class.getConstructors())
                .anyMatch(c -> Modifier.isPublic(c.getModifiers())));
    }
}
