package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that {@code uml/model.puml} stays consistent with the model classes in the source tree.
 */
class UmlModelTest {

    private static final Path UML = Path.of("uml", "model.puml");
    private static final Path MODEL_SOURCES = Path.of("src", "main", "java", "es", "upm", "amusementpark", "model");

    private static String uml() throws IOException {
        return Files.readString(UML);
    }

    @Test
    void fileIsWellFormed() throws IOException {
        List<String> lines = Files.readAllLines(UML).stream().map(String::strip).filter(l -> !l.isEmpty()).toList();
        assertTrue(lines.get(0).startsWith("@startuml"));
        assertEquals("@enduml", lines.get(lines.size() - 1));
    }

    @Test
    void everyModelTypeIsDeclared() throws IOException {
        String uml = uml();
        for (String type : modelTypes()) {
            assertTrue(uml.contains("class " + type + " ") || uml.contains("class " + type + "\n")
                    || uml.contains("enum " + type + " "), "missing from diagram: " + type);
        }
        assertTrue(uml.contains("class Park"));
    }

    @Test
    void diagramDeclaresNoUnknownTypes() throws IOException {
        Set<String> known = new TreeSet<>(modelTypes());
        known.add("Park");
        known.add("UserService");
        Set<String> declared = new TreeSet<>();
        for (String line : uml().split("\n")) {
            String[] words = line.strip().split(" ");
            int keyword = words[0].equals("abstract") ? 1 : 0;
            if (words.length > keyword + 1 && (words[keyword].equals("class") || words[keyword].equals("enum"))) {
                declared.add(words[keyword + 1]);
            }
        }
        assertEquals(known, declared);
    }

    @Test
    void keyRelationshipsArePresent() throws IOException {
        String uml = uml();
        assertTrue(uml.contains("Ticket <|-- IndividualTicket"));
        assertTrue(uml.contains("Ticket <|-- GroupTicket"));
        assertTrue(uml.contains("GroupTicket \"1\" *--> \"1..*\" IndividualTicket"));
        assertTrue(uml.contains("IndividualTicket \"*\" --> \"1\" User"));
        assertTrue(uml.contains("GroupTicket \"*\" --> \"0..1\" Group"));
        assertTrue(uml.contains("Group \"*\" --> \"*\" User"));
        assertTrue(uml.contains("Attraction \"*\" --> \"*\" User"));
    }

    private static List<String> modelTypes() throws IOException {
        List<String> types = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MODEL_SOURCES)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString();
                types.add(name.substring(0, name.length() - ".java".length()));
            }
        }
        return types;
    }
}
