package com.parque;

import com.parque.business.controller.*;
import com.parque.business.model.*;
import com.parque.business.service.*;
import com.parque.persistence.*;
import com.parque.presentation.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks uml/model.puml against the production classes so the diagram keeps describing the real code.
 */
class UmlModelTest {
    private static final Pattern RELATION = Pattern.compile(
            "^(\\w+) \"([^\"]+)\" (x-->|o-->|\\*-->) \"([^\"]+)\" (\\w+) : (\\w+)$");
    private static final Pattern DECLARATION = Pattern.compile("^\\s*(abstract class|class|enum|interface) (\\w+)");

    static String model;
    static List<String> lines;
    static List<String> productionClasses;
    static java.util.Map<String, String> qualified = new java.util.HashMap<>();

    @BeforeAll
    static void load() throws Exception {
        model = Files.readString(Path.of("uml/model.puml"));
        lines = model.lines().map(String::strip).toList();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path p : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String rel = Path.of("src/main/java").relativize(p).toString().replace('\\', '/');
                qualified.put(p.getFileName().toString().replace(".java", ""),
                        rel.substring(0, rel.length() - 5).replace('/', '.'));
            }
            productionClasses = qualified.keySet().stream().sorted().toList();
        }
    }

    private static Class<?> type(String simpleName) throws ClassNotFoundException {
        return Class.forName(qualified.get(simpleName));
    }

    @Test
    void validEnvelopeAndEveryClassDeclaredOnce() {
        assertTrue(model.strip().startsWith("@startuml"));
        assertTrue(model.strip().endsWith("@enduml"));
        List<String> declared = new ArrayList<>();
        for (String l : lines) {
            Matcher m = DECLARATION.matcher(l);
            if (m.find()) {
                declared.add(m.group(2));
            }
        }
        assertEquals(productionClasses, declared.stream().sorted().toList(), "declared elements = production classes");
    }

    @Test
    void classesAreInTheirPackages() {
        String current = null;
        for (String l : lines) {
            Matcher p = Pattern.compile("^package ([\\w.]+) \\{$").matcher(l);
            if (p.matches()) {
                current = p.group(1);
                continue;
            }
            Matcher m = DECLARATION.matcher(l);
            if (m.find()) {
                assertEquals(qualified.get(m.group(2)), current + "." + m.group(2), "package of " + m.group(2));
            }
        }
    }

    @Test
    void kindsMatchTheCode() throws Exception {
        for (String l : lines) {
            Matcher m = DECLARATION.matcher(l);
            if (!m.find()) {
                continue;
            }
            Class<?> c = type(m.group(2));
            switch (m.group(1)) {
                case "enum" -> assertTrue(c.isEnum(), c.getName());
                case "interface" -> assertTrue(c.isInterface(), c.getName());
                case "abstract class" -> assertTrue(Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), c.getName());
                default -> assertFalse(Modifier.isAbstract(c.getModifiers()) || c.isEnum(), c.getName());
            }
        }
    }

    @Test
    void inheritanceMatchesTheCode() throws Exception {
        Set<String> drawn = new HashSet<>();
        for (String l : lines) {
            if (l.contains("<|--")) {
                String[] p = l.split("\\s*<\\|--\\s*");
                assertEquals(type(p[0]), type(p[1]).getSuperclass(), l);
                drawn.add(p[1]);
            }
            if (l.contains("<|..")) {
                String[] p = l.split("\\s*<\\|\\.\\.\\s*");
                assertTrue(List.of(type(p[1]).getInterfaces()).contains(type(p[0])), l);
                drawn.add(p[1] + " implements " + p[0]);
            }
        }
        for (String name : productionClasses) {
            Class<?> c = type(name);
            if (c.getSuperclass() != null && c.getSuperclass().getPackageName().startsWith("com.parque")) {
                assertTrue(drawn.contains(name), "missing inheritance for " + name);
            }
            for (Class<?> i : c.getInterfaces()) {
                if (i.getPackageName().startsWith("com.parque")) {
                    assertTrue(drawn.contains(name + " implements " + i.getSimpleName()),
                            "missing realization " + name + " -> " + i.getSimpleName());
                }
            }
        }
    }

    @Test
    void everyRelationshipIsAFieldOfTheHolder() throws Exception {
        int relations = 0;
        for (String l : lines) {
            Matcher m = RELATION.matcher(l);
            if (!m.matches()) {
                continue;
            }
            relations++;
            Field f = type(m.group(1)).getDeclaredField(m.group(6));
            Class<?> target = type(m.group(5));
            Type t = f.getGenericType();
            if (t instanceof ParameterizedType pt) {
                assertTrue(List.class.isAssignableFrom(f.getType()), l);
                assertEquals(target, pt.getActualTypeArguments()[0], l);
                assertTrue(m.group(4).endsWith("*"), "collection needs a many multiplicity: " + l);
            } else {
                assertEquals(target, f.getType(), l);
            }
        }
        assertTrue(relations > 0);
    }

    @Test
    void everyModelReferenceFieldIsDrawn() throws Exception {
        Set<String> drawn = new HashSet<>();
        for (String l : lines) {
            Matcher m = RELATION.matcher(l);
            if (m.matches()) {
                drawn.add(m.group(1) + "." + m.group(6));
            }
        }
        for (String name : productionClasses) {
            for (Field f : type(name).getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
                    continue;
                }
                Class<?> ft = f.getType();
                if (f.getGenericType() instanceof ParameterizedType pt && pt.getActualTypeArguments()[0] instanceof Class<?> a) {
                    ft = a;
                }
                if (ft.getPackageName().startsWith("com.parque") && !ft.isEnum()) {
                    assertTrue(drawn.contains(name + "." + f.getName()), "relationship not drawn: " + name + "." + f.getName());
                }
            }
        }
    }

    @Test
    void noMethodsNoTechnicalLabelsNoObjectAttributes() {
        boolean inBody = false;
        for (String l : lines) {
            if (DECLARATION.matcher(l).find() && l.endsWith("{")) {
                inBody = true;
                continue;
            }
            if (inBody && l.equals("}")) {
                inBody = false;
                continue;
            }
            if (inBody) {
                assertFalse(l.contains("("), "method in class body: " + l);
                assertFalse(l.contains("{readOnly}") || l.contains("{static}") || l.contains("{abstract}"), l);
                for (String c : productionClasses) {
                    if (l.matches(".*:\\s*(List<)?" + c + ">?\\s*$")) {
                        fail("object-valued attribute should be a relationship: " + l);
                    }
                }
            }
        }
    }

    @Test
    void usageOnlyForClassesWithoutIncomingRelationships() {
        Set<String> incoming = new HashSet<>();
        for (String l : lines) {
            Matcher m = RELATION.matcher(l);
            if (m.matches()) {
                incoming.add(m.group(5));
            }
            if (l.contains("<|--")) {
                incoming.add(l.split("\\s*<\\|--\\s*")[0]);
            }
            if (l.contains("<|..")) {
                incoming.add(l.split("\\s*<\\|\\.\\.\\s*")[0]);
            }
        }
        Set<String> usedTargets = new HashSet<>();
        for (String l : lines) {
            if (l.contains("..>")) {
                String target = l.split("\\s*\\.\\.>\\s*")[1].split(" ")[0];
                assertFalse(incoming.contains(target), "usage drawn for a class with incoming relationships: " + l);
                usedTargets.add(target);
            }
        }
        for (String c : productionClasses) {
            if (!incoming.contains(c) && !c.equals("App")) {
                assertTrue(usedTargets.contains(c), "usage missing for " + c);
            }
        }
    }

    @Test
    void keyDomainRelationships() {
        assertTrue(lines.contains("Group \"0..*\" o--> \"0..*\" User : members"));
        assertTrue(lines.contains("IndividualTicket \"0..*\" x--> \"0..1\" User : user"));
        assertTrue(lines.contains("GroupTicket \"0..1\" *--> \"1..*\" IndividualTicket : constituents"));
        assertTrue(lines.contains("Attraction \"0..*\" o--> \"0..*\" User : present"));
    }
}
