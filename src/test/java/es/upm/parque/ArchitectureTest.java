package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the layered package structure on the source tree.
 */
class ArchitectureTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final Path ROOT = MAIN.resolve(Path.of("es", "upm", "parque"));

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void noClassInTheDefaultPackage() throws IOException {
        for (Path source : sources(MAIN)) {
            assertTrue(Files.readString(source).startsWith("package es.upm.parque"), source.toString());
        }
    }

    @Test
    void layersExistAndRootHoldsOnlyTheBootstrap() throws IOException {
        for (String layer : new String[] {"presentation", "business", "persistence"}) {
            assertFalse(sources(ROOT.resolve(layer)).isEmpty(), layer);
        }
        try (Stream<Path> root = Files.list(ROOT)) {
            assertEquals(List.of("App.java", "ParkInitializer.java"),
                    root.filter(Files::isRegularFile).map(p -> p.getFileName().toString()).sorted().toList());
        }
    }

    @Test
    void onlyPersistenceTouchesDataFiles() throws IOException {
        for (String layer : new String[] {"presentation", "business"}) {
            for (Path source : sources(ROOT.resolve(layer))) {
                String code = Files.readString(source);
                assertFalse(code.contains("java.nio.file") || code.contains("java.io.File"), source.toString());
                assertFalse(code.contains("es.upm.parque.persistence"), source.toString());
            }
        }
    }
}
