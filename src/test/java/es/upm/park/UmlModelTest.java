package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Keeps {@code uml/model.puml} consistent with the production classes. */
class UmlModelTest {

    static final Path MODEL = Path.of("uml", "model.puml");
    static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "park");
    static List<String> lines;

    @BeforeAll
    static void load() throws IOException {
        lines = Files.readAllLines(MODEL);
    }

    static Set<String> declaredInModel(List<String> model) {
        Set<String> names = new TreeSet<>();
        for (String raw : model) {
            String[] t = raw.trim().split(" ");
            int i = t.length > 2 && t[0].equals("abstract") ? 1 : 0;
            if (t.length > i + 1 && (t[i].equals("class") || t[i].equals("enum") || t[i].equals("interface"))) {
                names.add(t[i + 1].replace("{", ""));
            }
        }
        return names;
    }

    @Test
    void everyProductionTypeIsInTheModelAndViceVersa() throws IOException {
        Set<String> code = new TreeSet<>();
        try (Stream<Path> files = Files.list(SOURCES)) {
            files.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".java"))
                    .forEach(n -> code.add(n.substring(0, n.length() - ".java".length())));
        }
        assertEquals(code, declaredInModel(lines));
    }

    @Test
    void isWrappedAsPlantUml() {
        assertTrue(lines.get(0).startsWith("@startuml"));
        assertEquals("@enduml", lines.get(lines.size() - 1).trim());
    }

    @Test
    void classBodiesHaveNoMethodsOrTechnicalLabels() {
        boolean inBody = false;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.endsWith("{") && !line.startsWith("package")) {
                inBody = true;
                continue;
            }
            if (line.equals("}")) {
                inBody = false;
                continue;
            }
            if (inBody) {
                assertFalse(line.contains("(") || line.contains("<<") || line.contains("{"), "in class body: " + line);
            }
        }
    }

    @Test
    void detectsMissingClass() {
        List<String> withoutUser = lines.stream().filter(l -> !l.trim().startsWith("class User ")).toList();
        assertFalse(declaredInModel(withoutUser).contains("User"));
    }
}
