package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Guards the presentation / business / persistence layering.
 */
class LayeringTest {

    private static final Path ROOT = Path.of("src", "main", "java", "es", "upm", "amusementpark");
    private static final List<String> FILE_IO = List.of("import java.io.File", "import java.nio.file.",
            "import java.io.FileReader", "import java.io.FileWriter");

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static void assertNoImports(String layer, List<String> forbidden) throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : sources(ROOT.resolve(layer))) {
            String code = Files.readString(file);
            for (String prefix : forbidden) {
                if (code.contains(prefix)) {
                    violations.add(file.getFileName() + " -> " + prefix);
                }
            }
        }
        if (!violations.isEmpty()) {
            fail(layer + " layer violations: " + violations);
        }
    }

    @Test
    void layersExist() {
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertTrue(Files.isDirectory(ROOT.resolve(layer)), layer);
        }
    }

    @Test
    void noDefaultPackage() throws IOException {
        for (Path file : sources(Path.of("src", "main", "java"))) {
            assertTrue(Files.readString(file).startsWith("package es.upm.amusementpark"), file.toString());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        List<String> forbidden = new ArrayList<>(FILE_IO);
        forbidden.add("import es.upm.amusementpark.persistence.");
        assertNoImports("presentation", forbidden);
    }

    @Test
    void businessIsIndependent() throws IOException {
        List<String> forbidden = new ArrayList<>(FILE_IO);
        forbidden.add("import es.upm.amusementpark.persistence.");
        forbidden.add("import es.upm.amusementpark.presentation.");
        forbidden.add("import java.io.PrintStream");
        assertNoImports("business", forbidden);
    }
}
