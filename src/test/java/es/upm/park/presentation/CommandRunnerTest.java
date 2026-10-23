package es.upm.park.presentation;

import es.upm.park.App;
import es.upm.park.business.controller.Park;
import es.upm.park.business.controller.ParkFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRunnerTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private CommandRunner runner(Path dataDir) {
        return new CommandRunner(App.createCommandProcessor(ParkFixture.initialize(dataDir)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void stopsAtExitAndSkipsBlankLines() throws Exception {
        CommandRunner runner = runner(dir);
        String input = String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "   ",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");

        boolean exited = runner.run(new BufferedReader(new StringReader(input)));

        assertTrue(exited);
        assertEquals(1, Park.getInstance().getUsers().size());
        String[] lines = output().split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("EXIT", lines[1]);
        assertEquals(2, lines.length);
    }

    @Test
    void endOfInputWithoutExit() throws Exception {
        boolean exited = runner(dir).run(new BufferedReader(new StringReader("FOO\nHELP\n")));
        assertFalse(exited);
        assertTrue(output().contains("ERROR: Unknown command"));
        assertTrue(output().contains("Available commands"));
    }
}
