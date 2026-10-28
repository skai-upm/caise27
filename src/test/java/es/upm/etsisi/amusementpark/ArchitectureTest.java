package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Layered organization: presentation, business and persistence packages and their dependencies.
 */
class ArchitectureTest {

    private static final Path ROOT = Path.of("src/main/java/es/upm/etsisi/amusementpark");
    private static final String PACKAGE = "es.upm.etsisi.amusementpark.";

    private static List<Path> sources(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            return files.filter(file -> file.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> imports(Path file) throws IOException {
        return Files.readAllLines(file).stream().filter(line -> line.startsWith("import ")).toList();
    }

    @Test
    void layerPackagesExistAndNoDefaultPackage() throws IOException {
        assertTrue(Files.isDirectory(ROOT.resolve("presentation")));
        assertTrue(Files.isDirectory(ROOT.resolve("business")));
        assertTrue(Files.isDirectory(ROOT.resolve("persistence")));
        for (Path file : sources(Path.of("src/main/java"))) {
            assertTrue(Files.readString(file).contains("package es.upm.etsisi.amusementpark"), "no package: " + file);
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path file : sources(ROOT.resolve("presentation"))) {
            for (String line : imports(file)) {
                assertFalse(line.contains(PACKAGE + "persistence"), file + ": " + line);
                assertFalse(line.contains("java.nio.file.Files"), file + ": " + line);
            }
        }
    }

    @Test
    void businessDoesNotDependOnOtherLayers() throws IOException {
        for (Path file : sources(ROOT.resolve("business"))) {
            for (String line : imports(file)) {
                assertFalse(line.contains(PACKAGE + "persistence"), file + ": " + line);
                assertFalse(line.contains(PACKAGE + "presentation"), file + ": " + line);
                assertFalse(line.contains("java.io.") || line.contains("java.nio."), file + ": " + line);
            }
        }
    }
}
