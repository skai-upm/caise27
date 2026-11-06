package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private Path usersFile;

    @BeforeEach
    void useTemporaryUsersFile() {
        usersFile = dir.resolve("users.txt");
        System.setProperty(Main.USERS_FILE_PROPERTY, usersFile.toString());
    }

    @AfterEach
    void clearProperty() {
        System.clearProperty(Main.USERS_FILE_PROPERTY);
    }

    private String run(String input, String... args) {
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            Main.main(args);
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        return out.toString(StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
    }

    @Test
    void startsWithoutArguments() {
        assertEquals(Main.BANNER, run(""));
    }

    @Test
    void missingCommandFileIsReportedInAControlledWay() {
        Path missing = dir.resolve("missing.txt");
        assertEquals(Main.BANNER + "\nError: cannot read command file " + missing, run("", missing.toString()));
    }

    @Test
    void directoryAsCommandFileIsReported() {
        assertEquals(Main.BANNER + "\nError: cannot read command file " + dir, run("", dir.toString()));
    }

    @Test
    void commandFileIsProcessedInOrderWithoutReadingStandardInput() throws Exception {
        Path commands = dir.resolve("input.txt");
        Files.write(commands, List.of(ANA, "", "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        String out = run("USER DELETE;ana@correo.com;Clave1!\n", commands.toString());
        assertEquals(Main.BANNER + "\nUser created: 12345678Z\nUser updated: 12345678Z PHONE", out);
        assertEquals("699999999", Files.readAllLines(usersFile).get(0).split(";")[5]);
    }

    @Test
    void sameOutputAndEffectsFromFileAndStandardInput() throws Exception {
        String script = ANA + "\nFOO\nUSER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria\nUSER DELETE;x@y.com;Clave1!\n";
        String fromStdin = run(script);
        List<String> storedFromStdin = Files.readAllLines(usersFile);
        Files.delete(usersFile);
        Path commands = dir.resolve("same.txt");
        Files.writeString(commands, script);
        assertEquals(fromStdin, run("", commands.toString()));
        assertEquals(storedFromStdin, Files.readAllLines(usersFile));
    }

    @Test
    void processesStandardInputInOrder() {
        String input = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "\n"
                + "FOO BAR;1\n"
                + "USER CREATE;Luis;Perez;12345678Z;luis@correo.com;Clave1!;600000000;1111222233334444\n";
        String[] lines = run(input).split("\n");
        assertEquals(4, lines.length);
        assertEquals(Main.BANNER, lines[0]);
        assertEquals("User created: 12345678Z", lines[1]);
        assertEquals("Error: unknown command 'FOO BAR'", lines[2]);
        assertEquals("Error: a user with DNI 12345678Z already exists", lines[3]);
    }

    @Test
    void usersSurviveRestart() throws Exception {
        assertEquals(Main.BANNER + "\nUser created: 12345678Z", run(ANA + "\n"));
        assertEquals(List.of(ANA.substring("USER CREATE;".length())), Files.readAllLines(usersFile));
        assertEquals(Main.BANNER + "\nError: a user with DNI 12345678Z already exists", run(ANA + "\n"));
        assertEquals(1, Files.readAllLines(usersFile).size());
    }

    @Test
    void updatesSurviveRestart() throws Exception {
        run(ANA + "\nUSER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria\n");
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(usersFile));
        assertEquals(Main.BANNER + "\nUser updated: 12345678Z PHONE", run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699\n"));
        assertEquals("Ana Maria;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699;1234567890123456",
                Files.readAllLines(usersFile).get(0));
    }

    @Test
    void noCommandRunsAfterExitInFileMode() throws Exception {
        Path commands = dir.resolve("exit.txt");
        Files.write(commands, List.of(ANA, "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"));
        assertEquals(Main.BANNER + "\nUser created: 12345678Z\nBye", run("", commands.toString()));
        assertEquals(1, Files.readAllLines(usersFile).size());
    }

    @Test
    void noCommandRunsAfterExitInInteractiveMode() {
        assertEquals(Main.BANNER + "\nBye", run("EXIT\nHELP\n"));
    }

    @Test
    void helpIsIdenticalInBothModes() throws Exception {
        Path commands = dir.resolve("help.txt");
        Files.write(commands, List.of("HELP"));
        String fromStdin = run("HELP\n");
        assertTrue(fromStdin.contains("USER DELETE;<email>;<password>"));
        assertEquals(fromStdin, run("", commands.toString()));
    }
}
