package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.ApplicationInitializer;
import es.upm.etsisi.amusementpark.ParkFixture;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CommandRunnerTest {

    @TempDir
    Path dir;

    @Test
    void processesInOrderSkipsBlankLinesAndStopsAtExit() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandRunner runner = new CommandRunner(ApplicationInitializer.initialize(dir, ParkFixture.FAST_HASHER,
                new PrintStream(buffer, true, StandardCharsets.UTF_8)));
        ParkController park = ParkController.getInstance();

        String input = String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "",
                "   ",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        runner.run(new BufferedReader(new StringReader(input)));

        assertEquals(1, park.users().findAll().size());
        assertEquals("699999999", park.users().findByEmail("ana@correo.com").orElseThrow().getPhone());
        assertFalse(buffer.toString(StandardCharsets.UTF_8).contains("ERROR"));
    }
}
