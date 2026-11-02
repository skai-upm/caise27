package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Layering rules checked on the main source tree. */
class ArchitectureTest {

    private static final Path MAIN = Path.of("src/main/java/es/upm/etsisi/park");

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void layersExistAndNoDefaultPackage() throws IOException {
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertTrue(Files.isDirectory(MAIN.resolve(layer)), layer);
        }
        for (Path p : sources(Path.of("src/main/java"))) {
            assertTrue(Files.readString(p).contains("package es.upm.etsisi.park"), p + " has a package");
        }
    }

    @Test
    void businessAndPresentationDoNotTouchPersistence() throws IOException {
        for (String layer : List.of("business", "presentation")) {
            for (Path p : sources(MAIN.resolve(layer))) {
                String code = Files.readString(p);
                assertFalse(code.contains("import es.upm.etsisi.park.persistence"), p + " depends on persistence");
                assertFalse(code.contains("java.nio.file.Files"), p + " accesses files");
            }
        }
    }

    @Test
    void presentationDependsOnlyOnBusiness() throws IOException {
        for (Path p : sources(MAIN.resolve("business"))) {
            assertFalse(Files.readString(p).contains("import es.upm.etsisi.park.presentation"), p.toString());
        }
    }
}
