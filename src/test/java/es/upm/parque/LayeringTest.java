package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the package layering of the production sources.
 */
public class LayeringTest {

    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "parque");

    private static List<Path> javaFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList());
        }
    }

    @Test
    void everyClassIsInANamedPackageAndLayersExist() throws IOException {
        for (Path file : javaFiles(SOURCES)) {
            assertTrue(Files.readString(file).startsWith("package es.upm.parque"), file.toString());
        }
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertFalse(javaFiles(SOURCES.resolve(layer)).isEmpty(), layer);
        }
        try (Stream<Path> root = Files.list(SOURCES)) {
            assertEquals(List.of("App.java", "ParkApplication.java"), root.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList()));
        }
    }

    @Test
    void onlyPersistenceAccessesFiles() throws IOException {
        for (String layer : List.of("presentation", "business")) {
            for (Path file : javaFiles(SOURCES.resolve(layer))) {
                String source = Files.readString(file);
                assertFalse(source.contains("java.nio.file") || source.contains("java.io.File"), file.toString());
                assertFalse(source.contains("import es.upm.parque.persistence.File"), file.toString());
            }
        }
    }
}
