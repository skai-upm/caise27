package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppFlyTest {

    private static final String SCRIPT = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "GROUP CREATE;G01;Familia",
            "EXIT",
            "");

    @TempDir
    Path dir;

    private String run(String[] args, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void flyWithFileLeavesNoData() throws Exception {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, SCRIPT);
        String out = run(new String[] {"--fly", input.toString()}, "");
        assertTrue(out.startsWith("USER CREATED"), out);
        assertTrue(out.contains("ATTRACTION CREATED"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("attractions.txt")));
    }

    @Test
    void flyInteractiveLeavesNoData() {
        String out = run(new String[] {"--fly"}, SCRIPT);
        assertTrue(out.contains("GROUP CREATED"));
        assertFalse(Files.exists(dir.resolve("tmp")));
        assertFalse(Files.exists(dir.resolve("groups.txt")));
    }

    @Test
    void flyDoesNotTouchNormalData() throws Exception {
        run(new String[0], SCRIPT);
        String before = Files.readString(dir.resolve("users.txt"));
        String out = run(new String[] {"--fly"},
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\nEXIT\n");
        assertTrue(out.startsWith("USER CREATED"), "fly starts from empty independent data");
        assertEquals(before, Files.readString(dir.resolve("users.txt")));
    }

    @Test
    void flyKeepsExistingTmpRoot() throws Exception {
        Files.createDirectories(dir.resolve("tmp").resolve("other"));
        run(new String[] {"--fly"}, "EXIT\n");
        assertTrue(Files.exists(dir.resolve("tmp").resolve("other")));
        try (var entries = Files.list(dir.resolve("tmp"))) {
            assertEquals(1, entries.count());
        }
    }

    @Test
    void normalModePersists() {
        run(new String[0], SCRIPT);
        assertTrue(Files.exists(dir.resolve("users.txt")));
        assertTrue(Files.exists(dir.resolve("attractions.txt")));
        assertTrue(Files.exists(dir.resolve("groups.txt")));
    }
}
