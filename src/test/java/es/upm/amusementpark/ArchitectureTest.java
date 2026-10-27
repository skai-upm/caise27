package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.exception.AttractionOperationException;
import es.upm.amusementpark.business.exception.GroupOperationException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.TicketOperationException;
import es.upm.amusementpark.business.exception.UserOperationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the layered package organisation and the dependency rules between
 * layers.
 */
class ArchitectureTest {

    private static final Path ROOT = Path.of("src", "main", "java", "es", "upm", "amusementpark");

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(f -> f.toString().endsWith(".java")).toList();
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void everyClassBelongsToALayerPackage() throws IOException {
        for (Path file : sources(ROOT)) {
            String layer = ROOT.relativize(file).getName(0).toString();
            assertTrue(List.of("presentation", "business", "persistence").contains(layer), file.toString());
            String expectedPackage = "package es.upm.amusementpark."
                    + ROOT.relativize(file.getParent()).toString().replace(file.getFileSystem().getSeparator(), ".")
                    + ";";
            assertTrue(read(file).contains(expectedPackage), file.toString());
        }
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Path file : sources(ROOT.resolve("presentation"))) {
            assertFalse(read(file).contains("es.upm.amusementpark.persistence"), file.toString());
            assertFalse(read(file).contains("FileStorage"), file.toString());
        }
    }

    @Test
    void domainObjectsDoNotHandlePersistence() throws IOException {
        for (Path file : sources(ROOT.resolve("business").resolve("model"))) {
            String source = read(file);
            assertFalse(source.contains("es.upm.amusementpark.persistence"), file.toString());
            assertFalse(source.contains("java.nio.file"), file.toString());
            assertFalse(source.contains("java.io"), file.toString());
        }
    }

    @Test
    void operationExceptionsShareABase() {
        for (Class<?> type : List.of(UserOperationException.class, GroupOperationException.class,
                TicketOperationException.class, AttractionOperationException.class)) {
            assertEquals(ParkException.class, type.getSuperclass(), type.getSimpleName());
        }
    }
}
