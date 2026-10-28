package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks that the PlantUML class model matches the code.
 */
class UmlModelConsistencyTest {

    private static final Path MODEL = Path.of("uml", "model.puml");
    private static final List<String> KEYWORDS = List.of("abstract class ", "class ", "interface ", "enum ");

    /** Declared type name to its package. */
    private static Map<String, String> declaredTypes() throws IOException {
        Map<String, String> types = new HashMap<>();
        String currentPackage = null;
        for (String raw : Files.readAllLines(MODEL)) {
            String line = raw.trim();
            if (line.startsWith("package ")) {
                currentPackage = line.substring("package ".length(), line.indexOf('{')).trim();
                continue;
            }
            for (String keyword : KEYWORDS) {
                if (line.startsWith(keyword)) {
                    String name = line.substring(keyword.length()).split("[ <]")[0].trim();
                    types.put(name, currentPackage);
                    break;
                }
            }
        }
        return types;
    }

    @Test
    void fileIsAPlantUmlDiagram() throws IOException {
        String text = Files.readString(MODEL).trim();
        assertTrue(text.startsWith("@startuml"));
        assertTrue(text.endsWith("@enduml"));
        assertTrue(text.contains("abstract class Ticket"));
        assertTrue(text.contains("Ticket <|-- IndividualTicket"));
        assertTrue(text.contains("Ticket <|-- GroupTicket"));
        assertTrue(text.contains("GroupTicket \"1\" *-- \"1..*\" IndividualTicket"));
    }

    @Test
    void everyDeclaredTypeExistsInTheCode() throws IOException {
        for (Map.Entry<String, String> type : declaredTypes().entrySet()) {
            String qualified = type.getValue() + "." + type.getKey();
            assertDoesNotThrow(() -> Class.forName(qualified), qualified);
        }
    }

    @Test
    void everyModelTypeIsInTheDiagram() throws IOException {
        Map<String, String> declared = declaredTypes();
        Path modelSources = Path.of("src", "main", "java", "es", "upm", "amusementpark", "business", "model");
        try (Stream<Path> files = Files.list(modelSources)) {
            for (Path file : files.toList()) {
                String name = file.getFileName().toString().replace(".java", "");
                assertTrue("es.upm.amusementpark.business.model".equals(declared.get(name)), name + " missing in uml/model.puml");
            }
        }
    }
}
