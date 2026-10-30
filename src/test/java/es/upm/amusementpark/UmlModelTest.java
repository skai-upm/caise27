package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml matches the code: declared types exist with the right kind, inheritance arrows match
 * superclasses and named associations match fields.
 */
class UmlModelTest {

    private static final String PACKAGE = "es.upm.amusementpark.";

    private final List<String> lines = Files.readAllLines(Path.of("uml/model.puml")).stream().map(String::trim)
            .toList();

    UmlModelTest() throws IOException {
    }

    private static Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName(PACKAGE + name);
    }

    @Test
    void declaredTypesExist() throws ClassNotFoundException {
        int checked = 0;
        for (String line : lines) {
            String[] words = line.split(" ");
            if (line.startsWith("abstract class ")) {
                assertTrue(Modifier.isAbstract(type(words[2]).getModifiers()), words[2]);
                checked++;
            } else if (line.startsWith("class ")) {
                assertFalse(Modifier.isAbstract(type(words[1]).getModifiers()), words[1]);
                checked++;
            } else if (line.startsWith("enum ")) {
                assertTrue(type(words[1]).isEnum(), words[1]);
                checked++;
            }
        }
        assertEquals(12, checked);
    }

    @Test
    void inheritanceMatchesCode() throws ClassNotFoundException {
        for (String line : lines) {
            if (line.contains(" <|-- ")) {
                String[] words = line.split(" ");
                assertEquals(type(words[0]), type(words[2]).getSuperclass(), line);
            }
        }
    }

    @Test
    void namedAssociationsAreFields() throws Exception {
        int checked = 0;
        for (String line : lines) {
            if ((line.contains(" --> ") || line.contains(" *-- ")) && line.contains(" : ")) {
                String owner = line.substring(0, line.indexOf(' '));
                String field = line.substring(line.lastIndexOf(" : ") + 3).trim();
                type(owner).getDeclaredField(field);
                checked++;
            }
        }
        assertEquals(13, checked);
    }
}
