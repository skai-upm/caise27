package es.upm.amusementpark;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks that the PlantUML class model corresponds to the production code.
 */
class UmlModelTest {

    private static final Path MODEL = Path.of("uml", "model.puml");
    private static final Path SOURCES = Path.of("src", "main", "java");

    private List<String> lines;

    @BeforeEach
    void setUp() throws IOException {
        lines = Files.readAllLines(MODEL, StandardCharsets.UTF_8).stream().map(String::trim).toList();
    }

    private Set<String> declaredTypes() {
        Set<String> declared = new TreeSet<>();
        for (String line : lines) {
            for (String keyword : new String[] {"abstract class ", "class ", "enum ", "interface "}) {
                if (line.startsWith(keyword)) {
                    String rest = line.substring(keyword.length());
                    int end = 0;
                    while (end < rest.length() && Character.isJavaIdentifierPart(rest.charAt(end))) {
                        end++;
                    }
                    declared.add(rest.substring(0, end));
                    break;
                }
            }
        }
        return declared;
    }

    private static Set<String> sourceTypes() throws IOException {
        Set<String> types = new TreeSet<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            files.filter(f -> f.toString().endsWith(".java"))
                    .forEach(f -> types.add(f.getFileName().toString().replace(".java", "")));
        }
        return types;
    }

    @Test
    void modelIsAPlantUmlDocument() {
        assertEquals("@startuml", lines.get(0));
        assertEquals("@enduml", lines.get(lines.size() - 1));
    }

    @Test
    void everyProductionTypeIsDeclaredAndViceVersa() throws IOException {
        assertEquals(sourceTypes(), declaredTypes());
    }

    @Test
    void ticketHierarchyAndCompositionAreShown() {
        assertTrue(lines.contains("abstract class Ticket {"));
        assertTrue(lines.contains("Ticket <|-- IndividualTicket"));
        assertTrue(lines.contains("Ticket <|-- GroupTicket"));
        assertTrue(lines.contains("GroupTicket \"1\" *-- \"1..*\" IndividualTicket : composition"));
        assertTrue(lines.contains("enum UserField {"));
    }

    @Test
    void domainRelationshipsHaveTheRequiredDirections() {
        assertTrue(lines.contains("Group \"0..*\" --> \"0..*\" User : members"));
        assertTrue(lines.contains("IndividualTicket \"0..*\" --> \"1\" User : user"));
        assertTrue(lines.contains("GroupTicket \"0..*\" --> \"0..1\" Group : group"));
        assertTrue(lines.contains("Attraction \"0..*\" --> \"0..*\" User : occupants"));
        assertFalse(lines.stream().anyMatch(l -> l.startsWith("User ") && l.contains("-->")));
    }

    @Test
    void delivery3DesignElementsAreShown() {
        for (String line : List.of(
                "interface AccessTicket",
                "interface IndividualAccess",
                "interface GroupAccess",
                "IndividualAccess <|.. IndividualTicket",
                "GroupAccess <|.. GroupTicket",
                "abstract class Attraction<T extends AccessTicket> {",
                "Attraction <|-- GeneralAttraction",
                "Attraction <|-- IndividualAttraction",
                "Attraction <|-- GroupAttraction",
                "Attraction <|-- SpecialAttraction",
                "SpeedPass \"0..*\" --> \"1\" IndividualTicket : speedPasses of ticket",
                "SpecialPass \"0..1\" --> \"1\" IndividualTicket : specialPass of ticket",
                "abstract class InvoiceBuilder {",
                "InvoiceBuilder <|-- UserInvoiceBuilder",
                "InvoiceBuilder <|-- GroupInvoiceBuilder",
                "UserService --> \"1\" UserRepository : users",
                "PassService --> \"1\" PassRepository : passes",
                "UserService --> \"1\" PasswordHasher : passwordHasher (injected)",
                "HelpGenerator --> \"1\" HelpFormatter : formatter (injected)")) {
            assertTrue(lines.contains(line), line);
        }
        for (String controller : List.of("ParkController", "UserService", "GroupService", "TicketService",
                "AttractionService", "InvoiceService", "PassService")) {
            assertTrue(lines.stream().anyMatch(l -> l.startsWith("class " + controller + " <<singleton>>")), controller);
        }
    }
}
