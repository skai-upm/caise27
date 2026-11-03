package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

class AppTest {

    @TempDir
    Path dir;

    private List<String> run(Path file, String... commands) {
        ByteArrayInputStream in = new ByteArrayInputStream(
                String.join("\n", commands).getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(in, new PrintStream(out, true, StandardCharsets.UTF_8), file);
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    @Test
    void processesTerminalCommandsAndPersists() throws IOException {
        Path file = dir.resolve("users.txt");
        List<String> out = run(file,
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "NOT A COMMAND",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        assertEquals(3, out.size());
        assertEquals("OK", out.get(0).substring(0, 2));
        assertEquals("ERROR", out.get(1).substring(0, 5));
        assertEquals(2, Files.readAllLines(file).size());
    }

    @Test
    void restartRecoversOnlyCurrentUsers() throws IOException {
        Path file = dir.resolve("users.txt");
        run(file,
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#");
        // second run: a new process over the same file
        List<String> out = run(file,
                "USER CREATE;Ana2;X;12345678Z;otra@correo.com;Clave1!;6;1234567890123456",
                "USER UPDATE;ana.nueva@correo.com;Clave1!;PHONE;699999999",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        assertEquals("ERROR", out.get(0).substring(0, 5)); // DNI still unique after restart
        assertEquals("OK", out.get(1).substring(0, 2));    // updated email authenticates after restart
        assertEquals("OK", out.get(2).substring(0, 2));    // deleted user is really gone
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;Clave1!;699999999;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                Files.readAllLines(file));
        List<User> reloaded = new UserService(new UserFileStorage(file)).getUsers();
        assertEquals(2, reloaded.size());
    }
}
