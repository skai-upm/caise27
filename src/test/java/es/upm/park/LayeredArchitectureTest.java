package es.upm.park;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Enforces the layer structure and dependency directions of the production code. */
class LayeredArchitectureTest {

    private static final Path MAIN = Path.of("src", "main", "java");
    private static final String ROOT = "es.upm.park.";

    /** Layer package prefix and the layer packages it must not import. */
    private static final String[][] RULES = {
        {ROOT + "presentation", ROOT + "persistence"},
        {ROOT + "business", ROOT + "presentation", ROOT + "persistence"},
    };

    static List<String> violations(String packageName, String source, String fileName) {
        List<String> found = new ArrayList<>();
        for (String[] rule : RULES) {
            if (!packageName.startsWith(rule[0])) {
                continue;
            }
            for (int i = 1; i < rule.length; i++) {
                if (source.contains("import " + rule[i] + ".")) {
                    found.add(fileName + " imports " + rule[i]);
                }
            }
        }
        return found;
    }

    private static String packageOf(String source) {
        for (String line : source.split("\n")) {
            if (line.startsWith("package ")) {
                return line.substring("package ".length(), line.indexOf(';'));
            }
        }
        return "";
    }

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void everySourceDeclaresAPackageAndLayersExist() throws IOException {
        List<String> packages = new ArrayList<>();
        for (Path file : sources()) {
            String pkg = packageOf(Files.readString(file, StandardCharsets.UTF_8));
            assertTrue(!pkg.isEmpty(), file + " is in the default package");
            packages.add(pkg);
        }
        for (String layer : new String[] {"presentation", "business", "persistence"}) {
            assertTrue(packages.stream().anyMatch(pkg -> pkg.startsWith(ROOT + layer)), "missing layer " + layer);
        }
    }

    @Test
    void layersRespectDependencyDirections() throws IOException {
        List<String> found = new ArrayList<>();
        for (Path file : sources()) {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            found.addAll(violations(packageOf(source), source, file.getFileName().toString()));
        }
        assertEquals(List.of(), found);
    }

    @Test
    void detectsAForbiddenImport() {
        String source = "package es.upm.park.presentation;\nimport es.upm.park.persistence.TextParkStorage;\n";
        assertEquals(List.of("X.java imports es.upm.park.persistence"),
                violations("es.upm.park.presentation", source, "X.java"));
    }
}
