package es.upm.etsisi.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.TestSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

class AppTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private String previousDataDir;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;

    @BeforeEach
    void setUp() {
        previousDataDir = System.getProperty(App.DATA_DIR_PROPERTY);
        System.setProperty(App.DATA_DIR_PROPERTY, dir.toString());
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() {
        if (previousDataDir == null) {
            System.clearProperty(App.DATA_DIR_PROPERTY);
        } else {
            System.setProperty(App.DATA_DIR_PROPERTY, previousDataDir);
        }
    }

    private int run(InputStream in, String... args) {
        out.reset();
        err.reset();
        return App.run(args, in, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8), TestSupport.HASHER);
    }

    private int run(String... args) {
        return run(InputStream.nullInputStream(), args);
    }

    private List<String> outputLines() {
        return out.toString(StandardCharsets.UTF_8).lines().toList();
    }

    private Path commandFile(String name, String... lines) throws IOException {
        Path file = dir.resolve(name);
        Files.write(file, List.of(lines));
        return file;
    }

    @Test
    void runsCommandFileAndPersistsAcrossRuns() throws IOException {
        Path input = commandFile("input.txt", CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#", "HELP", "EXIT");
        assertEquals(0, run(input.toString()));
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED", "USER UPDATED", "USER DELETED"),
                outputLines().subList(0, 5));
        List<String> users = Files.readAllLines(dir.resolve("users.txt"));
        assertEquals(1, users.size());
        assertTrue(users.get(0).startsWith("Ana;Garcia Lopez;12345678Z;ana.nueva@correo.com;$2a$"));
        assertTrue(users.get(0).endsWith(";699999999;1234567890123456"));
        assertFalse(users.get(0).contains("Clave1!"));

        Path second = commandFile("second.txt", "USER DELETE;ana.nueva@correo.com;Clave1!");
        assertEquals(0, run(second.toString()));
        assertEquals(List.of("USER DELETED"), outputLines());
    }

    @Test
    void reportsMissingCommandFile() {
        assertEquals(1, run(dir.resolve("missing.txt").toString()));
        String message = err.toString(StandardCharsets.UTF_8);
        assertTrue(message.startsWith("ERROR"));
        assertTrue(message.contains("missing.txt"));
    }

    @Test
    void interactiveModeReadsStandardInput() {
        InputStream in = new ByteArrayInputStream((CREATE_ANA + "\nEXIT\n").getBytes(StandardCharsets.UTF_8));
        assertEquals(0, run(in));
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("USER CREATED"));
        assertTrue(output.contains("BYE"));
    }

    @Test
    void flyModeWithFileLeavesPermanentDataUntouched() throws IOException {
        Path input = commandFile("permanent.txt", CREATE_ANA);
        run(input.toString());
        List<String> permanentUsers = Files.readAllLines(dir.resolve("users.txt"));

        Path flyInput = commandFile("fly.txt",
                "USER DELETE;ana@correo.com;Clave1!",
                CREATE_PEDRO,
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "EXIT");
        assertEquals(0, run("--fly", flyInput.toString()));
        List<String> lines = outputLines();
        assertTrue(lines.get(0).startsWith("ERROR"), "fly mode must start from an empty park");
        assertEquals("USER CREATED", lines.get(1));
        assertEquals("ATTRACTION CREATED", lines.get(2));

        assertEquals(permanentUsers, Files.readAllLines(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
        assertFalse(Files.exists(dir.resolve(TemporaryDirectory.TMP_DIR)));
    }

    @Test
    void flyModeInteractiveCleansUpAtEndOfInput() {
        InputStream in = new ByteArrayInputStream((CREATE_ANA + "\n").getBytes(StandardCharsets.UTF_8));
        assertEquals(0, run(in, "--fly"));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("USER CREATED"));
        assertFalse(Files.exists(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve(TemporaryDirectory.TMP_DIR)));
    }

    @Test
    void flyModeKeepsExistingTmpParentButRemovesItsOwnDirectory() throws IOException {
        Path tmp = Files.createDirectories(dir.resolve(TemporaryDirectory.TMP_DIR));
        Path other = Files.createDirectory(tmp.resolve("other"));
        Path flyInput = commandFile("fly.txt", CREATE_ANA, "EXIT");
        assertEquals(0, run("--fly", flyInput.toString()));
        try (var entries = Files.list(tmp)) {
            assertEquals(List.of(other), entries.toList());
        }
    }

    @Test
    void temporaryDirectoryIsTimestamped() throws IOException {
        TemporaryDirectory temporary = TemporaryDirectory.create(dir);
        String name = temporary.path().getFileName().toString();
        assertEquals(dir.resolve(TemporaryDirectory.TMP_DIR), temporary.path().getParent());
        assertEquals(15, name.length());
        assertEquals('_', name.charAt(8));
        TemporaryDirectory second = TemporaryDirectory.create(dir);
        assertFalse(temporary.path().equals(second.path()));
        second.delete(new PrintStream(err));
        temporary.delete(new PrintStream(err));
        assertFalse(Files.exists(dir.resolve(TemporaryDirectory.TMP_DIR)));
    }
}
