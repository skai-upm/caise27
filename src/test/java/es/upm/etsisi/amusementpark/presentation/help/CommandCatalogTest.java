package es.upm.etsisi.amusementpark.presentation.help;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/** Keeps the help registry in sync with the operations the command processor dispatches. */
class CommandCatalogTest {

    private static final Path PROCESSOR = Path.of("src", "main", "java", "es", "upm", "etsisi", "amusementpark",
            "presentation", "CommandProcessor.java");

    @Test
    void catalogOperationsMatchDispatchedOperations() throws IOException {
        Set<String> dispatched = new TreeSet<>();
        for (String line : Files.readAllLines(PROCESSOR, StandardCharsets.UTF_8)) {
            String t = line.trim();
            if (t.startsWith("case \"") && t.contains("\" ->")) {
                dispatched.add(t.substring(6, t.indexOf('"', 6)));
            }
        }
        Set<String> registered = new TreeSet<>(CommandCatalog.standard().operations());
        // recipient types of INVOICE CREATE are parameters, not operations
        dispatched.removeIf(op -> op.equals("USER") || op.equals("GROUP"));
        assertEquals(registered, dispatched);
    }
}
