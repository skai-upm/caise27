package es.upm.etsisi.amusementpark;

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

class AppTest {

    static final List<String> SCRIPT = List.of(
            CommandProcessorTest.CREATE_ANA,
            CommandProcessorTest.CREATE_PEDRO,
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER CREATE;Eva;Ruiz;11111111H;eva@correo.com;Clave1!;600000000;1111222233334444");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path users) {
        return App.run(args, new BufferedReader(new StringReader(stdin)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8),
                users);
    }

    @Test
    void fileModeAndInteractiveModeBehaveTheSame() throws Exception {
        Path input = dir.resolve("input.txt");
        Files.write(input, SCRIPT);
        Path fileUsers = dir.resolve("file-users.txt");
        assertEquals(0, run(new String[] {input.toString()}, "", fileUsers));
        String fileOutput = out.toString(StandardCharsets.UTF_8);
        out.reset();

        Path interactiveUsers = dir.resolve("interactive-users.txt");
        assertEquals(0, run(new String[0], String.join("\n", SCRIPT) + "\n", interactiveUsers));
        String interactiveOutput = out.toString(StandardCharsets.UTF_8);

        assertEquals(fileOutput, interactiveOutput);
        assertTrue(fileOutput.startsWith("USER CREATED"));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(fileUsers));
        assertEquals(Files.readAllLines(fileUsers), Files.readAllLines(interactiveUsers));
    }

    @Test
    void missingCommandFileIsReportedWithoutException() {
        int status = run(new String[] {dir.resolve("missing.txt").toString()}, "", dir.resolve("users.txt"));
        assertEquals(1, status);
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
        assertEquals("", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void directoryAsCommandFileIsReportedWithoutException() {
        assertEquals(1, run(new String[] {dir.toString()}, "", dir.resolve("users.txt")));
        assertTrue(err.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
    }
}
