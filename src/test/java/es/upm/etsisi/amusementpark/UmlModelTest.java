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
 * Keeps {@code uml/model.puml} consistent with the code.
 */
class UmlModelTest {

    private static final String ROOT_PACKAGE = "es.upm.etsisi.amusementpark";
    private static final Path SOURCES = Path.of("src/main/java/es/upm/etsisi/amusementpark");
    /** Packages whose every type must be drawn (exceptions are deliberately not drawn). */
    private static final List<String> DRAWN_PACKAGES = List.of("business.model", "business.controller",
            "business.invoice", "business.repository", "business.security", "persistence");

    private static List<String> typeNames(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .filter(name -> !name.endsWith("Exception"))
                    .sorted()
                    .toList();
        }
    }

    private static List<String> allMainTypeNames() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".java"))
                    .map(name -> name.substring(0, name.length() - ".java".length()))
                    .toList();
        }
    }

    private static String diagram() throws IOException {
        return Files.readString(Path.of("uml/model.puml"));
    }

    private static String declaration(Class<?> type) {
        return (type.isInterface() ? "interface " : type.isEnum() ? "enum "
                : Modifier.isAbstract(type.getModifiers()) ? "abstract class " : "class ") + type.getSimpleName();
    }

    @Test
    void everyDrawnTypeIsDeclared() throws Exception {
        String uml = diagram();
        for (String pkg : DRAWN_PACKAGES) {
            for (String name : typeNames(SOURCES.resolve(pkg.replace('.', '/')))) {
                String declaration = declaration(Class.forName(ROOT_PACKAGE + "." + pkg + "." + name));
                assertTrue(uml.contains(declaration + " ") || uml.contains(declaration + "<"),
                        "missing " + declaration);
            }
        }
    }

    @Test
    void everyModelFieldIsInTheDiagram() throws Exception {
        String uml = diagram();
        for (String name : typeNames(SOURCES.resolve("business/model"))) {
            Class<?> type = Class.forName(ROOT_PACKAGE + ".business.model." + name);
            for (Field field : type.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                    assertTrue(uml.contains(" " + field.getName() + " ") || uml.contains(": " + field.getName()),
                            "missing field " + name + "." + field.getName());
                }
            }
        }
    }

    @Test
    void diagramDeclaresOnlyExistingTypes() throws Exception {
        List<String> known = allMainTypeNames();
        for (String line : diagram().lines().map(String::trim).toList()) {
            String rest = null;
            for (String keyword : List.of("abstract class ", "class ", "interface ", "enum ")) {
                if (rest == null && line.startsWith(keyword)) {
                    rest = line.substring(keyword.length());
                }
            }
            if (rest != null) {
                int end = 0;
                while (end < rest.length() && Character.isJavaIdentifierPart(rest.charAt(end))) {
                    end++;
                }
                String name = rest.substring(0, end);
                assertTrue(known.contains(name), "unknown type in diagram: " + name);
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
                "Attraction \"0..1\" o--> \"0..*\" User : occupants",
                "IndividualAdmission <|.. IndividualTicket",
                "GroupAdmission <|.. GroupTicket",
                "abstract class Attraction<T extends Admission>",
                "SpeedPass \"0..*\" --> \"1\" IndividualTicket : ticket",
                "SpecialPass \"0..1\" --> \"1\" IndividualTicket : ticket",
                "InvoiceBuilder <|-- UserInvoiceBuilder",
                "InvoiceBuilder <|-- GroupInvoiceBuilder",
                "class ParkController <<singleton>>",
                "class InvoiceController <<singleton>>",
                "InvoiceController --> \"0..1\" ParkController : park")) {
            assertTrue(uml.contains(relation), "missing relation " + relation);
        }
        assertTrue(!uml.contains("User \"") && !uml.contains("User -->"), "users must not reference other elements");
    }
}
