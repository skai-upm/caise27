package es.upm.parque;

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

/**
 * Tests for the interactive and file execution modes.
 */
class AppTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private String run(String[] args, String stdin, Path users) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), users);
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private UserManager reload(Path users) {
        return new UserManager(new UserRepository(users));
    }

    @Test
    void interactiveModeStopsAtExit() {
        Path users = dir.resolve("users.txt");
        String out = run(new String[0], ANA + "\nHELP\nEXIT\n" + PEDRO + "\n", users);
        assertTrue(out.contains("OK USER CREATED"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("OK EXIT"));
        assertNull(reload(users).findByEmail("pedro@correo.com"));
    }

    @Test
    void interactiveModeEndsWithInput() {
        Path users = dir.resolve("users.txt");
        run(new String[0], ANA + "\n" + PEDRO + "\n", users);
        assertEquals(2, reload(users).getUsers().size());
    }

    @Test
    void fileModeRunsLinesInOrderIgnoringBlanksAndStopsAtExit() throws Exception {
        Path users = dir.resolve("users.txt");
        Path input = dir.resolve("input_basico.txt");
        Files.writeString(input, ANA + "\n\n   \nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\nEXIT\n"
                + PEDRO + "\n");
        String out = run(new String[] {input.toString()}, "", users);
        assertTrue(!out.contains("ERROR"), out);
        UserManager m = reload(users);
        assertEquals("699999999", m.findByEmail("ana@correo.com").getPhone());
        assertNull(m.findByEmail("pedro@correo.com"));
    }

    @Test
    void fileModeAndInteractiveModeShareProcessing() throws Exception {
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, ANA + "\nUSER DELETE;ana@correo.com;Mal1!\n");
        String fromFile = run(new String[] {input.toString()}, "", dir.resolve("a.txt"));
        String fromStdin = run(new String[0], ANA + "\nUSER DELETE;ana@correo.com;Mal1!\n", dir.resolve("b.txt"));
        assertEquals(fromStdin, fromFile);
        assertNotNull(reload(dir.resolve("a.txt")).findByEmail("ana@correo.com"));
    }

    @Test
    void missingFileIsReportedWithoutException() {
        String out = run(new String[] {dir.resolve("nope.txt").toString()}, "", dir.resolve("users.txt"));
        assertTrue(out.startsWith("ERROR") && out.contains("nope.txt"), out);
    }

    @Test
    void directoryInsteadOfFileIsReported() {
        String out = run(new String[] {dir.toString()}, "", dir.resolve("users.txt"));
        assertTrue(out.startsWith("ERROR"), out);
    }
}
