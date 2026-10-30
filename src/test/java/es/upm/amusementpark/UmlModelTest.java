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
 * Checks that uml/model.puml matches the code: declared types exist with the right kind, inheritance and
 * implementation arrows hold, and named associations are fields of their owner (or its superclass).
 */
class UmlModelTest {

    private static final List<String> PACKAGES = List.of("es.upm.amusementpark.", "es.upm.amusementpark.presentation.",
            "es.upm.amusementpark.business.controller.", "es.upm.amusementpark.business.model.",
            "es.upm.amusementpark.business.invoice.", "es.upm.amusementpark.business.security.",
            "es.upm.amusementpark.persistence.", "es.upm.amusementpark.persistence.file.");

    private final List<String> lines = Files.readAllLines(Path.of("uml/model.puml")).stream().map(String::trim)
            .toList();

    UmlModelTest() throws IOException {
    }

    private static Class<?> type(String name) throws ClassNotFoundException {
        String simple = name.contains("<") ? name.substring(0, name.indexOf('<')) : name;
        for (String pkg : PACKAGES) {
            try {
                return Class.forName(pkg + simple);
            } catch (ClassNotFoundException e) {
                // try the next package
            }
        }
        throw new ClassNotFoundException(simple);
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
                Class<?> type = type(words[1]);
                assertFalse(Modifier.isAbstract(type.getModifiers()) || type.isInterface(), words[1]);
                checked++;
            } else if (line.startsWith("interface ")) {
                assertTrue(type(words[1]).isInterface(), words[1]);
                checked++;
            } else if (line.startsWith("enum ")) {
                assertTrue(type(words[1]).isEnum(), words[1]);
                checked++;
            }
        }
        assertEquals(53, checked);
    }

    @Test
    void inheritanceAndImplementationsMatchCode() throws ClassNotFoundException {
        int checked = 0;
        for (String line : lines) {
            String[] words = line.split(" ");
            if (line.contains(" <|-- ") || line.contains(" <|.. ")) {
                Class<?> parent = type(words[0]);
                Class<?> child = type(words[2]);
                assertTrue(parent.isAssignableFrom(child), line);
                assertEquals(line.contains(" <|.. "), parent.isInterface() && !child.isInterface(), line);
                checked++;
            }
        }
        assertEquals(25, checked);
    }

    @Test
    void namedAssociationsAreFields() throws Exception {
        int checked = 0;
        for (String line : lines) {
            if ((line.contains(" --> ") || line.contains(" *-- ")) && line.contains(" : ")) {
                Class<?> owner = type(line.substring(0, line.indexOf(' ')));
                String field = line.substring(line.lastIndexOf(" : ") + 3).trim();
                boolean found = false;
                for (Class<?> c = owner; c != null && !found; c = c.getSuperclass()) {
                    found = List.of(c.getDeclaredFields()).stream().anyMatch(f -> f.getName().equals(field));
                }
                assertTrue(found, line);
                checked++;
            }
        }
        assertEquals(35, checked);
    }
}
