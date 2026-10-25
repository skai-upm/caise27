package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Enforces the layer dependency rules on the import statements of the production sources.
 */
class LayeringTest {

    private static final Path SOURCES = Path.of("src", "main", "java");
    private static final Path ROOT = SOURCES.resolve(Path.of("es", "upm", "parque"));

    private static List<String> importsOf(String layer) throws IOException {
        try (Stream<Path> files = Files.walk(ROOT.resolve(layer))) {
            return files.filter(f -> f.toString().endsWith(".java"))
                    .flatMap(LayeringTest::lines)
                    .filter(l -> l.startsWith("import "))
                    .toList();
        }
    }

    private static Stream<String> lines(Path file) {
        try {
            return Files.readAllLines(file).stream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void layerPackagesExist() {
        for (String layer : List.of("presentation", "business/model", "business/controller", "persistence")) {
            assertTrue(Files.isDirectory(ROOT.resolve(layer)), layer);
        }
    }

    @Test
    void everySourceDeclaresAPackage() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                assertTrue(lines(file).anyMatch(l -> l.startsWith("package es.upm.parque")), file.toString());
            }
        }
    }

    @Test
    void presentationDoesNotTouchPersistenceOrFiles() throws IOException {
        for (String line : importsOf("presentation")) {
            assertFalse(line.contains("es.upm.parque.persistence"), line);
            assertFalse(line.contains("java.nio.file") || line.contains("java.io.File"), line);
        }
    }

    @Test
    void modelDoesNotDependOnOtherLayersOrFiles() throws IOException {
        for (String line : importsOf("business/model")) {
            assertFalse(line.contains("es.upm.parque.persistence") || line.contains("es.upm.parque.presentation")
                    || line.contains("es.upm.parque.business.controller"), line);
            assertFalse(line.contains("java.nio.file") || line.contains("java.io."), line);
        }
    }

    @Test
    void controllersDoNotDependOnPresentation() throws IOException {
        for (String line : importsOf("business/controller")) {
            assertFalse(line.contains("es.upm.parque.presentation"), line);
        }
    }

    @Test
    void persistenceDependsOnlyOnTheModel() throws IOException {
        for (String line : importsOf("persistence")) {
            assertFalse(line.contains("es.upm.parque.presentation")
                    || line.contains("es.upm.parque.business.controller"), line);
        }
    }
}
