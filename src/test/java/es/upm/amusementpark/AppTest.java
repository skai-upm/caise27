package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppTest {

    private static final List<String> COMMANDS = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER DELETE;pedro@correo.com;Wrong1!",
            "BOGUS",
            "HELP",
            "EXIT");

    @TempDir
    Path dir;

    private String runApp(String[] args, InputStream in, Path usersFile) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, in, new PrintStream(buffer, true, StandardCharsets.UTF_8), usersFile);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static InputStream noInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Test
    public void interactiveAndFileModesGiveSameResults() throws IOException {
        Path interactiveUsers = dir.resolve("interactive.txt");
        Path fileUsers = dir.resolve("file.txt");
        Path commandFile = dir.resolve("input_basico.txt");
        Files.write(commandFile, COMMANDS);

        String interactiveOut = runApp(new String[0],
                new ByteArrayInputStream(String.join("\n", COMMANDS).getBytes(StandardCharsets.UTF_8)),
                interactiveUsers);
        String fileOut = runApp(new String[] {commandFile.toString()}, noInput(), fileUsers);

        assertEquals(interactiveOut, fileOut);
        assertEquals(Files.readAllLines(interactiveUsers), Files.readAllLines(fileUsers));
        assertTrue(Files.readString(fileUsers).contains("699999999"));
    }

    @Test
    public void missingCommandFileIsReportedWithoutException() {
        String out = runApp(new String[] {dir.resolve("nope.txt").toString()}, noInput(), dir.resolve("u.txt"));
        assertTrue(out.startsWith("Error: command file not found or not readable"));
    }

    @Test
    public void directoryAsCommandFileIsReported() {
        String out = runApp(new String[] {dir.toString()}, noInput(), dir.resolve("u.txt"));
        assertTrue(out.startsWith("Error:"));
    }

    @Test
    public void usersAreRestoredOnNextExecution() throws IOException {
        Path users = dir.resolve("users.txt");
        Path first = dir.resolve("first.txt");
        Files.write(first, List.of(COMMANDS.get(0), "EXIT"));
        runApp(new String[] {first.toString()}, noInput(), users);

        String out = runApp(new String[0], new ByteArrayInputStream(
                (COMMANDS.get(0) + "\nEXIT\n").getBytes(StandardCharsets.UTF_8)), users);
        assertTrue(out.contains("already exists"));
    }
}
