package es.upm.amusementpark;

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
 * Checks the layered package organization of the sources.
 */
class ArchitectureTest {

    private static final Path ROOT = Path.of("src", "main", "java", "es", "upm", "amusementpark");

    private static List<Path> sources(String layer) throws IOException {
        try (Stream<Path> files = Files.walk(ROOT.resolve(layer))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void layersExistAndOnlyTheEntryPointIsAtTheRoot() throws IOException {
        for (String layer : List.of("presentation", "business", "persistence")) {
            assertFalse(sources(layer).isEmpty(), layer);
        }
        try (Stream<Path> root = Files.list(ROOT)) {
            assertEquals(List.of("App.java", "ApplicationContext.java"), root.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString()).sorted().toList());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path source : sources("presentation")) {
            assertFalse(Files.readString(source).contains("es.upm.amusementpark.persistence"), source.toString());
        }
    }

    @Test
    void modelDoesNotReadOrWriteFiles() throws IOException {
        for (Path source : sources("business/model")) {
            String code = Files.readString(source);
            assertFalse(code.contains("java.nio.file") || code.contains("es.upm.amusementpark.persistence"),
                    source.toString());
        }
    }

    @Test
    void businessDependsOnlyOnRepositoryInterfaces() throws IOException {
        for (Path source : sources("business")) {
            String code = Files.readString(source);
            assertFalse(code.contains("persistence.File"), source + " must not use file repositories");
        }
        assertTrue(Files.readString(ROOT.resolve("ApplicationContext.java")).contains("FileUserRepository"),
                "file repositories are created only in the composition root");
    }
}
