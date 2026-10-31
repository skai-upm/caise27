package es.upm.amusementpark.presentation;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** End-to-end E1/E2/F3 scenario through {@link App} in every execution mode. */
class Delivery3IntegrationTest {

    private static final String SCRIPT = String.join("\n",
            "SYSTEM DATE;2026-10-01",
            ParkTestSupport.ANA,
            ParkTestSupport.PEDRO,
            "USER CREATE;Luis;Perez;00000000T;luis@correo.com;Clave1!;600000000;1111222233334444",
            "",
            "GROUP CREATE;G01;Familia",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUP ADD;G01;ana@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2026-10-01",
            "GROUPTICKET CREATE;TG01;G01;2026-10-01",
            "SPEEDPASS CREATE;T001;10",
            "SPECIALPASS CREATE;T001",
            "SPEEDPASS CREATE;TG01-1;1",
            "SPEEDPASS CREATE;TG01-2;1",
            "ATTRACTION CREATE;A01;Tornado;20",
            "ATTRACTION CREATE;A02;Casa;5;SPECIAL",
            "ATTRACTION CREATE;A03;Grupal;10;GROUP",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A02;T001;SPEED",
            "ATTRACTION ENTER;A02;T001",
            "ATTRACTION STATUS;A01",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "HELP",
            "HELP EXTENDED",
            "USER DELETE;ana@correo.com;Clave1!",
            "EXIT",
            "USER CREATE;Nunca;Ejecutado;11111111H;n@correo.com;Clave1!;1;1234567890123456") + "\n";

    @TempDir
    Path dir;

    private String run(Path dataDir, String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8), dataDir);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullScenarioFromCommandFile() throws IOException {
        Path commands = dir.resolve("input_fase3.txt");
        Files.writeString(commands, SCRIPT);
        Path data = dir.resolve("normal");
        String output = run(data, "", commands.toString());

        assertTrue(output.contains("OK SPEEDPASS CREATED -> 80.00"));
        assertTrue(output.contains("OK: ticket T001 entered attraction A01 (consumed: SpeedPass T001-SPEED1)"));
        assertTrue(output.contains("ERROR: ticket TG01-3 has no unused SpeedPass"));
        assertTrue(output.contains("ERROR: SPEED is not accepted at SPECIAL attractions"));
        assertTrue(output.contains("(consumed: Special Pass T001-SPECIAL)"));
        assertTrue(output.contains("TOTAL: 160.00"));
        assertTrue(output.contains("TOTAL: 108.00"));
        assertTrue(output.contains("Available commands (extended):"));
        assertTrue(output.contains("ERROR: user ana@correo.com has tickets dated on or after 2026-10-01"));
        assertFalse(output.contains("n@correo.com"));
        assertFalse(Files.readString(data.resolve("users.txt")).contains("Clave1!"));

        String restarted = run(data, "SYSTEM DATE;2026-10-01\nATTRACTION ENTER;A02;T001\n"
                + "ATTRACTION STATUS;A02\nUSER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria\n");
        assertTrue(restarted.contains("ERROR: ticket T001 has no unused Special Pass"));
        assertTrue(restarted.contains("(SPECIAL)"));
        assertTrue(restarted.contains("OK: user ana@correo.com updated"));
    }

    @Test
    void interactiveFileAndFlyModesAgree() throws IOException {
        Path commands = dir.resolve("input_fase3.txt");
        Files.writeString(commands, SCRIPT);

        String fromFile = run(dir.resolve("file"), "", commands.toString());
        String interactive = run(dir.resolve("interactive"), SCRIPT);
        Path flyRoot = dir.resolve("fly");
        String fly = run(flyRoot, "", App.FLY, commands.toString());

        assertEquals(fromFile, interactive);
        assertEquals(fromFile, fly);
        assertFalse(Files.exists(flyRoot.resolve("passes.txt")));
        try (var runs = Files.list(flyRoot.resolve("tmp"))) {
            assertEquals(0, runs.count());
        }
    }
}
