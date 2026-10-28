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
}
