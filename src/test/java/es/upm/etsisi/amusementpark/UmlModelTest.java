package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Keeps uml/model.puml aligned with the production classes. */
class UmlModelTest {

    private static final Path MODEL = Path.of("uml", "model.puml");
    private static final Path SOURCES = Path.of("src", "main", "java", "es", "upm", "etsisi", "amusementpark");

    private static List<String> declarations() throws IOException {
        List<String> declared = new ArrayList<>();
        for (String line : Files.readAllLines(MODEL, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            for (String keyword : List.of("abstract class ", "class ", "interface ", "enum ")) {
                if (trimmed.startsWith(keyword)) {
                    String rest = trimmed.substring(keyword.length()).trim();
                    int end = rest.indexOf(' ');
                    declared.add(end < 0 ? rest : rest.substring(0, end));
                    break;
                }
            }
        }
        return declared;
    }

    @Test
    void modelIsDelimitedPlantUml() throws IOException {
        List<String> lines = Files.readAllLines(MODEL, StandardCharsets.UTF_8);
        assertTrue(lines.get(0).startsWith("@startuml"));
        assertTrue(lines.stream().map(String::trim).anyMatch("@enduml"::equals));
    }

    @Test
    void everyProductionTypeIsDeclared() throws IOException {
        List<String> declared = declarations();
        List<String> missing = new ArrayList<>();
        try (Stream<Path> files = Files.list(SOURCES)) {
            files.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".java"))
                    .map(n -> n.substring(0, n.length() - ".java".length()))
                    .filter(n -> !declared.contains(n))
                    .forEach(missing::add);
        }
        assertTrue(missing.isEmpty(), "Missing in uml/model.puml: " + missing);
    }

    @Test
    void classesShowNoMethods() throws IOException {
        for (String line : Files.readAllLines(MODEL, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("'")) {
                assertFalse(trimmed.contains("("), "Method-like member in model: " + trimmed);
            }
        }
    }
}
