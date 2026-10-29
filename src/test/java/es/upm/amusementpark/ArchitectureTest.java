package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Enforces the layered organization by inspecting package and import declarations. */
class ArchitectureTest {

    private static final String ROOT = "es.upm.amusementpark";
    private static final Path SOURCES = Path.of("src", "main", "java");

    private record Source(Path path, String pkg, List<String> imports) {
    }

    private static List<Source> sources() throws IOException {
        List<Source> result = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String pkg = null;
                List<String> imports = new ArrayList<>();
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("package ")) {
                        pkg = trimmed.substring(8, trimmed.length() - 1);
                    } else if (trimmed.startsWith("import ")) {
                        imports.add(trimmed.substring(7, trimmed.length() - 1).replace("static ", ""));
                    }
                }
                result.add(new Source(file, pkg, imports));
            }
        }
        return result;
    }

    private static boolean inLayer(String name, String layer) {
        return name.startsWith(ROOT + "." + layer);
    }

    @Test
    void everyClassIsInANamedPackageUnderTheRoot() throws IOException {
        for (Source source : sources()) {
            assertTrue(source.pkg() != null && source.pkg().startsWith(ROOT), "bad package: " + source.path());
        }
    }

    @Test
    void layerPackagesExist() throws IOException {
        List<String> packages = sources().stream().map(Source::pkg).toList();
        assertTrue(packages.stream().anyMatch(p -> inLayer(p, "presentation")));
        assertTrue(packages.stream().anyMatch(p -> inLayer(p, "business")));
        assertTrue(packages.stream().anyMatch(p -> inLayer(p, "persistence")));
    }

    @Test
    void presentationDoesNotUsePersistence() throws IOException {
        for (Source source : sources()) {
            if (inLayer(source.pkg(), "presentation")) {
                for (String imported : source.imports()) {
                    assertFalse(inLayer(imported, "persistence"), source.path() + " imports " + imported);
                }
            }
        }
    }

    @Test
    void businessDependsOnNoOtherLayerAndDoesNoFileIo() throws IOException {
        for (Source source : sources()) {
            if (inLayer(source.pkg(), "business")) {
                for (String imported : source.imports()) {
                    assertFalse(inLayer(imported, "presentation") || inLayer(imported, "persistence"),
                            source.path() + " imports " + imported);
                    assertFalse(imported.startsWith("java.nio.file") || imported.startsWith("java.io.File"),
                            source.path() + " performs file I/O: " + imported);
                }
            }
        }
    }

    @Test
    void persistenceDoesNotUsePresentation() throws IOException {
        for (Source source : sources()) {
            if (inLayer(source.pkg(), "persistence")) {
                for (String imported : source.imports()) {
                    assertFalse(inLayer(imported, "presentation"), source.path() + " imports " + imported);
                }
            }
        }
    }
}
