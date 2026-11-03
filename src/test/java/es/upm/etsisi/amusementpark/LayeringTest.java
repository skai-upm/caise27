package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Checks the package structure and the allowed dependencies between layers on the source tree. */
class LayeringTest {

    static final Path MAIN = Path.of("src/main/java/es/upm/etsisi/amusementpark");
    static final String BASE = "es.upm.etsisi.amusementpark.";

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> offenders(String layer, String... forbidden) throws IOException {
        return sources(MAIN.resolve(layer)).stream().filter(p -> {
            try {
                String src = Files.readString(p);
                return Stream.of(forbidden).anyMatch(src::contains);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }).map(Path::toString).toList();
    }

    @Test
    void layerPackagesExistAndEveryClassHasAPackage() throws IOException {
        for (String layer : new String[] {"presentation", "business", "business/model", "business/controller",
            "persistence"}) {
            assertTrue(Files.isDirectory(MAIN.resolve(layer)), layer);
        }
        for (Path p : sources(Path.of("src/main/java"))) {
            assertTrue(Files.readString(p).startsWith("package " + BASE.substring(0, BASE.length() - 1)), p.toString());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        assertEquals(List.of(), offenders("presentation", BASE + "persistence"));
    }

    @Test
    void modelDependsOnNoOtherLayerNorFileApis() throws IOException {
        assertEquals(List.of(), offenders("business/model", BASE + "persistence", BASE + "presentation",
                BASE + "business.controller", "java.nio.file", "java.io."));
    }

    @Test
    void businessDoesNotDependOnPresentation() throws IOException {
        assertEquals(List.of(), offenders("business", BASE + "presentation"));
    }

    @Test
    void persistenceDoesNotDependOnUpperLayers() throws IOException {
        assertEquals(List.of(), offenders("persistence", BASE + "presentation", BASE + "business.controller"));
    }
}
