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
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delivery 2 end-to-end run mixing Phase 1 and Phase 2 commands, normally and with --fly.
 */
class Delivery2IntegrationTest {

    private static final String TODAY = LocalDate.now().toString();
    private static final String LATER = LocalDate.now().plusDays(5).toString();

    private static final String INPUT = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "SYSTEM PRICE;45.50",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUPTICKET CREATE;TG01;G01;" + TODAY,
            "TICKET CREATE;T001;luis@correo.com;" + LATER,
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;2",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION EXIT;A01;ana@correo.com",
            "ATTRACTION LIST;A01",
            "ATTRACTION STATUS;A01",
            "GROUP REMOVE;G01;luis@correo.com",
            "USER DELETE;luis@correo.com;Clave3$",
            "TICKET DELETE;T001",
            "GROUPTICKET DELETE;TG01",
            "HELP",
            "EXIT",
            "USER CREATE;X;Y;00000000T;x@correo.com;Clave1!;1;1234567890123456") + "\n";

    @TempDir
    Path tmp;

    private String run(Path base, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = Main.run(args, new ByteArrayInputStream(new byte[0]), new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(new ByteArrayOutputStream()), base);
        assertEquals(0, code);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void phaseTwoFileRunsIdenticallyInNormalAndFlyModes() throws IOException {
        Path input = Files.writeString(tmp.resolve("input_fase2.txt"), INPUT);
        Path normal = Files.createDirectory(tmp.resolve("normal"));
        Path fly = Files.createDirectory(tmp.resolve("fly"));

        String normalOut = run(normal, input.toString());
        String flyOut = run(fly, "--fly", input.toString());

        assertEquals(normalOut, flyOut);
        assertTrue(normalOut.contains("GROUPTICKET CREATED - Total price: 122.85 EUR"), normalOut);
        assertTrue(normalOut.contains("ERROR: Not enough capacity in A02"), normalOut);
        assertTrue(normalOut.contains("EXITED A01: ana@correo.com"), normalOut);
        assertTrue(normalOut.contains("Users inside: 2\nAvailable places: 18"), normalOut);
        assertTrue(normalOut.contains("ERROR: Group has active group tickets and cannot be modified"), normalOut);
        assertTrue(normalOut.contains("ERROR: User has active tickets"), normalOut);
        assertTrue(normalOut.contains("TICKET DELETED"), normalOut);
        assertTrue(normalOut.contains("ERROR: Only group tickets dated after the current date can be deleted"), normalOut);
        assertFalse(normalOut.contains("x@correo.com"), normalOut);
        assertTrue(Files.exists(normal.resolve("users.txt")));
        assertTrue(Files.exists(normal.resolve("tickets.txt")));
        assertFalse(Files.exists(fly.resolve("tmp")));
        assertFalse(Files.exists(fly.resolve("users.txt")));
    }
}
