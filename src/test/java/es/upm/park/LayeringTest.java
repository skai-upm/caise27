package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Enforces the package structure and allowed dependency directions of the layers. */
class LayeringTest {

    static final Path SOURCES = Path.of("src", "main", "java");
    static final String PRESENTATION = "es.upm.park.presentation";
    static final String MODEL = "es.upm.park.business.model";
    static final String SERVICE = "es.upm.park.business.service";
    static final String PERSISTENCE = "es.upm.park.persistence";
    static final Set<String> LAYERS = Set.of(PRESENTATION, MODEL, SERVICE, PERSISTENCE);

    /** Source file name → (package, imports). */
    static final Map<String, Source> SOURCES_BY_CLASS = new TreeMap<>();

    record Source(Path path, String pkg, List<String> imports) {
    }

    @BeforeAll
    static void scan() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String pkg = "";
                List<String> imports = new ArrayList<>();
                for (String line : Files.readAllLines(p)) {
                    String t = line.trim();
                    if (t.startsWith("package ")) {
                        pkg = t.substring("package ".length(), t.length() - 1);
                    } else if (t.startsWith("import ")) {
                        imports.add(t.substring("import ".length(), t.length() - 1));
                    }
                }
                SOURCES_BY_CLASS.put(p.getFileName().toString(), new Source(p, pkg, imports));
            }
        }
    }

    private static void assertNoImport(String layer, String... forbiddenPrefixes) {
        for (Source s : SOURCES_BY_CLASS.values()) {
            if (s.pkg().equals(layer)) {
                for (String imp : s.imports()) {
                    for (String bad : forbiddenPrefixes) {
                        assertTrue(!imp.startsWith(bad), s.path() + " must not import " + imp);
                    }
                }
            }
        }
    }

    @Test
    void everyClassIsInALayerPackage() {
        assertTrue(SOURCES_BY_CLASS.size() > 0);
        for (Source s : SOURCES_BY_CLASS.values()) {
            assertTrue(LAYERS.contains(s.pkg()), s.path() + " is in package '" + s.pkg() + "'");
            Path expectedDir = SOURCES.resolve(s.pkg().replace('.', '/'));
            assertEquals(expectedDir, s.path().getParent(), "directory matches package");
        }
    }

    @Test
    void presentationDoesNotUsePersistence() {
        assertNoImport(PRESENTATION, PERSISTENCE);
    }

    @Test
    void domainModelIsIndependent() {
        assertNoImport(MODEL, SERVICE, PERSISTENCE, PRESENTATION, "java.nio.file", "java.io");
    }

    @Test
    void businessDoesNotUsePresentation() {
        assertNoImport(SERVICE, PRESENTATION);
    }

    @Test
    void persistenceDoesNotUseServicesOrPresentation() {
        assertNoImport(PERSISTENCE, SERVICE, PRESENTATION);
    }
}
