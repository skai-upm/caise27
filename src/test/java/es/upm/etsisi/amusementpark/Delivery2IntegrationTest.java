package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Delivery2IntegrationTest {

    private static final String NL = System.lineSeparator();

    private static String run(Path workDir, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(new byte[0]), new PrintStream(buffer, true, StandardCharsets.UTF_8),
                workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String fragment) {
        int count = 0;
        for (int i = text.indexOf(fragment); i >= 0; i = text.indexOf(fragment, i + 1)) {
            count++;
        }
        return count;
    }

    @Test
    void fullScenarioWithStoredUsersAndRestart(@TempDir Path workDir) throws Exception {
        Path data = Files.createDirectories(workDir.resolve("data"));
        Files.writeString(data.resolve("users.txt"), "12345678Z;ana@correo.com\n87654321X;pedro@correo.com\n"
                + "11111111H;luis@correo.com\n22222222J;maria@correo.com\n");
        Path input = workDir.resolve("input_fase2.txt");
        Files.writeString(input, String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "SYSTEM PRICE;40",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com",
                "GROUP ADD;G01;luis@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "GROUP REMOVE;G01;luis@correo.com",
                "TICKET CREATE;T001;maria@correo.com;2027-04-15",
                "TICKET CREATE;T002;maria@correo.com;2027-04-14",
                "TICKET DELETE;T001",
                "ATTRACTION CREATE;A01;Dragon Rojo;4",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION ENTER;A01;T001",
                "ATTRACTION EXIT;A01;pedro@correo.com",
                "ATTRACTION LIST;A01",
                "ATTRACTION STATUS;A01",
                "GROUP DELETE;G01",
                "HELP",
                "EXIT",
                "TICKET CREATE;T999;ana@correo.com;2027-04-20"));
        String out = run(workDir, input.toString());
        assertTrue(out.contains("GROUPTICKET CREATED. TOTAL PRICE: 108.00"), out);
        assertTrue(out.contains("TICKET CREATED. PRICE: 40.00"), out);
        assertTrue(out.contains("ATTRACTION ENTERED: 3 user(s) in A01"), out);
        assertTrue(out.contains("ATTRACTION ENTERED: 1 user(s) in A01"), out);
        assertTrue(out.contains("ana@correo.com" + NL + "luis@correo.com" + NL + "maria@correo.com" + NL), out);
        assertTrue(out.contains("AVAILABLE PLACES: 1"), out);
        assertEquals(5, count(out, "ERROR"), out);
        assertTrue(out.contains("Exiting application."));
        assertTrue(!out.contains("T999"));

        Path restart = workDir.resolve("restart.txt");
        Files.writeString(restart, "SYSTEM DATE;2027-04-15\nATTRACTION LIST;A01\nGROUP ADD;G01;maria@correo.com\n"
                + "TICKET DELETE;T001\nEXIT\n");
        String after = run(workDir, restart.toString());
        assertTrue(after.contains("NO USERS IN ATTRACTION A01"), after);
        assertTrue(after.contains("ERROR: Group G01 has an active group ticket"), after);
        assertTrue(after.contains("ERROR: Ticket T001 is for the current day"), after);
    }

    @Test
    void sameOutputInNormalAndFlyModeWithoutLeftovers(@TempDir Path normalDir, @TempDir Path flyDir)
            throws Exception {
        String commands = String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "SYSTEM PRICE;45.50",
                "SYSTEM PRICE;-1",
                "GROUP CREATE;G01;Amigos",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION STATUS;A01",
                "ATTRACTION ENTER;A01;NOPE",
                "GROUP DELETE;G01",
                "EXIT");
        Path normalInput = normalDir.resolve("input_fase2.txt");
        Files.writeString(normalInput, commands);
        Path flyInput = flyDir.resolve("input_fase2.txt");
        Files.writeString(flyInput, commands);
        String normal = run(normalDir, normalInput.toString());
        String fly = run(flyDir, "--fly", flyInput.toString());
        assertEquals(normal, fly);
        assertTrue(Files.exists(normalDir.resolve("data").resolve("attractions.txt")));
        assertTrue(!Files.exists(flyDir.resolve("data")));
        try (Stream<Path> left = Files.list(flyDir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
