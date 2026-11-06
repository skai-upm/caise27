package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks the layered organization of the production sources: every class is
 * in presentation, business or persistence, and imports respect the allowed
 * dependency directions.
 */
class ArchitectureTest {

    private static final String ROOT = "es.upm.park.";
    private static List<Path> sources;

    @BeforeAll
    static void collect() throws IOException {
        try (Stream<Path> walk = Files.walk(Path.of("src", "main", "java"))) {
            sources = walk.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static String packageOf(List<String> lines) {
        return lines.stream().filter(l -> l.startsWith("package ")).findFirst()
                .map(l -> l.substring("package ".length(), l.indexOf(';'))).orElse("");
    }

    private static void assertNoImport(String layer, String... forbidden) throws IOException {
        for (Path source : sources) {
            List<String> lines = Files.readAllLines(source);
            if (!packageOf(lines).startsWith(ROOT + layer)) {
                continue;
            }
            for (String line : lines) {
                for (String prefix : forbidden) {
                    assertFalse(line.startsWith("import " + prefix), source + ": " + line);
                }
            }
        }
    }

    @Test
    void everyClassBelongsToALayer() throws IOException {
        assertFalse(sources.isEmpty());
        for (Path source : sources) {
            String pkg = packageOf(Files.readAllLines(source));
            assertTrue(pkg.startsWith(ROOT + "presentation") || pkg.startsWith(ROOT + "business")
                    || pkg.startsWith(ROOT + "persistence"), source + " is in package '" + pkg + "'");
        }
    }

    /**
     * Presentation may only obtain the repository configuration as the
     * composition root (ParkRepositories); it never uses file stores or
     * individual repositories.
     */
    @Test
    void presentationDoesNotUseFileStores() throws IOException {
        assertNoImport("presentation", "es.upm.park.persistence.UserFileStore",
                "es.upm.park.persistence.GroupFileStore", "es.upm.park.persistence.TicketFileStore",
                "es.upm.park.persistence.AttractionFileStore", "es.upm.park.persistence.repository.UserRepository",
                "es.upm.park.persistence.repository.GroupRepository",
                "es.upm.park.persistence.repository.TicketRepository",
                "es.upm.park.persistence.repository.AttractionRepository");
    }

    @Test
    void modelDoesNotDependOnPersistenceOrPresentation() throws IOException {
        assertNoImport("business.model", "es.upm.park.persistence", "es.upm.park.presentation", "java.nio.file",
                "java.io");
    }

    @Test
    void lowerLayersDoNotDependOnPresentation() throws IOException {
        assertNoImport("business", "es.upm.park.presentation");
        assertNoImport("persistence", "es.upm.park.presentation");
    }
}
