package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {

    @TempDir
    Path tmp;

    private String run(String[] args, String stdin) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new BufferedReader(new StringReader(stdin)), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                tmp.resolve("base"));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void stdinAndFileProduceIdenticalOutput() throws IOException {
        String commands = "HELP\nEXIT\nHELP\n";
        Path file = tmp.resolve("input.txt");
        Files.writeString(file, commands);
        String fromStdin = run(new String[0], commands);
        String fromFile = run(new String[] {file.toString()}, "");
        assertEquals(fromStdin, fromFile);
        assertEquals(fromStdin.indexOf("Available commands:"), fromStdin.lastIndexOf("Available commands:"));
    }

    @Test
    void newExecutionStartsWithInitialPrice() {
        String out = run(new String[0], "SYSTEM PRICE;45.50\nEXIT\n");
        assertTrue(out.contains("SYSTEM PRICE UPDATED: 45.50"));
        run(new String[0], "EXIT\n");
        assertEquals(new java.math.BigDecimal("40.00"),
                es.upm.amusementpark.park.ParkController.getInstance().getCurrentPrice());
    }

    @Test
    void unreadableFileIsReportedInControlledManner() {
        String out = run(new String[] {tmp.resolve("missing.txt").toString()}, "");
        assertTrue(out.startsWith("ERROR: Cannot read command file:"));
    }
}
