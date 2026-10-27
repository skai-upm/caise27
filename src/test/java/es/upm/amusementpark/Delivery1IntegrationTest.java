package es.upm.amusementpark;

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
 * End-to-end checks of the complete Delivery 1 feature set through the
 * application entry point.
 */
class Delivery1IntegrationTest {

    private static final String EXAMPLE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "");

    @TempDir
    Path tempDir;

    private static String run(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dataDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void exampleFlowWorksFromFileAndInteractively() throws IOException {
        Path input = tempDir.resolve("input.txt");
        Files.writeString(input, EXAMPLE, StandardCharsets.UTF_8);

        String fromFile = run(new String[] {input.toString()}, "", tempDir.resolve("file"));
        String interactive = run(new String[0], EXAMPLE, tempDir.resolve("interactive"));

        String[] lines = fromFile.split(System.lineSeparator());
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED"),
                List.of(lines).subList(0, 5));
        assertTrue(fromFile.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        assertEquals(interactive, fromFile);

        List<String> stored = Files.readAllLines(tempDir.resolve("file/users.txt"), StandardCharsets.UTF_8);
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                stored);
        assertEquals(stored, Files.readAllLines(tempDir.resolve("interactive/users.txt"), StandardCharsets.UTF_8));
    }

    @Test
    void invalidInputIsHandledWithoutStopping() {
        String commands = String.join("\n",
                "FOO",
                "USER CREATE",
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo;clave;612345678;1234",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER DELETE;ana@correo.com;wrong",
                "USER DELETE;;",
                "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
                "USER UPDATE;ana@correo.com;Clave1!;CARD;1234 5678 9012 3456",
                ";;;",
                "user create;x",
                "USER DELETE;ana@correo.com;Clave1!",
                "EXIT",
                "");
        String out = run(new String[0], commands, tempDir.resolve("data"));
        String[] lines = out.split(System.lineSeparator());
        assertEquals(13, lines.length);
        assertEquals("USER CREATED", lines[3]);
        assertEquals("USER DELETED", lines[11]);
        assertEquals("Exiting application.", lines[12]);
        for (int i : new int[] {0, 1, 2, 4, 5, 6, 7, 8, 9, 10}) {
            assertTrue(lines[i].startsWith("ERROR"), lines[i]);
        }
    }
}
