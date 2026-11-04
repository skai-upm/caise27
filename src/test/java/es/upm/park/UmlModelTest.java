package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Keeps {@code uml/model.puml} consistent with the production classes and their packages. */
class UmlModelTest {

    static final Path MODEL = Path.of("uml", "model.puml");
    static final Path SOURCES = Path.of("src", "main", "java");
    static List<String> lines;

    @BeforeAll
    static void load() throws IOException {
        lines = Files.readAllLines(MODEL);
    }

    /** Class name → package, as declared inside {@code package x.y {} } blocks of the diagram. */
    static Map<String, String> declaredInModel(List<String> model) {
        Map<String, String> names = new TreeMap<>();
        Deque<String> blocks = new ArrayDeque<>();
        for (String raw : model) {
            String line = raw.trim();
            String[] t = line.split(" ");
            if (t[0].equals("package") && line.endsWith("{")) {
                blocks.push(t[1]);
                continue;
            }
            int i = t.length > 2 && t[0].equals("abstract") ? 1 : 0;
            boolean declaration = t.length > i + 1
                    && (t[i].equals("class") || t[i].equals("enum") || t[i].equals("interface"));
            if (declaration) {
                String name = t[i + 1].replace("{", "");
                int generic = name.indexOf('<');
                names.put(generic < 0 ? name : name.substring(0, generic), blocks.isEmpty() ? "" : blocks.peek());
                if (line.endsWith("{")) {
                    blocks.push("");
                }
            } else if (line.equals("}") && !blocks.isEmpty()) {
                blocks.pop();
            }
        }
        names.values().removeIf(String::isEmpty);
        return names;
    }

    static Map<String, String> declaredInCode() throws IOException {
        Map<String, String> code = new TreeMap<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String name = p.getFileName().toString();
                String pkg = SOURCES.relativize(p.getParent()).toString().replace('/', '.').replace('\\', '.');
                code.put(name.substring(0, name.length() - ".java".length()), pkg);
            }
        }
        return code;
    }

    @Test
    void everyProductionTypeIsInTheModelInItsPackageAndViceVersa() throws IOException {
        assertEquals(declaredInCode(), declaredInModel(lines));
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
        assertFalse(declaredInModel(withoutUser).containsKey("User"));
    }
}
