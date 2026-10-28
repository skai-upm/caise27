package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class EntryPointTest {

    private static String element(String xml, String name) {
        int start = xml.indexOf("<" + name + ">");
        int end = xml.indexOf("</" + name + ">");
        assertTrue(start >= 0 && end > start, "missing <" + name + "> in pom.xml");
        return xml.substring(start + name.length() + 2, end).trim();
    }

    @Test
    void jarNameAndMainClassConfigured() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        assertEquals("amusement-park", element(pom, "finalName"));
        String mainClass = element(pom, "mainClass");
        assertEquals(App.class.getName(), mainClass);
        Method main = Class.forName(mainClass).getMethod("main", String[].class);
        assertTrue(Modifier.isPublic(main.getModifiers()) && Modifier.isStatic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }

    @Test
    void onlyJbcryptIsARuntimeDependency() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        String dependencies = pom.substring(pom.indexOf("</dependencyManagement>"), pom.indexOf("<build>"));
        int start = dependencies.indexOf("<dependency>");
        while (start >= 0) {
            String block = dependencies.substring(start, dependencies.indexOf("</dependency>", start));
            assertTrue(block.contains("<scope>test</scope>") || block.contains("<artifactId>jbcrypt</artifactId>"),
                    block);
            start = dependencies.indexOf("<dependency>", start + 1);
        }
        assertTrue(dependencies.contains("<groupId>org.mindrot</groupId>"));
    }
}
