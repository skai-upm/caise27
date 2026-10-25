package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path dir;

    @Test
    void printsOneResponsePerNonBlankLine() throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserRepository(dir.resolve("u.txt"))));
        String input = CommandProcessorTest.CREATE_ANA + "\n\n   \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nUSER FLY\n";
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(new BufferedReader(new StringReader(input)), new PrintStream(bytes, true, StandardCharsets.UTF_8),
                processor);
        assertEquals(String.join(System.lineSeparator(), "OK USER CREATED", "OK USER UPDATED",
                "ERROR UNKNOWN COMMAND", ""), bytes.toString(StandardCharsets.UTF_8));
    }
}
