package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.AttractionType;
import es.upm.parque.presentation.CommandProcessor;
import es.upm.parque.presentation.help.CommandInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the Delivery 3 example script (examples/input_fase3.txt) through the whole application in
 * every execution mode.
 */
class Delivery3IntegrationTest {

    private static final Path SCRIPT = Path.of("examples", "input_fase3.txt");

    /** First word of each response: O = OK, E = ERROR, in script order. */
    private static final String EXPECTED = "OOOOOOEOOOOOOOOOOEOOOOEOEOOOOOEOOEEOEOEOEOOOOOEEO";

    @TempDir
    Path base;

    private String run(String[] args, InputStream in) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.execute(args, base, TestPark.CLOCK, in, new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String outcomes(String output) {
        return output.lines().filter(l -> l.startsWith("OK") || l.startsWith("ERROR"))
                .map(l -> l.startsWith("OK") ? "O" : "E").collect(Collectors.joining());
    }

    private static InputStream noInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Test
    void scriptGivesExpectedOutcomesAndSurvivesRestart() {
        String output = run(new String[] {SCRIPT.toString()}, noInput());
        assertEquals(EXPECTED, outcomes(output));
        assertFalse(output.contains("Nunca"));

        TestPark restarted = new TestPark(base.resolve("data"));
        assertEquals("11111111H", restarted.users.authenticate("luis@correo.com", "Nueva4%").getDni());
        assertTrue(restarted.passes.findPass("SP1").isConsumed());
        assertTrue(restarted.passes.findPass("XP1").isConsumed());
        assertFalse(restarted.passes.findPass("SP2").isConsumed());
        assertEquals(AttractionType.SPECIAL, restarted.attractions.findById("A04").getType());
        assertEquals(AttractionType.GROUP, restarted.attractions.findById("A02").getType());
    }

    @Test
    void interactiveModeMatchesFileMode() throws IOException {
        String fromFile = run(new String[] {SCRIPT.toString()}, noInput());
        Path second = base.resolve("second");
        Files.createDirectories(second);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.execute(new String[0], second, TestPark.CLOCK, Files.newInputStream(SCRIPT),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        assertEquals(fromFile, bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
    }

    @Test
    void flyModeGivesSameResponsesWithoutPermanentData() {
        String normal = run(new String[] {SCRIPT.toString()}, noInput());
        Path flyBase = base.resolve("fly");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.execute(new String[] {"--fly", SCRIPT.toString()}, flyBase, TestPark.CLOCK, noInput(),
                new PrintStream(bytes, true, StandardCharsets.UTF_8));
        assertEquals(normal, bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"));
        assertFalse(Files.exists(flyBase.resolve("data")));
        assertTrue(Files.notExists(flyBase.resolve("tmp")) || flyBase.resolve("tmp").toFile().list().length == 0);
    }

    @Test
    void everyRegisteredCommandIsInBothHelps() {
        String output = run(new String[] {SCRIPT.toString()}, noInput());
        String help = output.substring(output.indexOf("OK Available commands:"), output.indexOf("OK Available commands (extended):"));
        String extended = output.substring(output.indexOf("OK Available commands (extended):"));
        for (CommandInfo command : CommandProcessor.COMMANDS) {
            assertTrue(help.contains(command.syntax() + "\n"), command.syntax());
            assertTrue(extended.contains(command.syntax() + "\n    " + command.description()), command.syntax());
        }
    }
}
