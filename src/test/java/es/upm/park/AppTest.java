package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path dir;

    private String[] runSession(Path file, String input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(file, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8).split("\\R");
    }

    @Test
    void sessionPrintsOneResultPerCommandAndPersists() throws IOException {
        Path file = dir.resolve("users.txt");
        String[] lines = runSession(file, CommandProcessorTest.CREATE_ANA + "\nnonsense\n"
                + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n"
                + "USER DELETE;pedro@correo.com;Otra2#\n");
        assertEquals(4, lines.length);
        assertTrue(lines[0].startsWith("OK"));
        assertTrue(lines[1].startsWith("ERROR"));
        assertTrue(lines[3].startsWith("OK"));
        assertEquals(1, Files.readAllLines(file).size());

        String[] second = runSession(file, "USER DELETE;ana@correo.com;Clave1!\n");
        assertTrue(second[0].startsWith("OK"), second[0]);
        assertEquals(0, Files.readAllLines(file).size());
    }
}
