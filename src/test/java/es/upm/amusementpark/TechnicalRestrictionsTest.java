package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Guards the Delivery 1 technical restrictions: Java 21, standard library only, no regex, no encryption.
 */
class TechnicalRestrictionsTest {

    private static final List<String> FORBIDDEN = List.of(
            "java.util.regex", "javax.crypto", "java.security", "Pattern.", ".matches(", ".replaceAll(",
            ".replaceFirst(");

    @Test
    void targetsJava21() throws IOException {
        assertTrue(Files.readString(Path.of("pom.xml")).contains("<maven.compiler.release>21</maven.compiler.release>"));
    }

    @Test
    void allDependenciesAreTestScoped() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        int managed = pom.indexOf("</dependencyManagement>");
        String deps = pom.substring(managed < 0 ? 0 : managed);
        int from = deps.indexOf("<dependency>");
        while (from >= 0) {
            int to = deps.indexOf("</dependency>", from);
            String dependency = deps.substring(from, to);
            if (!dependency.contains("<scope>test</scope>")) {
                fail("Non-test dependency declared: " + dependency);
            }
            from = deps.indexOf("<dependency>", to);
        }
    }

    @Test
    void productionCodeAvoidsRegexAndEncryption() throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src", "main", "java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String code = Files.readString(file);
                for (String api : FORBIDDEN) {
                    if (code.contains(api)) {
                        fail(file + " uses forbidden API " + api);
                    }
                }
            }
        }
    }
}
