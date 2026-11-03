package es.upm.etsisi.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandRunnerTest {

    private static final String SEQUENCE = String.join("\n",
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "   ",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER CREATE;Bad;User;12345678A;bad@correo.com;Clave1!;600000000;1111222233334444",
            "HELP");

    @TempDir
    Path dir;

    private record Outcome(String output, List<String> stored) {
    }

    private Outcome runFrom(BufferedReader reader, String dataName) throws IOException {
        Path dataDir = dir.resolve(dataName);
        Park park = TestParks.start(dataDir);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        new CommandRunner(processor).run(reader);
        return new Outcome(buffer.toString(StandardCharsets.UTF_8), Files.readAllLines(dataDir.resolve("users.txt")));
    }

    @Test
    void interactiveAndFileInputBehaveIdentically() throws IOException {
        Outcome interactive = runFrom(new BufferedReader(new StringReader(SEQUENCE)), "interactive");

        Path commands = dir.resolve("input.txt");
        Files.writeString(commands, SEQUENCE);
        Outcome file;
        try (BufferedReader reader = Files.newBufferedReader(commands)) {
            file = runFrom(reader, "file");
        }

        assertEquals(interactive, file);
        assertEquals(List.of(
                "Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;699999999;1234567890123456",
                "Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654"),
                file.stored());
    }

    @Test
    void blankLinesProduceNoOutput() throws IOException {
        Outcome outcome = runFrom(new BufferedReader(new StringReader(SEQUENCE)), "data");
        List<String> lines = outcome.output().lines().toList();
        assertEquals(List.of("USER CREATED", "USER CREATED", "USER UPDATED"), lines.subList(0, 3));
        assertEquals("ERROR: invalid DNI 12345678A", lines.get(3));
    }

    @Test
    void commandsAfterExitAreNotExecuted() throws IOException {
        Park service = TestParks.start(dir);
        CommandProcessor processor = new CommandProcessor(service, new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        String file = String.join("\n",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "EXIT",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        new CommandRunner(processor).run(new BufferedReader(new StringReader(file)));
        assertNotNull(service.findUser("ana@correo.com"));
        assertNull(service.findUser("pedro@correo.com"));
    }
}
