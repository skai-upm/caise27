package es.upm.park;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks that every type of the business and persistence layers is declared in uml/model.puml. */
class UmlModelConsistencyTest {

    private static final Path UML = Path.of("uml", "model.puml");
    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "park");
    private static final String[] PACKAGES = {"business/model", "business/controller", "business/exception", "business/invoice", "business/security",
        "persistence"};

    static List<String> missingTypes(String diagram, List<String> typeNames) {
        List<String> missing = new ArrayList<>();
        for (String name : typeNames) {
            boolean declared = false;
            for (String keyword : new String[] {"class ", "interface ", "enum "}) {
                if (diagram.contains(keyword + name + " ") || diagram.contains(keyword + name + "\n")
                        || diagram.contains(keyword + name + "<")) {
                    declared = true;
                }
            }
            if (!declared) {
                missing.add(name);
            }
        }
        return missing;
    }

    private static List<String> documentedTypeNames() throws IOException {
        List<String> names = new ArrayList<>();
        for (String pkg : PACKAGES) {
            try (Stream<Path> files = Files.list(SOURCES.resolve(Path.of(pkg)))) {
                files.map(file -> file.getFileName().toString())
                        .filter(name -> name.endsWith(".java"))
                        .map(name -> name.substring(0, name.length() - ".java".length()))
                        .forEach(names::add);
            }
        }
        return names;
    }

    @Test
    void everyDocumentedTypeAppearsInTheDiagram() throws IOException {
        String diagram = Files.readString(UML, StandardCharsets.UTF_8);
        assertTrue(diagram.startsWith("@startuml") && diagram.strip().endsWith("@enduml"));
        List<String> missing = missingTypes(diagram, documentedTypeNames());
        assertTrue(missing.isEmpty(), "Types missing from uml/model.puml: " + missing);
    }

    @Test
    void detectsAMissingType() {
        assertTrue(missingTypes("class Ticket {\n}\n", List.of("Ticket", "Group")).contains("Group"));
    }

    @Test
    void diagramShowsTicketHierarchyAndComposition() throws IOException {
        String diagram = Files.readString(UML, StandardCharsets.UTF_8);
        assertTrue(diagram.contains("abstract class Ticket"));
        assertTrue(diagram.contains("Ticket <|-- IndividualTicket"));
        assertTrue(diagram.contains("Ticket <|-- GroupTicket"));
        assertTrue(diagram.contains("GroupTicket *--> \"1..*\" IndividualTicket"));
        assertTrue(diagram.contains("GroupTicket --> \"0..1\" Group"));
    }

    @Test
    void diagramUsesLayerPackages() throws IOException {
        String diagram = Files.readString(UML, StandardCharsets.UTF_8);
        for (String pkg : new String[] {"es.upm.park.business.model", "es.upm.park.business.controller",
                "es.upm.park.business.exception", "es.upm.park.business.invoice", "es.upm.park.persistence",
                "es.upm.park.presentation"}) {
            assertTrue(diagram.contains("package \"" + pkg + "\""), pkg);
        }
    }
}
