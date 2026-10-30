package es.upm.etsisi.amusementpark;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ArchitectureTest {

    private static final Path ROOT = Path.of("src/main/java/es/upm/etsisi/amusementpark");

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void everyClassBelongsToALayer() throws IOException {
        for (Path file : sources(ROOT)) {
            Path relative = ROOT.relativize(file);
            if (relative.getNameCount() == 1) {
                assertTrue(List.of("App.java", "ApplicationInitializer.java").contains(relative.toString()), relative.toString());
            } else {
                assertTrue(List.of("presentation", "business", "persistence").contains(relative.getName(0).toString()),
                        relative.toString());
            }
        }
    }

    @Test
    void presentationDoesNotTouchFiles() throws IOException {
        for (Path file : sources(ROOT.resolve("presentation"))) {
            String code = Files.readString(file);
            assertFalse(code.contains("java.nio.file"), file.toString());
            assertFalse(code.contains("FileStore") || code.contains("LineFile"), file.toString());
        }
    }

    @Test
    void domainObjectsDoNotPersistThemselves() throws IOException {
        for (Path file : sources(ROOT.resolve("business/model"))) {
            String code = Files.readString(file);
            assertFalse(code.contains("java.nio.file") || code.contains(".persistence."), file.toString());
        }
    }
}
