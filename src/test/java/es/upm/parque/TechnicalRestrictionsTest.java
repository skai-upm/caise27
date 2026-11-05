package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Guards the technical restrictions: Java standard library only (plus jBCrypt for passwords since Delivery 3),
 * no regular expressions.
 */
class TechnicalRestrictionsTest {

    private static final String[] FORBIDDEN = {"java.util.regex", ".matches(", "replaceAll(", "replaceFirst("};

    @Test
    void everyMavenDependencyIsTestOrImportScoped() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        int start = 0;
        while ((start = pom.indexOf("<dependency>", start)) >= 0) {
            int end = pom.indexOf("</dependency>", start);
            String dependency = pom.substring(start, end);
            boolean passwordLibrary = dependency.contains("<groupId>org.mindrot</groupId>")
                    && dependency.contains("<artifactId>jbcrypt</artifactId>");
            assertTrue(passwordLibrary || dependency.contains("<scope>test</scope>")
                    || dependency.contains("<scope>import</scope>"), "runtime dependency found: " + dependency);
            start = end;
        }
    }

    @Test
    void mainSourcesUseNoRegularExpressions() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("src", "main", "java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String code = Files.readString(file, StandardCharsets.UTF_8);
                for (String token : FORBIDDEN) {
                    if (code.contains(token)) {
                        offenders.add(file + " uses " + token);
                    }
                }
            }
        }
        assertTrue(offenders.isEmpty(), offenders.toString());
    }
}
