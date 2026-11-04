package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Checks the layer boundaries by inspecting the production sources. */
class LayeringTest {

    private static final Path ROOT = Path.of("src", "main", "java", "es", "upm", "etsisi", "amusementpark");
    private static final String BASE = "es.upm.etsisi.amusementpark.";

    private static List<Path> sources(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    private static List<String> imports(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .map(String::trim).filter(l -> l.startsWith("import ")).toList();
    }

    private static List<String> violations(Path dir, List<String> forbiddenPrefixes) throws IOException {
        List<String> found = new ArrayList<>();
        for (Path f : sources(dir)) {
            for (String imp : imports(f)) {
                for (String prefix : forbiddenPrefixes) {
                    if (imp.startsWith("import " + prefix)) {
                        found.add(f.getFileName() + ": " + imp);
                    }
                }
            }
        }
        return found;
    }

    @Test
    void layerPackagesExistAndNothingUsesTheDefaultPackage() throws IOException {
        for (String layer : List.of("presentation", "business/model", "business/service", "business/controller", "persistence")) {
            assertTrue(Files.isDirectory(ROOT.resolve(layer)), layer);
        }
        for (Path f : sources(Path.of("src", "main", "java"))) {
            assertTrue(Files.readAllLines(f).stream().anyMatch(l -> l.startsWith("package " + BASE)
                    || l.equals("package es.upm.etsisi.amusementpark;")), "No package in " + f);
            Path rel = ROOT.relativize(f);
            assertTrue(rel.getNameCount() > 1 || rel.toString().equals("App.java"), "Outside a layer: " + rel);
        }
    }

    @Test
    void presentationDoesNotTouchPersistenceOrFiles() throws IOException {
        List<String> found = violations(ROOT.resolve("presentation"), List.of(BASE + "persistence", "java.nio.file"));
        assertTrue(found.isEmpty(), found.toString());
    }

    @Test
    void modelIsIndependentOfOtherLayersAndFiles() throws IOException {
        List<String> found = violations(ROOT.resolve("business/model"), List.of(BASE + "persistence", "java.nio.file",
                BASE + "business.service", BASE + "business.controller", BASE + "presentation"));
        assertTrue(found.isEmpty(), found.toString());
    }

    @Test
    void onlyTheEntryPointUsesThePresentationLayer() throws IOException {
        List<String> found = new ArrayList<>();
        for (String layer : List.of("business", "persistence")) {
            found.addAll(violations(ROOT.resolve(layer), List.of(BASE + "presentation")));
        }
        assertTrue(found.isEmpty(), found.toString());
    }

    @Test
    void persistenceDependsOnlyOnTheModel() throws IOException {
        List<String> found = violations(ROOT.resolve("persistence"), List.of(BASE + "business.service",
                BASE + "business.controller", BASE + "presentation"));
        assertTrue(found.isEmpty(), found.toString());
    }
}
