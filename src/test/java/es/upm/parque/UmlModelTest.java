package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that uml/model.puml mirrors the production classes: elements, attributes,
 * relationships named after fields, inheritance and usage lines.
 */
class UmlModelTest {

    private static final String PACKAGE = "es.upm.parque.";
    private static final Pattern PACKAGE_BLOCK = Pattern.compile("^package ([\\w.]+) \\{$");
    private static final Pattern CLASS = Pattern.compile("^\\s*(?:abstract )?(class|enum|interface) (\\w+)(?:<[^>]*>)?(?: \\{)?$");
    private static final Pattern ATTRIBUTE = Pattern.compile("^\\s*(\\w+) : (\\w+)$");
    private static final Pattern RELATION =
            Pattern.compile("^(\\w+) \"([^\"]+)\" (x-->|o-->|\\*-->) \"([^\"]+)\" (\\w+) : (\\w+)$");
    private static final Pattern INHERITANCE = Pattern.compile("^(\\w+) --\\|> (\\w+)$");
    private static final Pattern REALIZATION = Pattern.compile("^(\\w+) \\.\\.\\|> (\\w+)$");
    private static final Pattern USAGE = Pattern.compile("^(\\w+) \\.\\.> (\\w+) : .+$");

    private static final Path SOURCES = Path.of("src", "main", "java");
    private static final Map<String, String> qualifiedNames = new HashMap<>();
    private static final Map<String, String> modelPackages = new HashMap<>();
    private static final Map<String, Map<String, String>> attributes = new HashMap<>();
    private static final Map<String, List<String>> enumValues = new HashMap<>();
    private static final List<String[]> relations = new ArrayList<>();
    private static final List<String[]> inheritances = new ArrayList<>();
    private static final List<String[]> realizations = new ArrayList<>();
    private static final List<String[]> usages = new ArrayList<>();

    @BeforeAll
    static void parseModel() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String name = file.getFileName().toString().replace(".java", "");
                StringBuilder qualified = new StringBuilder();
                for (Path part : SOURCES.relativize(file.getParent())) {
                    qualified.append(part).append('.');
                }
                qualifiedNames.put(name, qualified + name);
            }
        }
        String current = null;
        String currentPackage = null;
        boolean isEnum = false;
        for (String line : Files.readAllLines(Path.of("uml", "model.puml"))) {
            Matcher m;
            if ((m = PACKAGE_BLOCK.matcher(line)).matches()) {
                currentPackage = m.group(1);
            } else if ((m = CLASS.matcher(line)).matches()) {
                current = m.group(2);
                modelPackages.put(current, currentPackage);
                isEnum = m.group(1).equals("enum");
                attributes.put(current, new HashMap<>());
                if (isEnum) {
                    enumValues.put(current, new ArrayList<>());
                }
                if (!line.trim().endsWith("{")) {
                    current = null;
                }
            } else if (line.trim().equals("}")) {
                current = null;
            } else if (current != null && isEnum && !line.isBlank()) {
                enumValues.get(current).add(line.trim());
            } else if (current != null && (m = ATTRIBUTE.matcher(line)).matches()) {
                attributes.get(current).put(m.group(1), m.group(2));
            } else if ((m = RELATION.matcher(line)).matches()) {
                relations.add(new String[] {m.group(1), m.group(5), m.group(6)});
            } else if ((m = INHERITANCE.matcher(line)).matches()) {
                inheritances.add(new String[] {m.group(1), m.group(2)});
            } else if ((m = REALIZATION.matcher(line)).matches()) {
                realizations.add(new String[] {m.group(1), m.group(2)});
            } else if ((m = USAGE.matcher(line)).matches()) {
                usages.add(new String[] {m.group(1), m.group(2)});
            }
        }
    }

    private static Class<?> modelClass(String name) {
        try {
            return Class.forName(qualifiedNames.getOrDefault(name, PACKAGE + name));
        } catch (ClassNotFoundException e) {
            throw new AssertionError("model element without class: " + name, e);
        }
    }

    private static boolean isModelType(Class<?> type) {
        return type.getName().startsWith(PACKAGE);
    }

    /** Model classes referenced by a field, directly or as List/Map element types. */
    private static Set<String> referencedModelTypes(Field field) {
        Set<String> referenced = new HashSet<>();
        if (isModelType(field.getType())) {
            referenced.add(field.getType().getSimpleName());
        }
        if (field.getGenericType() instanceof ParameterizedType parameterized) {
            for (Type argument : parameterized.getActualTypeArguments()) {
                Type raw = argument instanceof ParameterizedType nested ? nested.getRawType() : argument;
                if (raw instanceof Class<?> c && isModelType(c)) {
                    referenced.add(c.getSimpleName());
                }
            }
        }
        return referenced;
    }

    private static List<Field> instanceFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()) && !f.isSynthetic())
                .toList();
    }

    @Test
    void everyProductionClassIsInTheModelAndViceVersa() {
        assertEquals(qualifiedNames.keySet(), attributes.keySet());
        attributes.keySet().forEach(UmlModelTest::modelClass);
    }

    @Test
    void classesAreDrawnInsideTheirPackage() {
        for (Map.Entry<String, String> entry : modelPackages.entrySet()) {
            assertEquals(qualifiedNames.get(entry.getKey()), entry.getValue() + "." + entry.getKey());
        }
    }

    @Test
    void attributesAreValueFieldsOfTheClass() throws NoSuchFieldException {
        for (Map.Entry<String, Map<String, String>> entry : attributes.entrySet()) {
            Class<?> type = modelClass(entry.getKey());
            for (Map.Entry<String, String> attribute : entry.getValue().entrySet()) {
                Field field = type.getDeclaredField(attribute.getKey());
                assertEquals(field.getType().getSimpleName(), attribute.getValue(),
                        entry.getKey() + "." + field.getName());
                assertTrue(referencedModelTypes(field).isEmpty() || field.getType().isEnum(), field.toString());
            }
        }
    }

    @Test
    void relationshipsAreFieldsReferencingTheTarget() throws NoSuchFieldException {
        for (String[] relation : relations) {
            Field field = modelClass(relation[0]).getDeclaredField(relation[2]);
            assertTrue(referencedModelTypes(field).contains(relation[1]), String.join(" ", relation));
        }
    }

    @Test
    void everyFieldIsShownAsAttributeOrRelationship() {
        for (String name : attributes.keySet()) {
            Class<?> type = modelClass(name);
            if (type.isEnum()) {
                continue;
            }
            for (Field field : instanceFields(type)) {
                boolean asRelation = relations.stream()
                        .anyMatch(r -> r[0].equals(name) && r[2].equals(field.getName()));
                boolean asAttribute = attributes.get(name).containsKey(field.getName());
                assertTrue(asRelation ^ asAttribute, name + "." + field.getName());
                assertEquals(!referencedModelTypes(field).isEmpty(), asRelation, name + "." + field.getName());
            }
        }
    }

    @Test
    void inheritanceMatchesTheCode() {
        Set<String> modelled = inheritances.stream().map(i -> i[0] + ">" + i[1]).collect(Collectors.toSet());
        Set<String> actual = new HashSet<>();
        for (String name : attributes.keySet()) {
            Class<?> type = modelClass(name);
            Class<?> superclass = type.getSuperclass();
            if (superclass != null && isModelType(superclass)) {
                actual.add(name + ">" + superclass.getSimpleName());
            }
            if (type.isInterface()) {
                for (Class<?> extended : type.getInterfaces()) {
                    actual.add(name + ">" + extended.getSimpleName());
                }
            }
        }
        assertEquals(actual, modelled);
    }

    @Test
    void realizationsMatchTheCode() {
        Set<String> modelled = realizations.stream().map(r -> r[0] + ">" + r[1]).collect(Collectors.toSet());
        Set<String> actual = new HashSet<>();
        for (String name : attributes.keySet()) {
            if (modelClass(name).isInterface()) {
                continue;
            }
            for (Class<?> implemented : modelClass(name).getInterfaces()) {
                if (isModelType(implemented)) {
                    actual.add(name + ">" + implemented.getSimpleName());
                }
            }
        }
        assertEquals(actual, modelled);
    }

    @Test
    void enumerationsListTheirConstants() {
        for (Map.Entry<String, List<String>> entry : enumValues.entrySet()) {
            List<String> constants = Arrays.stream(modelClass(entry.getKey()).getEnumConstants())
                    .map(c -> ((Enum<?>) c).name()).toList();
            assertEquals(constants, entry.getValue());
        }
    }

    @Test
    void usageOnlyForClassesWithoutIncomingRelationships() {
        Set<String> withIncoming = relations.stream().map(r -> r[1]).collect(Collectors.toSet());
        assertFalse(usages.isEmpty());
        for (String[] usage : usages) {
            assertTrue(!withIncoming.contains(usage[0]) || !withIncoming.contains(usage[1]),
                    usage[0] + " ..> " + usage[1]);
        }
        for (String name : attributes.keySet()) {
            boolean isolated = !withIncoming.contains(name)
                    && inheritances.stream().noneMatch(i -> i[1].equals(name))
                    && realizations.stream().noneMatch(r -> r[1].equals(name));
            if (isolated) {
                assertTrue(usages.stream().anyMatch(u -> u[0].equals(name) || u[1].equals(name)), name);
            }
        }
    }
}
