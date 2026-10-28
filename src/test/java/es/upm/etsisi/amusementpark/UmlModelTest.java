package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Keeps {@code uml/model.puml} consistent with the model classes.
 */
class UmlModelTest {

    private static final String MODEL_PACKAGE = "es.upm.etsisi.amusementpark.business.model";
    private static final Path MODEL_SOURCES = Path.of("src/main/java/es/upm/etsisi/amusementpark/business/model");

    private static List<String> modelClassNames() throws IOException {
        try (Stream<Path> files = Files.list(MODEL_SOURCES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .sorted()
                    .toList();
        }
    }

    private static String diagram() throws IOException {
        return Files.readString(Path.of("uml/model.puml"));
    }

    @Test
    void everyModelClassAndFieldIsInTheDiagram() throws Exception {
        String uml = diagram();
        for (String name : modelClassNames()) {
            Class<?> type = Class.forName(MODEL_PACKAGE + "." + name);
            String declaration = (type.isInterface() ? "interface " : type.isEnum() ? "enum "
                    : Modifier.isAbstract(type.getModifiers()) ? "abstract class " : "class ") + name;
            assertTrue(uml.contains(declaration + " ") || uml.contains(declaration + "<"), "missing " + declaration);
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                    assertTrue(uml.contains(" " + field.getName() + " ") || uml.contains(": " + field.getName()),
                            "missing field " + name + "." + field.getName());
                }
            }
        }
    }

    @Test
    void diagramDeclaresOnlyExistingClasses() throws Exception {
        List<String> known = modelClassNames();
        for (String line : diagram().lines().map(String::trim).toList()) {
            String rest = line.startsWith("abstract class ") ? line.substring("abstract class ".length())
                    : line.startsWith("class ") ? line.substring("class ".length())
                    : line.startsWith("interface ") ? line.substring("interface ".length())
                    : line.startsWith("enum ") ? line.substring("enum ".length()) : null;
            if (rest != null) {
                String name = rest.substring(0, rest.indexOf(' '));
                if (name.indexOf('<') > 0) {
                    name = name.substring(0, name.indexOf('<'));
                }
                assertTrue(known.contains(name) || name.equals("ParkController"), "unknown class in diagram: " + name);
            }
        }
    }

    @Test
    void keyRelationshipsArePresent() throws Exception {
        String uml = diagram();
        for (String relation : List.of(
                "Ticket <|-- IndividualTicket",
                "Ticket <|-- GroupTicket",
                "GroupTicket \"1\" *--> \"1..*\" IndividualTicket : composition",
                "IndividualTicket \"*\" --> \"1\" User : user",
                "GroupTicket \"*\" --> \"0..1\" Group : group",
                "Group \"*\" o--> \"*\" User : members",
                "Attraction \"0..1\" o--> \"0..*\" User : occupants")) {
            assertTrue(uml.contains(relation), "missing relation " + relation);
        }
        assertTrue(!uml.contains("User \"") && !uml.contains("User -->"), "users must not reference other elements");
    }
}
