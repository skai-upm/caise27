package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path dir;

    private String run(Path file, String input) {
        App app = new App(new CommandProcessor(new UserManager(new UserRepository(file))));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        app.run(new BufferedReader(new StringReader(input)), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void processesEachNonBlankLineAndSurvivesRestart() {
        Path file = dir.resolve("users.txt");
        String out = run(file, String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999") + "\n");
        assertEquals("OK USER CREATED\nOK USER UPDATED\n", out.replace("\r\n", "\n"));

        String second = run(file, "USER DELETE;ana@correo.com;Clave1!\n");
        assertEquals("OK USER DELETED", second.trim());
    }

    @Test
    void readFailureIsReported() {
        App app = new App(new CommandProcessor(new UserManager(new UserRepository(dir.resolve("u.txt")))));
        Reader failing = new Reader() {
            @Override
            public int read(char[] buf, int off, int len) throws IOException {
                throw new IOException("boom");
            }

            @Override
            public void close() {
                // nothing to release
            }
        };
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        app.run(new BufferedReader(failing), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        assertTrue(bytes.toString(StandardCharsets.UTF_8).startsWith("ERROR Cannot read input"));
    }
}
