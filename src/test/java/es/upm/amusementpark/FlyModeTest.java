package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FlyModeTest {

    private static final String DAY = LocalDate.now().plusDays(1).toString();
    private static final String COMMANDS = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "TICKET CREATE;T001;ana@correo.com;" + DAY,
            "GROUPTICKET CREATE;TG01;G01;" + DAY,
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "EXIT") + "\n";

    @TempDir
    Path base;

    private String run(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = Main.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()), base);
        assertEquals(0, code);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void flyWithFileLeavesNoDataBehind() throws IOException {
        Path input = Files.writeString(base.resolve("input_fase2.txt"), COMMANDS);
        String out = run("", "--fly", input.toString());
        assertTrue(out.startsWith("USER CREATED\nGROUP CREATED\nGROUP USER ADDED\nTICKET CREATED"), out);
        assertFalse(Files.exists(base.resolve("tmp")));
        assertFalse(Files.exists(base.resolve("users.txt")));
        assertFalse(Files.exists(base.resolve("tickets.txt")));
    }

    @Test
    void flyInteractiveEndedWithExitCleansUp() {
        run(COMMANDS, "--fly");
        assertFalse(Files.exists(base.resolve("tmp")));
    }

    @Test
    void flyDoesNotTouchPermanentData() throws IOException {
        run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n");
        String before = Files.readString(base.resolve("users.txt"));
        String out = run("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n",
                "--fly");
        assertEquals("USER CREATED\n", out);
        assertEquals(before, Files.readString(base.resolve("users.txt")));
    }

    @Test
    void repeatedFlyRunsAreIdentical() throws IOException {
        Path input = Files.writeString(base.resolve("input_fase2.txt"), COMMANDS);
        assertEquals(run("", "--fly", input.toString()), run("", "--fly", input.toString()));
    }

    @Test
    void flyWithMissingFileStillCleansUp() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int code = Main.run(new String[] {"--fly", base.resolve("none.txt").toString()},
                new ByteArrayInputStream(new byte[0]), new PrintStream(new ByteArrayOutputStream()),
                new PrintStream(err, true, StandardCharsets.UTF_8), base);
        assertEquals(1, code);
        assertFalse(Files.exists(base.resolve("tmp")));
    }
}
