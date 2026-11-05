package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import es.upm.parque.command.CommandProcessor;
import es.upm.parque.repository.UserRepository;

/**
 * Tests for the application entry point and its command loop.
 */
public class AppTest {

    @Test
    public void entryPointStartsWithoutArgumentsAndEmptyInput() {
        InputStream original = System.in;
        try (InputStream empty = new ByteArrayInputStream(new byte[0])) {
            System.setIn(empty);
            assertDoesNotThrow(() -> App.main(new String[0]));
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        } finally {
            System.setIn(original);
        }
    }

    @Test
    public void commandsAreProcessedInOrderAndBlankLinesIgnored() throws Exception {
        UserRepository repo = new UserRepository();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String input = String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "FOO;bar",
                "USER CREATE;Luis;Perez;87654321X;luis@correo.com;Clave1!;600000000;1234567890123457",
                "");
        App.run(new CommandProcessor(repo), new StringReader(input), new PrintStream(out, true, StandardCharsets.UTF_8));
        String[] lines = out.toString(StandardCharsets.UTF_8).trim().split("\\R");
        assertEquals(3, lines.length);
        assertTrue(lines[0].startsWith("OK"));
        assertTrue(lines[1].startsWith("ERROR"));
        assertTrue(lines[2].startsWith("OK"));
        assertEquals(2, repo.size());
    }
}
