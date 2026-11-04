package com.parque;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Architecture rules: production classes are organised in presentation, business and persistence packages and
 * only depend on the layers they are allowed to.
 */
class LayeringTest {
    static final String ROOT = "com.parque";
    static final List<String> PACKAGES = List.of(ROOT, ROOT + ".presentation", ROOT + ".business.model",
            ROOT + ".business.service", ROOT + ".business.controller", ROOT + ".persistence");

    /** Forbidden references (package prefixes or JDK packages) per layer. */
    static final Map<String, List<String>> FORBIDDEN = Map.of(
            ROOT + ".presentation", List.of(ROOT + ".persistence"),
            ROOT + ".business.model", List.of(ROOT + ".persistence", ROOT + ".presentation", ROOT + ".business.service",
                    ROOT + ".business.controller", "java.nio.file", "java.io"),
            ROOT + ".business.service", List.of(ROOT + ".presentation", ROOT + ".business.controller"),
            ROOT + ".business.controller", List.of(ROOT + ".presentation", ROOT + ".persistence"),
            ROOT + ".persistence", List.of(ROOT + ".presentation", ROOT + ".business.service",
                    ROOT + ".business.controller"));

    record Source(Path path, String pkg, String text) {
    }

    static List<Source> sources() throws Exception {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            return files.filter(p -> p.toString().endsWith(".java")).map(p -> {
                try {
                    String text = Files.readString(p);
                    String pkg = text.lines().filter(l -> l.startsWith("package ")).findFirst()
                            .map(l -> l.substring(8, l.length() - 1).trim()).orElse("");
                    return new Source(p, pkg, text);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).toList();
        }
    }

    @Test
    void everyClassIsInALayerPackageAndNoneInTheDefaultPackage() throws Exception {
        for (Source s : sources()) {
            assertFalse(s.pkg().isEmpty(), "default package: " + s.path());
            assertTrue(PACKAGES.contains(s.pkg()), "unexpected package " + s.pkg() + " for " + s.path());
            String expectedDir = s.pkg().replace('.', '/');
            assertTrue(s.path().getParent().toString().replace('\\', '/').endsWith(expectedDir), s.path().toString());
            if (s.pkg().equals(ROOT)) {
                assertTrue(List.of("App.java", "ApplicationContext.java").contains(s.path().getFileName().toString()),
                        "only the App bootstrap and the ApplicationContext composition root may live in " + ROOT
                                + ": " + s.path());
            }
        }
    }

    @Test
    void layersOnlyDependOnAllowedLayers() throws Exception {
        for (Source s : sources()) {
            List<String> forbidden = FORBIDDEN.getOrDefault(s.pkg(), List.of());
            for (String line : s.text().lines().toList()) {
                String l = line.strip();
                if (l.startsWith("//") || l.startsWith("*") || l.startsWith("/*")) {
                    continue;
                }
                for (String f : forbidden) {
                    if (l.startsWith("import " + f + ".") || l.contains(" " + f + ".")) {
                        fail(s.path() + " (" + s.pkg() + ") must not depend on " + f + ": " + l);
                    }
                }
            }
        }
    }

    @Test
    void mainControllerIsInTheBusinessLayer() throws Exception {
        Class<?> controller = Class.forName(ROOT + ".business.controller.ParkController");
        assertTrue(controller.getDeclaredField("currentDate") != null);
        assertTrue(controller.getDeclaredField("ticketPrice") != null);
    }
}
