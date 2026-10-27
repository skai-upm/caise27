package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the Delivery 1 technical restrictions on the production sources.
 */
class TechnicalRestrictionsTest {

    private static final Path MAIN = Path.of("src", "main", "java", "es", "upm", "amusementpark");

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void onlyStandardLibraryAndNoRegularExpressions() throws IOException {
        for (Path source : sources()) {
            for (String line : Files.readAllLines(source)) {
                String l = line.trim();
                if (l.startsWith("import ")) {
                    assertTrue(l.startsWith("import java.") || l.startsWith("import static java.")
                            || l.startsWith("import es.upm.amusementpark."), source + ": " + l);
                }
                for (String forbidden : new String[] {"java.util.regex", ".matches(", "replaceAll(", "replaceFirst(",
                        "ObjectOutputStream", "ObjectInputStream", "Serializable"}) {
                    assertFalse(l.contains(forbidden), source + ": " + l);
                }
            }
        }
    }

    @Test
    void userMainAndPersistenceAreSeparated() throws IOException {
        String user = Files.readString(MAIN.resolve("model/User.java"));
        String main = Files.readString(MAIN.resolve("Main.java"));
        String store = Files.readString(MAIN.resolve("persistence/UserFileStore.java"));
        assertFalse(user.contains("import java.io") || user.contains("import java.nio"));
        assertFalse(main.contains("Files.write") || main.contains("Files.readAllLines") || main.contains("Files.delete"));
        assertTrue(store.contains("Files.write") && store.contains("Files.readAllLines"));
    }
}
