package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml is consistent with the model code.
 */
class UmlModelTest {

    private static final Path PUML = Path.of("uml", "model.puml");
    private static final Path BUSINESS = Path.of("src", "main", "java", "es", "upm", "amusementpark", "business");

    private static String simpleName(String declaration) {
        String[] words = declaration.trim().split(" ");
        for (int i = 0; i < words.length - 1; i++) {
            if (words[i].equals("class") || words[i].equals("enum") || words[i].equals("interface")) {
                return words[i + 1].split("<")[0];
            }
        }
        return null;
    }

    @Test
    void fileIsWellFormed() throws IOException {
        List<String> lines = Files.readAllLines(PUML);
        assertTrue(lines.get(0).startsWith("@startuml"));
        assertTrue(lines.get(lines.size() - 1).startsWith("@enduml"));
    }

    @Test
    void everyModelTypeIsDeclaredAndAttributesExist() throws Exception {
        Set<String> declared = new HashSet<>();
        String current = null;
        for (String raw : Files.readAllLines(PUML)) {
            String line = raw.trim();
            String name = simpleName(line);
            if (name != null && !line.startsWith("'") && !line.contains("--")) {
                declared.add(name);
                current = line.endsWith("{") ? name : null;
                continue;
            }
            if (line.equals("}")) {
                current = null;
            } else if (current != null && line.startsWith("- ") && Character.isUpperCase(current.charAt(0))) {
                String field = line.substring(2).replace("{static}", "").trim().split(" ")[0];
                Class<?> type = resolve(current);
                if (type != null) {
                    String owner = current;
                    assertDoesNotThrow(() -> type.getDeclaredField(field), owner + "." + field);
                }
            }
        }
        for (String pkg : new String[] {"model", "controller", "invoice", "security"}) {
            try (Stream<Path> files = Files.list(BUSINESS.resolve(pkg))) {
                for (Path p : files.toList()) {
                    String cls = p.getFileName().toString().replace(".java", "");
                    assertTrue(declared.contains(cls), "missing in diagram: " + cls);
                }
            }
        }
        assertTrue(declared.contains("ParkController"));
    }

    private static Class<?> resolve(String simple) {
        for (String pkg : new String[] {"business.model", "business.controller", "business.invoice", "business.security",
                "persistence.repository", "persistence", "presentation"}) {
            try {
                return Class.forName("es.upm.amusementpark." + pkg + "." + simple);
            } catch (ClassNotFoundException e) {
                // try next package
            }
        }
        return null;
    }

    @Test
    void delivery3ElementsAreRepresented() throws IOException {
        String uml = Files.readString(PUML);
        for (String expected : new String[] {"interface IndividualAccess", "interface GroupAccess",
                "abstract class Attraction<T extends AttractionAccess>", "SpeedPass \"0..*\" --> \"1\" IndividualTicket",
                "SpecialPass \"0..1\" --> \"1\" IndividualTicket", "interface InvoiceBuilder",
                "class UserController <<singleton>>", "ApplicationInitializer ..> UserController : injects"}) {
            assertTrue(uml.contains(expected), expected);
        }
    }
}
