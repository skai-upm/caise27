package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.persistence.text.TextUserRepository;
import org.junit.jupiter.api.Test;

class ArchitectureTest extends ParkTestSupport {

    private static final Path MAIN = Path.of("src", "main", "java", "es", "upm", "amusementpark");

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void layeredPackagesExist() {
        assertTrue(Files.isDirectory(MAIN.resolve("presentation")));
        assertTrue(Files.isDirectory(MAIN.resolve("business")));
        assertTrue(Files.isDirectory(MAIN.resolve("persistence")));
    }

    @Test
    void noDefaultPackage() throws IOException {
        for (Path source : sources(Path.of("src", "main", "java"))) {
            assertTrue(Files.readString(source).contains("package es.upm.amusementpark"), source.toString());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path source : sources(MAIN.resolve("presentation"))) {
            String code = Files.readString(source);
            assertFalse(code.contains("es.upm.amusementpark.persistence"), source.toString());
            assertFalse(code.contains("java.nio.file.Files"), source.toString());
        }
    }

    @Test
    void businessDoesNotDependOnPersistenceImplementations() throws IOException {
        for (Path source : sources(MAIN.resolve("business"))) {
            assertFalse(Files.readString(source).contains("persistence.text"), source.toString());
        }
    }

    @Test
    void controllersAreSingletons() {
        for (Class<?> controller : List.of(ParkController.class, UserController.class,
                PassController.class, InvoiceController.class)) {
            for (Constructor<?> c : controller.getDeclaredConstructors()) {
                assertTrue(Modifier.isPrivate(c.getModifiers()), controller.getSimpleName());
            }
        }
        assertSame(UserController.getInstance(), UserController.getInstance());
        assertSame(PassController.getInstance(), PassController.getInstance());
        assertSame(InvoiceController.getInstance(), InvoiceController.getInstance());
        assertSame(park.getUserController(), UserController.getInstance(), "collaboration injected");
    }

    @Test
    void repositoriesAreNotSingletons() {
        Path file = dir.resolve("users.txt");
        assertFalse(new TextUserRepository(file) == new TextUserRepository(file));
        for (Constructor<?> c : TextUserRepository.class.getConstructors()) {
            assertTrue(Modifier.isPublic(c.getModifiers()));
        }
    }

    @Test
    void replaceableRepositoriesChangeOnlyTheDirectory() throws IOException {
        Path other = Files.createDirectory(dir.resolve("other"));
        AppInitializer.initializeBusiness(other, HASHER);
        park.createUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");
        assertTrue(Files.exists(other.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
        AppInitializer.initializeBusiness(dir, HASHER);
        assertEquals(0, park.getUserController().getUsers().size());
    }
}
