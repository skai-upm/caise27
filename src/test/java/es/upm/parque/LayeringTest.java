package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the package layering rules on the source tree.
 */
class LayeringTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final Path BASE = MAIN.resolve("es/upm/parque");

    private static List<Path> sources(Path dir) throws Exception {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void layerPackagesExistAndDefaultPackageIsUnused() throws Exception {
        assertTrue(Files.isDirectory(BASE.resolve("presentation")));
        assertTrue(Files.isDirectory(BASE.resolve("business")));
        assertTrue(Files.isDirectory(BASE.resolve("persistence")));
        try (Stream<Path> s = Files.list(MAIN)) {
            assertFalse(s.anyMatch(p -> p.toString().endsWith(".java")));
        }
        for (Path p : sources(MAIN)) {
            assertTrue(Files.readString(p).startsWith("package es.upm.parque"), p.toString());
        }
    }

    @Test
    void onlyPersistenceAccessesFiles() throws Exception {
        for (Path layer : List.of(BASE.resolve("presentation"), BASE.resolve("business"))) {
            for (Path p : sources(layer)) {
                String code = Files.readString(p);
                assertFalse(code.contains("java.nio.file"), p + " uses java.nio.file");
                assertFalse(code.contains("FileReader") || code.contains("FileWriter"), p + " uses file streams");
            }
        }
    }
}
