package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Guards the executable-JAR configuration (F0-C02): declared entry point and interactive mode.
 */
class ExecutableJarTest {

    @TempDir
    Path base;

    @Test
    void manifestMainClassIsTheEntryPoint() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        int start = pom.indexOf("<mainClass>");
        int end = pom.indexOf("</mainClass>");
        assertTrue(start >= 0 && end > start, "pom.xml must declare the manifest mainClass");
        String mainClass = pom.substring(start + "<mainClass>".length(), end).trim();
        assertEquals(App.class.getName(), mainClass);
        Method main = Class.forName(mainClass).getMethod("main", String[].class);
        assertTrue(Modifier.isStatic(main.getModifiers()) && Modifier.isPublic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }

    @Test
    void finalNameProducesAmusementParkJar() throws IOException {
        assertTrue(Files.readString(Path.of("pom.xml")).contains("<finalName>amusement-park</finalName>"));
    }

    @Test
    void interactiveModeKeepsWaitingUntilExit() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[0], new BufferedReader(new StringReader("HELP\n\nHELP\nEXIT\nHELP\n")),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), base);
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(2, out.split("Available commands:", -1).length - 1);
        assertTrue(out.endsWith("Exiting application." + System.lineSeparator()));
    }
}
