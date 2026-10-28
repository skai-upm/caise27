package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs the Delivery 1 example session of the statement end to end.
 */
class DeliveryOneIntegrationTest {

    static final String SESSION = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private String run(String[] args, String stdin, Path users) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        int status = App.execute(args, new BufferedReader(new StringReader(stdin)), out, out, users.getParent());
        assertEquals(0, status);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void assertSession(String output, Path users) throws Exception {
        List<String> lines = List.of(output.split("\\R"));
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED"),
                lines.subList(0, 5));
        assertTrue(output.contains("USER DELETE;<Email>;<Password>"));
        assertEquals("Goodbye", lines.get(lines.size() - 1));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;<bcrypt>;699999999;1234567890123456"),
                StoredUsers.masked(Files.readAllLines(users)));
    }

    @Test
    void statementSessionFromFile() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.writeString(input, SESSION);
        Path users = dir.resolve("file").resolve("users.txt");
        assertSession(run(new String[] {input.toString()}, "", users), users);
    }

    @Test
    void statementSessionInteractive() throws Exception {
        Path users = dir.resolve("stdin").resolve("users.txt");
        assertSession(run(new String[0], SESSION, users), users);
    }

    @Test
    void malformedInputNeverStopsTheSession() throws Exception {
        Path users = dir.resolve("users.txt");
        String output = run(new String[0], String.join("\n", ";", ";;;;", "USER", "USER CREATE;;;;;;;",
                "USER UPDATE;a;b;c;d", "USER DELETE;;", "help", "EXIT"), users);
        List<String> lines = List.of(output.split("\\R"));
        assertEquals(8, lines.size());
        lines.subList(0, 7).forEach(l -> assertTrue(l.startsWith("ERROR:"), l));
        assertEquals("Goodbye", lines.get(7));
    }
}
