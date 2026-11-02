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
 * Checks the presentation / business / persistence layering by inspecting imports.
 */
class LayeringTest {
    private static final Path MAIN = Path.of("src/main/java/es/upm/etsisi/amusementpark");
    private static final String ROOT = "es.upm.etsisi.amusementpark.";

    private static List<Path> sources(String layer) throws IOException {
        try (Stream<Path> files = Files.walk(MAIN.resolve(layer))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void everyClassHasANonDefaultPackage() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> assertTrue(read(p).contains("package " + ROOT.substring(0, ROOT.length() - 1)), p
                            .toString()));
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path p : sources("presentation")) {
            assertFalse(read(p).contains("import " + ROOT + "persistence"), p.toString());
        }
    }

    @Test
    void businessDoesNotDependOnPersistence() throws IOException {
        for (Path p : sources("business")) {
            assertFalse(read(p).contains("import " + ROOT + "persistence"), p.toString());
            assertFalse(read(p).contains("import " + ROOT + "presentation"), p.toString());
        }
    }

    @Test
    void domainObjectsDoNotTouchFiles() throws IOException {
        for (String domain : List.of("business/user", "business/group", "business/ticket", "business/pass",
                "business/attraction", "business/invoice")) {
            for (Path p : sources(domain)) {
                assertFalse(read(p).contains("java.nio.file"), p.toString());
                assertFalse(read(p).contains("java.io.File"), p.toString());
            }
        }
    }
}
