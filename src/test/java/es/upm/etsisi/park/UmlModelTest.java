package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

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
    private static final Path SOURCES = Path.of("src/main/java");

    private static List<String> lines() throws IOException {
        return Files.readAllLines(MODEL).stream().map(String::strip).toList();
    }

    /** Simple name to fully qualified name of every main source class. */
    private static java.util.Map<String, String> sourceClasses() throws IOException {
        java.util.Map<String, String> classes = new java.util.TreeMap<>();
        try (var files = Files.walk(SOURCES)) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String fqn = SOURCES.relativize(p).toString().replace(".java", "").replace('/', '.');
                classes.put(p.getFileName().toString().replace(".java", ""), fqn);
            }
        }
        return classes;
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
        assertEquals(new TreeSet<>(sourceClasses().keySet()), declared);
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
            String className = sourceClasses().get(owner);
            assertDoesNotThrow(() -> Class.forName(className).getDeclaredField(field), line);
            checked++;
        }
        assertFalse(checked == 0);
    }
}
