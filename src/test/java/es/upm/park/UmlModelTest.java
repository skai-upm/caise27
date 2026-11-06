package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml matches the code: declared classes, kinds,
 * inheritance and associations (each association label names a field of the
 * source class whose type, or collection element type, is the target).
 */
class UmlModelTest {

    private static final String[] PACKAGES = {
        "es.upm.park.business.model.", "es.upm.park.business.controller.", "es.upm.park.business.service.",
        "es.upm.park.business.invoice.", "es.upm.park.persistence.repository.", "es.upm.park.presentation.help."};

    private static List<String> lines;

    @BeforeAll
    static void read() throws IOException {
        lines = Files.readAllLines(Path.of("uml", "model.puml")).stream().map(String::trim).toList();
    }

    private static Class<?> type(String simpleName) {
        for (String pkg : PACKAGES) {
            try {
                return Class.forName(pkg + simpleName);
            } catch (ClassNotFoundException e) {
                // try next package
            }
        }
        return fail("class in diagram not found in code: " + simpleName);
    }

    private static String firstWordAfter(String line, String keyword) {
        String rest = line.substring(keyword.length()).trim();
        int end = 0;
        while (end < rest.length() && Character.isJavaIdentifierPart(rest.charAt(end))) {
            end++;
        }
        return rest.substring(0, end);
    }

    @Test
    void isDelimitedPlantUml() {
        assertEquals("@startuml", lines.get(0));
        assertEquals("@enduml", lines.get(lines.size() - 1));
    }

    @Test
    void declaredClassesExistWithTheSameKind() {
        int checked = 0;
        for (String line : lines) {
            if (line.startsWith("abstract class ")) {
                assertTrue(Modifier.isAbstract(type(firstWordAfter(line, "abstract class")).getModifiers()), line);
                checked++;
            } else if (line.startsWith("class ")) {
                Class<?> c = type(firstWordAfter(line, "class"));
                assertFalse(c.isEnum() || c.isInterface() || Modifier.isAbstract(c.getModifiers()), line);
                checked++;
            } else if (line.startsWith("interface ")) {
                assertTrue(type(firstWordAfter(line, "interface")).isInterface(), line);
                checked++;
            } else if (line.startsWith("enum ")) {
                assertTrue(type(firstWordAfter(line, "enum")).isEnum(), line);
                checked++;
            }
        }
        assertEquals(43, checked, "classes checked");
    }

    @Test
    void inheritanceMatches() {
        List<String> edges = lines.stream().filter(l -> l.contains("<|--")).toList();
        assertTrue(edges.size() >= 7, "inheritance edges: " + edges.size());
        for (String edge : edges) {
            String[] parts = edge.split("<\\|--");
            Class<?> parent = type(parts[0].trim());
            Class<?> child = type(parts[1].trim());
            if (child.isInterface()) {
                assertTrue(List.of(child.getInterfaces()).contains(parent), edge);
            } else {
                assertEquals(parent, child.getSuperclass(), edge);
            }
        }
        List<String> realizations = lines.stream().filter(l -> l.contains("<|..")).toList();
        assertTrue(realizations.size() >= 7, "realizations: " + realizations.size());
        for (String edge : realizations) {
            String[] parts = edge.split("<\\|\\.\\.");
            Class<?> contract = type(parts[0].trim());
            assertTrue(contract.isInterface(), edge);
            assertTrue(List.of(type(parts[1].trim()).getInterfaces()).contains(contract), edge);
        }
    }

    @Test
    void associationsAndCompositionsMatchFields() {
        List<String> edges = new ArrayList<>();
        for (String line : lines) {
            if ((line.contains(" --> ") || line.contains(" *-- ")) && line.contains(":")) {
                edges.add(line);
            }
        }
        assertTrue(edges.size() >= 20, "too few associations: " + edges.size());
        for (String edge : edges) {
            String[] sides = edge.split(" --> | \\*-- ");
            String source = sides[0].trim().split("\\s+")[0];
            String rest = sides[1];
            String label = rest.substring(rest.indexOf(':') + 1).trim();
            String targetPart = rest.substring(0, rest.indexOf(':')).trim();
            String target = targetPart.substring(targetPart.lastIndexOf(' ') + 1);
            String fieldName = "composition".equals(label) ? "composition" : label;
            Field field = field(type(source), fieldName, edge);
            assertEquals(type(target), elementType(field.getGenericType()), edge);
        }
    }

    @Test
    void usersHoldNoReferencesToOtherModelElements() {
        for (Field f : type("User").getDeclaredFields()) {
            assertEquals(String.class, f.getType(), f.getName());
        }
        for (Field f : type("Group").getDeclaredFields()) {
            assertFalse(f.getGenericType().getTypeName().contains("Ticket"), f.getName());
        }
    }

    private static Field field(Class<?> owner, String name, String edge) {
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // look in the superclass
            }
        }
        return fail("association without matching field: " + edge);
    }

    private static Class<?> elementType(Type type) {
        if (type instanceof ParameterizedType p) {
            Type argument = p.getActualTypeArguments()[0];
            return (Class<?>) (argument instanceof ParameterizedType nested ? nested.getRawType() : argument);
        }
        return (Class<?>) type;
    }
}
