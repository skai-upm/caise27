package es.upm.amusementpark.command;

import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.InMemoryUserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CommandRunnerTest {

    private static String run(String input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        CommandRunner runner = new CommandRunner(
                new CommandProcessor(new UserService(new InMemoryUserRepository())), out);
        runner.run(new BufferedReader(new StringReader(input)));
        return bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void processesCommandsInOrderAndSkipsBlankLines() throws IOException {
        String output = run(CREATE_ANA + "\n\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n"
                + CREATE_PEDRO + "\n"
                + "USER DELETE;pedro@correo.com;Otra2#\n");
        assertEquals("USER CREATED\nUSER UPDATED\nUSER CREATED\nUSER DELETED\n", output);
    }

    @Test
    void continuesAfterErrors() throws IOException {
        String output = run("FOO\nUSER DELETE;x\n" + CREATE_ANA + "\n");
        String[] lines = output.split("\n");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("ERROR"));
        assertTrue(lines[1].startsWith("ERROR"));
        assertEquals("USER CREATED", lines[2]);
    }

    @Test
    void exitStopsProcessingLaterLines() throws IOException {
        String output = run(CREATE_ANA + "\nEXIT\n" + CREATE_PEDRO + "\nHELP\n");
        assertEquals("USER CREATED\nBye\n", output);
    }

    @Test
    void endOfInputStopsCleanly() throws IOException {
        assertEquals("", run(""));
    }
}
