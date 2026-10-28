package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import es.upm.etsisi.amusementpark.business.model.Pass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Delivery3PersistenceIntegrationTest {

    private static String run(Path workDir, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream("EXIT\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void delivery3StateSurvivesRestart(@TempDir Path workDir) throws Exception {
        Path data = Files.createDirectories(workDir.resolve("data"));
        Files.writeString(data.resolve("users.txt"),
                "12345678Z;ana@correo.com;;Ana;Garcia Lopez;612345678;1234567890123456\n");
        Path input = workDir.resolve("input.txt");
        Files.writeString(input, String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "TICKET CREATE;T002;ana@correo.com;2027-04-20",
                "SPECIALPASS CREATE;PE01;T001",
                "SPEEDPASS CREATE;SP01;T001;1",
                "SPEEDPASS CREATE;SP02;T002;10",
                "ATTRACTION ENTER;A04;T001",
                "ATTRACTION ENTER;A01;T001;SPEED",
                "TICKET DELETE;T002",
                "EXIT"));
        String out = run(workDir, input.toString());
        assertFalse(out.contains("ERROR"), out);

        run(workDir);
        ParkController park = ParkController.getInstance();
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").orElseThrow().getType());
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").orElseThrow().getType());
        assertTrue(park.findAttraction("A04").orElseThrow().getOccupants().isEmpty());
        Pass special = park.findPass("PE01").orElseThrow();
        assertTrue(special.isConsumed());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertEquals("T001", special.getTicket().getIdentifier());
        assertTrue(park.findPass("SP01:1").orElseThrow().isConsumed());
        assertEquals(new BigDecimal("8.00"), park.findPass("SP01:1").orElseThrow().getPrice());
        assertTrue(park.findTicket("T002").isEmpty());
        assertTrue(park.getPasses().stream().noneMatch(pass -> pass.getIdentifier().startsWith("SP02")));
        assertEquals(2, park.getPasses().size());
    }

    @Test
    void flyModeCoversDelivery3Elements(@TempDir Path normalDir, @TempDir Path flyDir) throws Exception {
        String commands = String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
                "ATTRACTION STATUS;A03",
                "SPEEDPASS CREATE;SP01;NOPE;1",
                "EXIT");
        Files.writeString(normalDir.resolve("input.txt"), commands);
        Files.writeString(flyDir.resolve("input.txt"), commands);
        String normal = run(normalDir, normalDir.resolve("input.txt").toString());
        String fly = run(flyDir, "--fly", flyDir.resolve("input.txt").toString());
        assertEquals(normal, fly);
        assertTrue(Files.readString(normalDir.resolve("data").resolve("attractions.txt")).contains("A03;Rafting Familiar;24;GROUP"));
        assertTrue(Files.exists(normalDir.resolve("data").resolve("passes.txt")));
        assertFalse(Files.exists(flyDir.resolve("data")));
        try (Stream<Path> left = Files.list(flyDir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
