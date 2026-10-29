package es.upm.amusementpark;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Keeps uml/model.puml consistent with the production classes. */
class UmlModelTest {

    private static final Path MODEL = Path.of("uml", "model.puml");
    private static final Path SOURCES = Path.of("src", "main", "java");

    private static String model() throws IOException {
        return Files.readString(MODEL, StandardCharsets.UTF_8);
    }

    static List<String> productionClasses() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            return files.map(p -> p.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .toList();
        }
    }

    static boolean declares(String model, String className) {
        for (String line : model.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("class " + className + " ") || trimmed.equals("class " + className)
                    || trimmed.startsWith("abstract class " + className + " ")
                    || trimmed.startsWith("interface " + className + " ")
                    || trimmed.startsWith("enum " + className + " ")) {
                return true;
            }
        }
        return false;
    }

    @Test
    void fileIsAPlantUmlDiagram() throws IOException {
        String text = model().trim();
        assertTrue(text.startsWith("@startuml"));
        assertTrue(text.endsWith("@enduml"));
    }

    @Test
    void everyProductionClassIsDeclared() throws IOException {
        String text = model();
        List<String> classes = productionClasses();
        assertFalse(classes.isEmpty());
        for (String className : classes) {
            assertTrue(declares(text, className), "missing in uml/model.puml: " + className);
        }
    }

    @Test
    void detectsMissingClass() {
        assertFalse(declares("@startuml\nclass User\n@enduml", "Group"));
    }

    @Test
    void keyRelationshipsDeclared() throws IOException {
        String text = model();
        for (String relation : List.of(
                "Ticket <|-- IndividualTicket",
                "Ticket <|-- GroupTicket",
                "GroupTicket *--> \"1..*\" IndividualTicket",
                "IndividualTicket --> \"1\" User",
                "GroupTicket --> \"0..1\" Group",
                "Group o--> \"0..*\" User",
                "Attraction --> \"0..*\" User")) {
            assertTrue(text.contains(relation), "missing relationship: " + relation);
        }
    }
}
