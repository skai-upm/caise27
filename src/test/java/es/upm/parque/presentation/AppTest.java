package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.controller.UserController;
import es.upm.parque.persistence.FileUserRepository;

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

    private String run(String[] args, String stdin, Path dataDir) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), dataDir, dir.resolve("tmp"));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private UserController reload(Path dataDir) {
        return UserController.getInstance().configure(new FileUserRepository(dataDir.resolve("users.txt")));
    }

    @Test
    void interactiveModeStopsAtExit() {
        Path users = dir.resolve("data");
        String out = run(new String[0], ANA + "\nHELP\nEXIT\n" + PEDRO + "\n", users);
        assertTrue(out.contains("OK USER CREATED"));
        assertTrue(out.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(out.contains("OK EXIT"));
        assertNull(reload(users).findByEmail("pedro@correo.com"));
    }

    @Test
    void interactiveModeEndsWithInput() {
        Path users = dir.resolve("data");
        run(new String[0], ANA + "\n" + PEDRO + "\n", users);
        assertEquals(2, reload(users).getUsers().size());
    }

    @Test
    void fileModeRunsLinesInOrderIgnoringBlanksAndStopsAtExit() throws Exception {
        Path users = dir.resolve("data");
        Path input = dir.resolve("input_basico.txt");
        Files.writeString(input, ANA + "\n\n   \nUSER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\nHELP\nEXIT\n"
                + PEDRO + "\n");
        String out = run(new String[] {input.toString()}, "", users);
        assertTrue(!out.contains("ERROR"), out);
        UserController m = reload(users);
        assertEquals("699999999", m.findByEmail("ana@correo.com").getPhone());
        assertNull(m.findByEmail("pedro@correo.com"));
    }

    @Test
    void fileModeAndInteractiveModeShareProcessing() throws Exception {
        Path input = dir.resolve("cmds.txt");
        Files.writeString(input, ANA + "\nUSER DELETE;ana@correo.com;Mal1!\n");
        String fromFile = run(new String[] {input.toString()}, "", dir.resolve("a"));
        String fromStdin = run(new String[0], ANA + "\nUSER DELETE;ana@correo.com;Mal1!\n", dir.resolve("b"));
        assertEquals(fromStdin, fromFile);
        assertNotNull(reload(dir.resolve("a")).findByEmail("ana@correo.com"));
    }

    @Test
    void missingFileIsReportedWithoutException() {
        String out = run(new String[] {dir.resolve("nope.txt").toString()}, "", dir.resolve("data"));
        assertTrue(out.startsWith("ERROR") && out.contains("nope.txt"), out);
    }

    @Test
    void directoryInsteadOfFileIsReported() {
        String out = run(new String[] {dir.toString()}, "", dir.resolve("data"));
        assertTrue(out.startsWith("ERROR"), out);
    }

    @Test
    void flyModeUsesATemporaryDirectoryAndDeletesIt() throws Exception {
        Path data = dir.resolve("data");
        run(new String[0], ANA + "\n", data);
        String before = Files.readString(data.resolve("users.txt"));
        String out = run(new String[] {"--fly"}, PEDRO + "\nGROUP CREATE;G01;Amigos\nGROUP ADD;G01;pedro@correo.com\n"
                + "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n",
                data);
        assertTrue(!out.contains("ERROR"), out);
        assertEquals(before, Files.readString(data.resolve("users.txt")));
        assertTrue(Files.notExists(data.resolve("groups.txt")));
        try (var left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }

    @Test
    void flyModeWithCommandFile() throws Exception {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, ANA + "\nGROUP CREATE;G01;Amigos\nGROUP ADD;G01;ana@correo.com\n");
        Path data = dir.resolve("data");
        String out = run(new String[] {"--fly", input.toString()}, "", data);
        assertEquals(3, out.lines().filter(l -> l.startsWith("OK")).count(), out);
        assertTrue(Files.notExists(data.resolve("users.txt")));
        try (var left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }

    @Test
    void unknownOptionIsRejected() {
        String out = run(new String[] {"--fast"}, ANA + "\n", dir.resolve("data"));
        assertTrue(out.startsWith("ERROR") && out.contains("--fast"), out);
        assertTrue(Files.notExists(dir.resolve("data").resolve("users.txt")));
    }
}
