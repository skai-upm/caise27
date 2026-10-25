package es.upm.etsisi.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.TestSupport;
import es.upm.etsisi.park.business.controller.ParkController;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private ParkController park;
    private ByteArrayOutputStream buffer;
    private CommandRunner runner;

    @BeforeEach
    void setUp() {
        park = TestSupport.park(dir);
        buffer = new ByteArrayOutputStream();
        runner = new CommandRunner(TestSupport.processor(park), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private List<String> runLines(boolean interactive, String... lines) throws IOException {
        runner.run(new BufferedReader(new StringReader(String.join("\n", lines))), interactive);
        return buffer.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void processesCommandsInOrderIgnoringBlankLines() throws IOException {
        List<String> output = runLines(false, CREATE_ANA, "", "   ",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertEquals(List.of("USER CREATED", "USER UPDATED"), output);
        assertEquals("699999999", park.users().findByEmail("ana@correo.com").orElseThrow().getPhone());
    }

    @Test
    void stopsAtExit() throws IOException {
        List<String> output = runLines(false, CREATE_ANA, "EXIT", CREATE_PEDRO);
        assertEquals(List.of("USER CREATED", "BYE"), output);
        assertFalse(park.users().findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void continuesAfterInvalidCommand() throws IOException {
        List<String> output = runLines(false, "NOT A COMMAND", CREATE_ANA);
        assertTrue(output.get(0).startsWith("ERROR"));
        assertEquals("USER CREATED", output.get(1));
    }

    @Test
    void endOfInputEndsRun() throws IOException {
        assertEquals(List.of("USER CREATED"), runLines(false, CREATE_ANA));
    }

    @Test
    void interactiveModePrintsPrompt() throws IOException {
        runLines(true, "EXIT");
        assertTrue(buffer.toString(StandardCharsets.UTF_8).startsWith("> "));
    }
}
