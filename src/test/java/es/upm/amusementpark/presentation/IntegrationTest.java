package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end runs of the Delivery 1 reference example (assignment §16).
 */
class IntegrationTest {

    private static final String EXAMPLE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER DELETE;ana.nueva@correo.com;Clave1!") + "\n";

    @TempDir
    Path dir;

    private String run(Path dataDir, String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = Main.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()),
                dataDir);
        assertEquals(0, code);
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void referenceExampleFromFileAndInteractiveGiveSameResult() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, EXAMPLE);
        Path fileRun = Files.createDirectory(dir.resolve("file-run"));
        Path stdinRun = Files.createDirectory(dir.resolve("stdin-run"));

        String fromFile = run(fileRun, "", input.toString());
        String interactive = run(stdinRun, EXAMPLE);

        assertTrue(fromFile.startsWith("USER CREATED\nUSER CREATED\nUSER UPDATED\nUSER UPDATED\nUSER DELETED\n"
                + "Available commands:"), fromFile);
        assertEquals(fromFile, interactive);
        List<String> expected = List.of(
                "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456");
        assertEquals(expected, Files.readAllLines(fileRun.resolve("users.txt")));
        assertEquals(expected, Files.readAllLines(stdinRun.resolve("users.txt")));
    }

    @Test
    void unprocessableInputIsHandled() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, String.join("\n",
                "DROP TABLE users",
                "USER CREATE;Ana;Garcia Lopez",
                "USER DELETE;nadie@correo.com;Clave1!",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        String[] lines = run(dir, "", input.toString()).split("\n");
        assertEquals(4, lines.length);
        for (int i = 0; i < 3; i++) {
            assertTrue(lines[i].startsWith("ERROR"), lines[i]);
        }
        assertEquals("USER CREATED", lines[3]);
    }
}
