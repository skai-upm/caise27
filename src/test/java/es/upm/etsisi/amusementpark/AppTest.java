package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    private static final String SEQUENCE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Dup;Dup;11111111H;ana@correo.com;Clave1!;1;1234567890123456",
            "NOT A COMMAND",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
            "USER UPDATE;ana.nueva@correo.com;Clave1!;DNI;87654321X",
            "USER DELETE;pedro@correo.com;Otra2#",
            "HELP",
            "EXIT",
            "USER CREATE;Late;User;00000000T;late@correo.com;Clave1!;1;1234567890123456",
            "");

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String[] args, String stdin, Path users) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), users);
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void interactiveAndFileModesBehaveIdentically() throws Exception {
        Path usersA = dir.resolve("a.txt");
        assertEquals(0, run(new String[0], SEQUENCE, usersA));
        String interactive = output();
        out.reset();

        Path commands = dir.resolve("input.txt");
        Files.writeString(commands, SEQUENCE);
        Path usersB = dir.resolve("b.txt");
        assertEquals(0, run(new String[] {commands.toString()}, "", usersB));
        String fromFile = output();

        assertEquals(interactive, fromFile);
        assertEquals(Files.readAllLines(usersA), Files.readAllLines(usersB));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456"),
                Files.readAllLines(usersB));
        List<String> lines = fromFile.lines().toList();
        assertEquals("USER CREATED", lines.get(0));
        assertEquals("USER CREATED", lines.get(1));
        assertTrue(lines.get(2).startsWith("ERROR:"));
        assertTrue(lines.get(3).startsWith("ERROR:"));
        assertEquals("USER UPDATED", lines.get(4));
        assertEquals("USER UPDATED", lines.get(5));
        assertTrue(lines.get(6).startsWith("ERROR:"));
        assertEquals("USER DELETED", lines.get(7));
        assertTrue(fromFile.contains("USER DELETE;<email>;<password>"));
        assertEquals("BYE", lines.get(lines.size() - 1));
    }

    @Test
    void usersSurviveRestart() throws Exception {
        Path users = dir.resolve("users.txt");
        run(new String[0], "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n", users);
        out.reset();
        run(new String[0], "USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;1;1234567890123456\n"
                + "USER DELETE;ana@correo.com;Clave1!\n", users);
        List<String> lines = output().lines().toList();
        assertTrue(lines.get(0).startsWith("ERROR:"), "email still reserved after restart");
        assertEquals("USER DELETED", lines.get(1));
        assertTrue(Files.readAllLines(users).isEmpty());
    }

    @Test
    void missingCommandFileIsControlled() {
        Path users = dir.resolve("users.txt");
        assertEquals(1, run(new String[] {dir.resolve("nope.txt").toString()}, "", users));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("not found or not readable"));
        assertFalse(Files.exists(users));
    }

    @Test
    void directoryAsCommandFileIsControlled() {
        assertEquals(1, run(new String[] {dir.toString()}, "", dir.resolve("users.txt")));
    }

    @Test
    void unreadableUsersFileIsControlled() {
        // A directory where the users file should be cannot be read as a file.
        assertEquals(1, run(new String[0], "EXIT\n", dir));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("could not load users"));
    }
}
