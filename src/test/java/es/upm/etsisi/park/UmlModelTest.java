package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/** Checks that uml/model.puml matches the classes and field-based associations in the code. */
class UmlModelTest {

    private static final Path MODEL = Path.of("uml/model.puml");
    private static final Path SOURCES = Path.of("src/main/java/es/upm/etsisi/park");
    private static final String PACKAGE = "es.upm.etsisi.park.";

    private static List<String> lines() throws IOException {
        return Files.readAllLines(MODEL).stream().map(String::strip).toList();
    }

    @Test
    void diagramDeclaresExactlyTheSourceClasses() throws IOException {
        Set<String> declared = new TreeSet<>();
        for (String line : lines()) {
            for (String keyword : List.of("abstract class ", "class ", "enum ", "interface ")) {
                if (line.startsWith(keyword)) {
                    declared.add(line.substring(keyword.length()).split("[ {<]")[0]);
                    break;
                }
            }
        }
        Set<String> sources = new TreeSet<>();
        try (var files = Files.list(SOURCES)) {
            files.map(p -> p.getFileName().toString().replace(".java", "")).forEach(sources::add);
        }
        assertEquals(sources, declared);
    }

    @Test
    void navigableAssociationsMatchFields() throws IOException {
        int checked = 0;
        for (String line : lines()) {
            boolean association = line.contains("-->") && !line.contains("..>") && line.contains(" : ");
            if (!association) {
                continue;
            }
            String owner = line.split(" ")[0];
            String field = line.substring(line.indexOf(" : ") + 3).split("\\\\n")[0].strip();
            assertDoesNotThrow(() -> Class.forName(PACKAGE + owner).getDeclaredField(field), line);
            checked++;
        }
        assertFalse(checked == 0);
    }
}
