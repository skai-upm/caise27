package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class InteractiveSessionTest {

    private static String run(String input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(new UserService(new UserRepository()));
        new InteractiveSession(new BufferedReader(new StringReader(input)),
                new PrintStream(out, true, StandardCharsets.UTF_8), processor).run();
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void emptyInputTerminatesImmediately() {
        assertEquals("", run(""));
    }

    @Test
    void processesEachLineAndContinuesAfterErrors() {
        String out = run("FOO\nUSER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n");
        String[] lines = out.split("\\R");
        assertEquals(2, lines.length);
        assertTrue(lines[0].startsWith("ERROR"));
        assertEquals("USER CREATED", lines[1]);
    }
}
