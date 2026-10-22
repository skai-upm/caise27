package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml matches the code: every diagram type exists with
 * the declared kind, every attribute is a field, every labelled relationship is
 * a field of the source type, and every inheritance edge holds.
 */
class UmlModelTest {

    private static final Pattern TYPE = Pattern.compile(
            "^\\s*(abstract class|class|enum|interface)\\s+(\\w+)");
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)");
    private static final Pattern ATTRIBUTE = Pattern.compile("^\\s*-\\s*(\\{static}\\s*)?(\\w+)\\s*:");
    private static final Pattern RELATION = Pattern.compile(
            "^(\\w+)\\s+(-->|\\*--)\\s+\"[^\"]+\"\\s+(\\w+)\\s*:\\s*(\\w+)\\s*$");
    private static final Pattern EXTENSION = Pattern.compile("^(\\w+)\\s+(<\\|--|<\\|\\.\\.)\\s+(\\w+)\\s*$");

    private static List<String> lines;
    private static final Map<String, Class<?>> types = new HashMap<>();

    @BeforeAll
    static void readDiagram() throws IOException, ClassNotFoundException {
        lines = Files.readAllLines(Path.of("uml", "model.puml"), StandardCharsets.UTF_8);
        String currentPackage = null;
        for (String line : lines) {
            Matcher pkg = PACKAGE.matcher(line);
            if (pkg.find()) {
                currentPackage = pkg.group(1);
            }
            Matcher type = TYPE.matcher(line);
            if (type.find()) {
                Class<?> clazz = Class.forName(currentPackage + "." + type.group(2));
                checkKind(type.group(1), clazz);
                types.put(type.group(2), clazz);
            }
        }
    }

    private static void checkKind(String kind, Class<?> clazz) {
        switch (kind) {
            case "abstract class" -> assertTrue(Modifier.isAbstract(clazz.getModifiers()), clazz.getName());
            case "enum" -> assertTrue(clazz.isEnum(), clazz.getName());
            case "interface" -> assertTrue(clazz.isInterface(), clazz.getName());
            default -> assertFalse(clazz.isInterface() || clazz.isEnum(), clazz.getName());
        }
    }

    private static Class<?> type(String name) {
        Class<?> clazz = types.get(name);
        if (clazz == null) {
            fail("Type " + name + " used in a relationship is not declared in the diagram");
        }
        return clazz;
    }

    private static void assertField(Class<?> owner, String field) {
        try {
            Field declared = owner.getDeclaredField(field);
            assertEquals(field, declared.getName());
        } catch (NoSuchFieldException e) {
            fail(owner.getSimpleName() + " has no field " + field);
        }
    }

    @Test
    void diagramIsASinglePlantUmlDocument() {
        assertTrue(lines.get(0).startsWith("@startuml"));
        assertEquals(1, lines.stream().filter(l -> l.startsWith("@startuml")).count());
        assertEquals("@enduml", lines.get(lines.size() - 1).trim());
    }

    @Test
    void coreModelTypesArePresent() {
        for (String name : List.of("Park", "User", "UserField", "Group", "Ticket", "IndividualTicket",
                "GroupTicket", "GroupDiscount", "Attraction")) {
            assertTrue(types.containsKey(name), name);
        }
    }

    @Test
    void attributesExistInCode() {
        Class<?> current = null;
        for (String line : lines) {
            Matcher type = TYPE.matcher(line);
            if (type.find()) {
                current = types.get(type.group(2));
            }
            Matcher attribute = ATTRIBUTE.matcher(line);
            if (current != null && attribute.find()) {
                assertField(current, attribute.group(2));
            }
        }
    }

    @Test
    void relationshipsAreFieldsOfTheSource() {
        int checked = 0;
        for (String line : lines) {
            Matcher relation = RELATION.matcher(line.trim());
            if (relation.find()) {
                type(relation.group(3));
                assertField(type(relation.group(1)), relation.group(4));
                checked++;
            }
        }
        assertTrue(checked >= 10, "expected the labelled associations to be checked");
    }

    @Test
    void usersHoldNoReferencesToTicketsGroupsOrAttractions() {
        for (Field field : type("User").getDeclaredFields()) {
            assertEquals(String.class, field.getType(), field.getName());
        }
    }

    @Test
    void inheritanceMatchesCode() {
        int checked = 0;
        for (String line : lines) {
            Matcher extension = EXTENSION.matcher(line.trim());
            if (extension.find()) {
                Class<?> parent = type(extension.group(1));
                Class<?> child = type(extension.group(3));
                assertTrue(parent.isAssignableFrom(child), child + " is not a " + parent);
                checked++;
            }
        }
        assertEquals(3, checked);
    }
}
