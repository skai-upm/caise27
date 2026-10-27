package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Checks the Delivery 1 technical restrictions on the production sources.
 */
class TechnicalRestrictionsTest {

    private static final Path MAIN = Path.of("src", "main", "java", "es", "upm", "amusementpark");

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(MAIN)) {
            return files.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void onlyStandardLibraryAndNoRegularExpressions() throws IOException {
        for (Path source : sources()) {
            for (String line : Files.readAllLines(source)) {
                String l = line.trim();
                if (l.startsWith("import ")) {
                    assertTrue(l.startsWith("import java.") || l.startsWith("import static java.")
                            || l.startsWith("import es.upm.amusementpark.") || l.startsWith("import org.mindrot.jbcrypt."),
                            source + ": " + l);
                }
                for (String forbidden : new String[] {"java.util.regex", ".matches(", "replaceAll(", "replaceFirst(",
                        "ObjectOutputStream", "ObjectInputStream", "Serializable"}) {
                    assertFalse(l.contains(forbidden), source + ": " + l);
                }
            }
        }
    }

    @Test
    void userMainAndPersistenceAreSeparated() throws IOException {
        String user = Files.readString(MAIN.resolve("business/model/User.java"));
        String main = Files.readString(MAIN.resolve("presentation/Main.java"));
        String store = Files.readString(MAIN.resolve("persistence/UserFileStore.java"));
        assertFalse(user.contains("import java.io") || user.contains("import java.nio"));
        assertFalse(main.contains("Files.write") || main.contains("Files.readAllLines") || main.contains("Files.delete"));
        assertTrue(store.contains("Files.write") && store.contains("Files.readAllLines"));
    }

    @Test
    void relationshipDirectionsAreRespected() {
        for (java.lang.reflect.Field f : es.upm.amusementpark.business.model.User.class.getDeclaredFields()) {
            String type = f.getGenericType().getTypeName();
            assertFalse(type.contains("Ticket") || type.contains("Group") || type.contains("Attraction"), f.getName());
        }
        for (java.lang.reflect.Field f : es.upm.amusementpark.business.model.Group.class.getDeclaredFields()) {
            String type = f.getGenericType().getTypeName();
            assertFalse(type.contains("Ticket") || type.contains("Attraction"), f.getName());
        }
        assertTrue(es.upm.amusementpark.business.controller.ParkController.getInstance()
                == es.upm.amusementpark.business.controller.ParkController.getInstance());
    }

    @Test
    void layersAreSeparated() throws IOException {
        for (Path source : sources()) {
            String rel = MAIN.relativize(source).toString().replace('\\', '/');
            assertTrue(rel.startsWith("presentation/") || rel.startsWith("business/") || rel.startsWith("persistence/"),
                    "outside the layers: " + rel);
            String code = Files.readString(source);
            if (rel.startsWith("presentation/")) {
                for (String line : code.split("\n")) {
                    if (line.startsWith("import es.upm.amusementpark.persistence")) {
                        assertTrue(line.equals("import es.upm.amusementpark.persistence.PersistenceException;"),
                                rel + ": " + line);
                    }
                }
            }
            if (rel.startsWith("business/model/")) {
                assertFalse(code.contains("import es.upm.amusementpark.persistence"), rel);
            }
        }
    }

    @Test
    void bcryptIsTheOnlyRuntimeDependency() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        String dependencies = pom.substring(pom.indexOf("</dependencyManagement>"), pom.indexOf("<build>"));
        String[] blocks = dependencies.split("<dependency>");
        int runtime = 0;
        for (int i = 1; i < blocks.length; i++) {
            if (!blocks[i].contains("<scope>test</scope>")) {
                runtime++;
                assertTrue(blocks[i].contains("<artifactId>jbcrypt</artifactId>"), blocks[i]);
            }
        }
        assertTrue(runtime == 1);
    }
}
