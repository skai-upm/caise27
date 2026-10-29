package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    private static final String SCRIPT = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave1!;600000000;1111222233334444",
            "");

    @TempDir
    Path dir;

    private String runApp(Path workDir, String[] args, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir.resolve("users.txt"));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fileAndInteractiveModesBehaveIdentically() throws Exception {
        Path fileDir = Files.createDirectory(dir.resolve("file"));
        Path input = fileDir.resolve("input.txt");
        Files.writeString(input, SCRIPT);
        String fileOutput = runApp(fileDir, new String[] {input.toString()}, "");

        Path stdinDir = Files.createDirectory(dir.resolve("stdin"));
        String stdinOutput = runApp(stdinDir, new String[0], SCRIPT);

        assertEquals(fileOutput, stdinOutput);
        assertEquals(Files.readAllLines(fileDir.resolve("users.txt")), Files.readAllLines(stdinDir.resolve("users.txt")));

        String[] lines = fileOutput.split(System.lineSeparator());
        assertEquals("USER CREATED", lines[0]);
        assertEquals("USER CREATED", lines[1]);
        assertEquals("USER UPDATED", lines[2]);
        assertEquals("USER UPDATED", lines[3]);
        assertEquals("USER DELETED", lines[4]);
        assertTrue(fileOutput.contains("USER DELETE;<email>;<password>"));
    }

    @Test
    void commandsAfterExitAreNotExecuted() {
        runApp(dir, new String[0], SCRIPT);
        UserService service = new UserService(new UserRepository(dir.resolve("users.txt")));
        assertNull(service.findByEmail("luis@correo.com"));
        assertNotNull(service.findByEmail("ana.nueva@correo.com"));
        assertEquals("699999999", service.findByEmail("ana.nueva@correo.com").getPhone());
    }

    @Test
    void missingCommandFileIsReported() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = App.run(new String[] {dir.resolve("nope.txt").toString()}, new ByteArrayInputStream(new byte[0]),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir.resolve("users.txt"));
        assertEquals(1, code);
        assertTrue(buffer.toString(StandardCharsets.UTF_8).startsWith("ERROR:"));
    }

    @Test
    void endOfInputWithoutExitTerminates() {
        String out = runApp(dir, new String[0], "HELP\n");
        assertTrue(out.contains("USER CREATE"));
    }
}
