package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml stays consistent with the code of the documented packages.
 */
class UmlModelConsistencyTest {

    private static final Path MODEL = Path.of("uml", "model.puml");
    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "parque");
    private static final String[] PACKAGES = {"model", "repository", "park", "persistence"};
    private static final String[] KINDS = {"abstract class ", "class ", "enum ", "interface "};

    private static List<String> lines() throws IOException {
        return Files.readAllLines(MODEL, StandardCharsets.UTF_8).stream().map(String::trim).toList();
    }

    /** Names declared in the diagram as classes, enumerations or interfaces. */
    private static Set<String> declaredInDiagram() throws IOException {
        Set<String> names = new TreeSet<>();
        for (String line : lines()) {
            for (String kind : KINDS) {
                if (line.startsWith(kind)) {
                    String rest = line.substring(kind.length()).trim();
                    int end = 0;
                    while (end < rest.length() && Character.isJavaIdentifierPart(rest.charAt(end))) {
                        end++;
                    }
                    names.add(rest.substring(0, end));
                    break;
                }
            }
        }
        return names;
    }

    private static Set<String> classesInCode() throws IOException {
        Set<String> names = new TreeSet<>();
        for (String pkg : PACKAGES) {
            try (Stream<Path> files = Files.list(SOURCES.resolve(pkg))) {
                files.map(p -> p.getFileName().toString())
                        .filter(n -> n.endsWith(".java"))
                        .map(n -> n.substring(0, n.length() - ".java".length()))
                        .forEach(names::add);
            }
        }
        return names;
    }

    @Test
    void diagramDeclaresExactlyTheDocumentedCodeClasses() throws IOException {
        assertEquals(classesInCode(), declaredInDiagram());
    }

    @Test
    void requiredRelationshipsArePresent() throws IOException {
        List<String> lines = lines();
        for (String required : new String[] {
                "Ticket <|-- IndividualTicket",
                "Ticket <|-- GroupTicket",
                "GroupTicket \"0..1\" *-- \"1..*\" IndividualTicket : composition",
                "IndividualTicket \"*\" --> \"1\" User : user",
                "GroupTicket \"*\" --> \"0..1\" Group : group",
                "Group \"*\" o--> \"*\" User : members",
                "Attraction \"*\" o--> \"*\" User : occupants"}) {
            assertTrue(lines.contains(required), required);
        }
    }

    @Test
    void noAssociationStartsAtUser() throws IOException {
        for (String line : lines()) {
            assertFalse(line.startsWith("User ") && (line.contains("-->") || line.contains("--")), line);
        }
    }
}
