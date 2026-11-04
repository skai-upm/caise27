package com.parque;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Keeps uml/model.puml aligned with the production classes.
 */
class UmlModelTest {

    @Test
    void everyProductionClassIsDeclared() throws Exception {
        String model = Files.readString(Path.of("uml/model.puml"));
        assertTrue(model.strip().startsWith("@startuml"));
        assertTrue(model.strip().endsWith("@enduml"));
        List<String> classes;
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            classes = files.filter(p -> p.toString().endsWith(".java"))
                    .map(p -> p.getFileName().toString().replace(".java", "")).toList();
        }
        for (String name : classes) {
            assertTrue(model.contains("class " + name + " ") || model.contains("class " + name + "\n")
                    || model.contains("enum " + name + " ") || model.contains("interface " + name + " "),
                    "uml/model.puml must declare " + name);
        }
    }

    @Test
    void groupUserAssociationWithMultiplicities() throws Exception {
        String model = Files.readString(Path.of("uml/model.puml"));
        assertTrue(model.contains("Group \"0..*\" o-- \"0..*\" User : members"));
    }
}
