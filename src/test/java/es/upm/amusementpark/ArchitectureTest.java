package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Static checks of the layered organisation (Delivery 3, F3-C01).
 */
class ArchitectureTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final Path ROOT = MAIN.resolve(Path.of("es", "upm", "amusementpark"));

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(f -> f.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void layerPackagesExistAndNoDefaultPackage() throws IOException {
        assertTrue(Files.isDirectory(ROOT.resolve("presentation")));
        assertTrue(Files.isDirectory(ROOT.resolve("business")));
        assertTrue(Files.isDirectory(ROOT.resolve("persistence")));
        for (Path source : sources(MAIN)) {
            assertTrue(Files.readString(source).startsWith("package es.upm.amusementpark"), source.toString());
        }
    }

    @Test
    void presentationDoesNotTouchPersistence() throws IOException {
        for (Path source : sources(ROOT.resolve("presentation"))) {
            String code = Files.readString(source);
            assertFalse(code.contains("es.upm.amusementpark.persistence"), source.toString());
            assertFalse(code.contains("java.nio.file"), source.toString());
        }
    }

    @Test
    void businessDoesNotDependOnPersistenceOrPresentation() throws IOException {
        for (Path source : sources(ROOT.resolve("business"))) {
            String code = Files.readString(source);
            assertFalse(code.contains("import es.upm.amusementpark.persistence"), source.toString());
            assertFalse(code.contains("import es.upm.amusementpark.presentation"), source.toString());
        }
    }

    @Test
    void modelHasNoFileIo() throws IOException {
        for (Path source : sources(ROOT.resolve(Path.of("business", "model")))) {
            String code = Files.readString(source);
            assertFalse(code.contains("import java.io."), source.toString());
            assertFalse(code.contains("import java.nio.file."), source.toString());
        }
    }
}
