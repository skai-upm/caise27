package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Keeps {@code uml/model.puml} consistent with the production code.
 */
class UmlModelTest {

    private static String model;

    @BeforeAll
    static void load() throws IOException {
        model = Files.readString(Path.of("uml", "model.puml"));
    }

    @Test
    void isDelimitedPlantUml() {
        assertTrue(model.trim().startsWith("@startuml"));
        assertTrue(model.trim().endsWith("@enduml"));
    }

    /** @return the declaration kind written in the diagram for the given source, e.g. "abstract class" */
    static String kindOf(String source, String name) {
        for (String kind : List.of("abstract class", "interface", "enum", "class")) {
            for (String prefix : List.of("public ", "public final ", "final ", "")) {
                if (source.contains("\n" + prefix + kind + " " + name + " ")) {
                    return kind;
                }
            }
        }
        return null;
    }

    @Test
    void everyProductionTypeIsDeclaredWithItsKind() throws IOException {
        List<String> missing = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("src", "main", "java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString().replace(".java", "");
                String kind = kindOf(Files.readString(file), name);
                if (kind == null || !(model.contains(kind + " " + name + " {")
                        || model.contains(kind + " " + name + " <<"))) {
                    missing.add(kind + " " + name);
                }
            }
        }
        if (!missing.isEmpty()) {
            fail("uml/model.puml does not declare: " + missing);
        }
    }

    @Test
    void keyRelationshipsAreDrawn() {
        for (String edge : List.of("Ticket <|-- IndividualTicket", "Ticket <|-- GroupTicket",
                "GroupTicket *-- \"1..*\" IndividualTicket", "GroupTicket --> \"0..1\" Group",
                "ParkController --> \"0..*\" Attraction", "ParkController --> \"0..*\" Group",
                "ParkController --> \"0..*\" Ticket")) {
            assertTrue(model.contains(edge), "missing relationship: " + edge);
        }
    }
}
