package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that {@code uml/model.puml} stays consistent with the source tree.
 */
class UmlModelTest {

    private static final Path UML = Path.of("uml", "model.puml");
    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "amusementpark");
    private static final Set<String> KEYWORDS = Set.of("class", "interface", "enum");

    private static String uml() throws IOException {
        return Files.readString(UML);
    }

    /**
     * Type names declared in the diagram ({@code class X}, {@code abstract class "X<T>" as X}, ...).
     */
    private static Set<String> declaredTypes() throws IOException {
        Set<String> declared = new TreeSet<>();
        for (String raw : uml().split("\n")) {
            String[] words = raw.strip().split(" ");
            int keyword = words[0].equals("abstract") ? 1 : 0;
            if (words.length > keyword + 1 && KEYWORDS.contains(words[keyword])) {
                int asIndex = List.of(words).indexOf("as");
                declared.add(asIndex > 0 ? words[asIndex + 1] : words[keyword + 1]);
            }
        }
        return declared;
    }

    private static Set<String> sourceTypes(Path dir) throws IOException {
        Set<String> types = new TreeSet<>();
        try (Stream<Path> files = Files.walk(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString();
                types.add(name.substring(0, name.length() - ".java".length()));
            }
        }
        return types;
    }

    @Test
    void fileIsWellFormed() throws IOException {
        List<String> lines = Files.readAllLines(UML).stream().map(String::strip).filter(l -> !l.isEmpty()).toList();
        assertTrue(lines.get(0).startsWith("@startuml"));
        assertEquals("@enduml", lines.get(lines.size() - 1));
    }

    @Test
    void everyModelTypeIsDeclared() throws IOException {
        Set<String> declared = declaredTypes();
        for (String type : sourceTypes(SOURCES.resolve(Path.of("business", "model")))) {
            assertTrue(declared.contains(type), "missing from diagram: " + type);
        }
    }

    @Test
    void delivery3DesignElementsAreDeclared() throws IOException {
        Set<String> declared = declaredTypes();
        Set<String> required = new TreeSet<>();
        required.addAll(sourceTypes(SOURCES.resolve(Path.of("business", "controller"))));
        required.addAll(sourceTypes(SOURCES.resolve(Path.of("business", "invoice"))));
        required.addAll(sourceTypes(SOURCES.resolve(Path.of("business", "security"))));
        required.addAll(List.of("UserRepository", "GroupRepository", "TicketRepository", "AttractionRepository",
                "PassRepository", "ParkInitializer"));
        for (String type : required) {
            assertTrue(declared.contains(type), "missing from diagram: " + type);
        }
    }

    @Test
    void diagramDeclaresNoUnknownTypes() throws IOException {
        Set<String> existing = sourceTypes(SOURCES);
        for (String type : declaredTypes()) {
            assertTrue(existing.contains(type), "diagram declares a type absent from the code: " + type);
        }
    }

    @Test
    void keyRelationshipsArePresent() throws IOException {
        String uml = uml();
        for (String relation : List.of(
                "Ticket <|-- IndividualTicket",
                "Ticket <|-- GroupTicket",
                "IndividualAccess <|.. IndividualTicket",
                "GroupAccess <|.. GroupTicket",
                "GroupTicket \"1\" *--> \"1..*\" IndividualTicket",
                "IndividualTicket \"*\" --> \"1\" User",
                "GroupTicket \"*\" --> \"0..1\" Group",
                "Group \"*\" --> \"*\" User",
                "Attraction \"*\" --> \"*\" User",
                "SpeedPass \"*\" --> \"1\" IndividualTicket",
                "SpecialPass \"0..1\" --> \"1\" IndividualTicket",
                "Attraction <|-- GroupAttraction : <<bind>> T -> GroupAccess",
                "InvoiceBuilder <|.. AbstractInvoiceBuilder",
                "UserController --> \"1\" UserRepository : <<inject>>",
                "class Park <<singleton>>")) {
            assertTrue(uml.contains(relation), "missing relation: " + relation);
        }
    }
}
