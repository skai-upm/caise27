package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Checks the layer rules on the production sources.
 */
class LayeredArchitectureTest {

    private static final Path SOURCES = Path.of("src", "main", "java");
    private static final Path ROOT = SOURCES.resolve(Path.of("es", "upm", "parque"));

    private static List<Path> javaFiles(Path dir) throws IOException {
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> violations(Path layer, String... forbidden) throws IOException {
        return javaFiles(layer).stream().filter(f -> {
            String text = read(f);
            return Stream.of(forbidden).anyMatch(text::contains);
        }).map(Path::toString).toList();
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void layerPackagesExist() {
        assertTrue(Files.isDirectory(ROOT.resolve("presentation")));
        assertTrue(Files.isDirectory(ROOT.resolve("business")));
        assertTrue(Files.isDirectory(ROOT.resolve("persistence")));
    }

    @Test
    void everyClassDeclaresAPackage() throws IOException {
        List<String> missing = javaFiles(SOURCES).stream()
                .filter(f -> !read(f).startsWith("package es.upm.parque")).map(Path::toString).toList();
        assertEquals(List.of(), missing);
    }

    @Test
    void presentationDoesNotAccessPersistence() throws IOException {
        assertEquals(List.of(), violations(ROOT.resolve("presentation"),
                "es.upm.parque.persistence", "java.nio.file.Files", "java.io.FileWriter", "java.io.FileReader"));
    }

    @Test
    void domainModelDoesNotAccessFiles() throws IOException {
        assertEquals(List.of(), violations(ROOT.resolve("business").resolve("model"),
                "es.upm.parque.persistence", "java.nio.file", "java.io.File"));
    }
}
