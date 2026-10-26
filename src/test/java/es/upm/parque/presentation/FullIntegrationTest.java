package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.presentation.help.CommandCatalog;
import es.upm.parque.presentation.help.CommandDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the reference Delivery 3 scenario (examples/input_fase3.txt) through every execution mode.
 */
class FullIntegrationTest {

    private static final Path SCRIPT = Path.of("examples", "input_fase3.txt");
    private static final List<String> EXPECTED_ERRORS = List.of(
            "ERROR: a user with DNI 12345678Z already exists",
            "ERROR: no user with email nadie@correo.com",
            "ERROR: group G01 cannot be modified: the group has current or future group tickets",
            "ERROR: ticket T001 is for 2026-10-15 and is not valid on 2026-10-01",
            "ERROR: INDIVIDUAL attraction A03 does not accept ticket TG01",
            "ERROR: SPEED is not accepted at SPECIAL attraction A04",
            "ERROR: user ana@correo.com cannot be deleted: the user has current or future tickets");

    @TempDir
    Path dir;

    private String run(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), dataDir, dir.resolve("tmp"));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static List<String> errors(String output) {
        return output.lines().filter(l -> l.startsWith("ERROR")).toList();
    }

    private static long filesIn(Path p) throws IOException {
        if (Files.notExists(p)) {
            return 0;
        }
        try (Stream<Path> files = Files.list(p)) {
            return files.count();
        }
    }

    @Test
    void scriptGivesTheSameResultInEveryExecutionMode() throws IOException {
        String script = Files.readString(SCRIPT);
        String fromFile = run(new String[] {SCRIPT.toString()}, "", dir.resolve("file"));
        String fromStdin = run(new String[0], script, dir.resolve("stdin"));
        String fly = run(new String[] {"--fly", SCRIPT.toString()}, "", dir.resolve("fly"));
        String flyStdin = run(new String[] {"--fly"}, script, dir.resolve("fly"));

        assertEquals(EXPECTED_ERRORS, errors(fromFile));
        assertEquals(fromFile, fromStdin);
        assertEquals(fromFile, fly);
        assertEquals(fromFile, flyStdin);
        assertTrue(fromFile.contains("OK GROUP TICKET CREATED -> 108.00"));
        assertTrue(fromFile.contains("[SpeedPass consumed: TG01-1-SP1, TG01-2-SP1, TG01-3-SP1]"));
        assertTrue(fromFile.contains("[Special Pass consumed: T002-SPECIAL]"));
        assertTrue(fromFile.contains("TOTAL: 120.00") && fromFile.contains("TOTAL: 108.00"));
        assertTrue(fromFile.trim().endsWith("OK EXIT"));
        assertFalse(fromFile.contains("Nunca"));
        assertEquals(0, filesIn(dir.resolve("fly")));
        assertEquals(0, filesIn(dir.resolve("tmp")));
    }

    @Test
    void normalModeKeepsDeliveryThreeStateAcrossRestarts() {
        Path data = dir.resolve("data");
        run(new String[] {SCRIPT.toString()}, "", data);
        String after = run(new String[0], "SYSTEM DATE;2026-10-16\nINVOICE CREATE;USER;ana@correo.com\n"
                + "INVOICE CREATE;USER;eva@correo.com\nATTRACTION STATUS;A04\nATTRACTION STATUS;A02\n"
                + "USER DELETE;ana@correo.com;Clave1!\nUSER DELETE;eva@correo.com;Nueva5&\nEXIT\n", data);
        assertTrue(after.contains("TOTAL: 120.00"), after);
        assertTrue(after.contains("T002-SPECIAL") && !after.contains("T003"), after);
        assertTrue(after.contains("Type: SPECIAL") && after.contains("Type: GROUP"), after);
        assertTrue(after.contains("Present: 0"), after);
        assertEquals(List.of(), errors(after));
    }

    @Test
    void everyHandledCommandFamilyIsInBothHelpModes() throws IOException {
        String processor = Files.readString(Path.of("src/main/java/es/upm/parque/presentation/CommandProcessor.java"));
        Matcher families = Pattern.compile("operation\\[0\\]\\.equals\\(\"(\\w+)\"\\)").matcher(processor);
        String out = run(new String[0], "HELP\nHELP EXTENDED\n", dir.resolve("help"));
        String help = out.substring(0, out.indexOf("Available commands (extended):"));
        String extended = out.substring(out.indexOf("Available commands (extended):"));
        int count = 0;
        while (families.find()) {
            count++;
            String family = families.group(1);
            assertTrue(help.contains("  " + family), family + " missing from HELP");
            assertTrue(extended.contains("  " + family), family + " missing from HELP EXTENDED");
        }
        assertTrue(count >= 10, "expected every command family to be found, got " + count);
    }

    @Test
    void everyCatalogSyntaxIsRecognisedByTheProcessor() {
        CommandProcessor processor = App.initialize(dir.resolve("catalog"));
        for (CommandDescriptor command : CommandCatalog.parkCommands().getCommands()) {
            String operation = command.getSyntax().split(";")[0];
            String response = processor.process(operation + ";x;y;z;w;v;u;t;s");
            assertFalse(response.contains("unknown command"), operation + " -> " + response);
        }
    }
}
