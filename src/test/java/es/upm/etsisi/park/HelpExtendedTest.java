package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HelpExtendedTest {

    @TempDir
    Path dir;

    @Test
    void extendedHelpExplainsEveryCommand() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        List<String> syntax = f.run("HELP").lines().toList();
        List<String> extended = f.run("HELP EXTENDED").lines().toList();
        assertEquals(syntax.size() * 2, extended.size());
        for (int i = 0; i < syntax.size(); i++) {
            assertEquals(syntax.get(i), extended.get(2 * i));
            String purpose = extended.get(2 * i + 1);
            assertTrue(purpose.startsWith("    ") && purpose.strip().length() > 10, purpose);
        }
        assertTrue(syntax.contains("HELP EXTENDED"));
        assertFalse(syntax.stream().anyMatch(l -> l.startsWith("    ")));
    }
}
