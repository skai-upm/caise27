package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Checks the layered package structure on the source files. */
class ArchitectureTest {

    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "parque");

    private static List<Path> sources(String subPackage) throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES.resolve(subPackage))) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void everyClassIsInALayerPackage() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            List<Path> all = files.filter(p -> p.toString().endsWith(".java")).toList();
            int layered = sources("presentation").size() + sources("business").size() + sources("persistence").size();
            assertEquals(all.size(), layered);
        }
        try (Stream<Path> files = Files.walk(Path.of("src", "main", "java"), 1)) {
            assertTrue(files.noneMatch(p -> p.toString().endsWith(".java")), "default package used");
        }
    }

    @Test
    void commandProcessingDoesNotTouchPersistence() throws IOException {
        String code = Files.readString(SOURCES.resolve("presentation/CommandProcessor.java"));
        assertFalse(code.contains("es.upm.parque.persistence"));
        assertFalse(code.contains("java.nio.file"));
    }

    @Test
    void modelDoesNoFileIo() throws IOException {
        for (Path p : sources("business/model")) {
            String code = Files.readString(p);
            assertFalse(code.contains("java.nio.file") || code.contains("java.io."), p.toString());
            assertFalse(code.contains("es.upm.parque.persistence"), p.toString());
        }
    }

    @Test
    void businessDoesNotDependOnPresentation() throws IOException {
        for (Path p : sources("business")) {
            assertFalse(Files.readString(p).contains("es.upm.parque.presentation"), p.toString());
        }
        for (Path p : sources("persistence")) {
            assertFalse(Files.readString(p).contains("es.upm.parque.presentation"), p.toString());
        }
    }
}
