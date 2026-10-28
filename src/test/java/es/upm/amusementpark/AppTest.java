package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBytes, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBytes, true, StandardCharsets.UTF_8);

    private static BufferedReader reader(String text) {
        return new BufferedReader(new StringReader(text));
    }

    @Test
    void interactiveModeReadsStandardInputUntilEnd() {
        int status = App.execute(new String[0], reader("first\nsecond\n"), out, err);
        assertEquals(0, status);
        assertEquals("", errBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void fileModeProcessesLinesOfTheGivenFile(@TempDir Path dir) throws Exception {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, "a\nb\n");
        int status = App.execute(new String[] {input.toString()}, reader("ignored"), out, err);
        assertEquals(0, status);
        assertEquals("", errBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    void runProcessesEveryLineInOrder() throws Exception {
        List<String> seen = new ArrayList<>();
        App app = new App(out) {
            @Override
            void processLine(String line) {
                seen.add(line);
            }
        };
        app.run(reader("one\ntwo\nthree"));
        assertEquals(List.of("one", "two", "three"), seen);
    }

    @Test
    void missingFileIsReportedWithNonZeroStatus(@TempDir Path dir) {
        String missing = dir.resolve("missing.txt").toString();
        int status = App.execute(new String[] {missing}, reader(""), out, err);
        assertEquals(1, status);
        assertTrue(errBytes.toString(StandardCharsets.UTF_8).contains(missing));
    }
}
