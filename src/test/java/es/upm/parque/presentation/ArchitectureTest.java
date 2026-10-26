package es.upm.parque.presentation;

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
 * Checks the layered package structure of the main source tree.
 */
class ArchitectureTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final List<String> LAYERS = List.of("es.upm.parque.presentation", "es.upm.parque.business",
            "es.upm.parque.persistence");

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static String packageOf(String source) {
        return source.lines().filter(l -> l.startsWith("package ")).findFirst()
                .map(l -> l.substring("package ".length(), l.length() - 1)).orElse("");
    }

    @Test
    void everyClassBelongsToALayerPackageMatchingItsDirectory() throws IOException {
        List<Path> sources = sources();
        assertTrue(sources.size() > 0);
        for (Path p : sources) {
            String pkg = packageOf(Files.readString(p));
            assertFalse(pkg.isEmpty(), p + " is in the default package");
            assertTrue(LAYERS.stream().anyMatch(pkg::startsWith), p + " is outside the layers: " + pkg);
            assertEquals(MAIN.relativize(p.getParent()).toString().replace('/', '.').replace('\\', '.'), pkg,
                    p + " does not match its directory");
        }
    }

    @Test
    void commandProcessingDoesNotTouchPersistence() throws IOException {
        String source = Files.readString(MAIN.resolve("es/upm/parque/presentation/CommandProcessor.java"));
        assertFalse(source.contains("import es.upm.parque.persistence"));
        assertFalse(source.contains("import java.io"));
        assertFalse(source.contains("import java.nio.file"));
    }

    @Test
    void domainModelDoesNotHandleFiles() throws IOException {
        for (Path p : sources()) {
            String source = Files.readString(p);
            if (packageOf(source).equals("es.upm.parque.business.model")) {
                assertFalse(source.contains("import java.io"), p.toString());
                assertFalse(source.contains("import java.nio.file"), p.toString());
                assertFalse(source.contains("import es.upm.parque.persistence"), p.toString());
            }
        }
    }
}
