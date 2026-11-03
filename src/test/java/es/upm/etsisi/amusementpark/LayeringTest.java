package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class LayeringTest {

    private static final Path ROOT = Path.of("src/main/java/es/upm/etsisi/amusementpark");

    private static List<Path> sources(Path directory) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            return walk.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void everyClassHasANamedPackage() throws IOException {
        for (Path source : sources(Path.of("src/main/java"))) {
            assertTrue(Files.readString(source).startsWith("package es.upm.etsisi.amusementpark"), source.toString());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path source : sources(ROOT.resolve("presentation"))) {
            assertFalse(Files.readString(source).contains("import es.upm.etsisi.amusementpark.persistence"),
                    source.toString());
        }
    }

    @Test
    void businessDoesNotTouchFilesOrPersistence() throws IOException {
        for (Path source : sources(ROOT.resolve("business"))) {
            String code = Files.readString(source);
            assertFalse(code.contains("import es.upm.etsisi.amusementpark.persistence"), source.toString());
            assertFalse(code.contains("import es.upm.etsisi.amusementpark.presentation"), source.toString());
            assertFalse(code.contains("java.nio.file"), source.toString());
            assertFalse(code.contains("java.io.File"), source.toString());
        }
    }
}
