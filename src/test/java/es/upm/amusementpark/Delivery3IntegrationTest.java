package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.AttractionType;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.UserDirectory;
import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end Delivery 3 flow through the real command wiring and persistence.
 */
class Delivery3IntegrationTest {

    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X");
    private static final UserDirectory DIRECTORY = new UserDirectory() {
        @Override
        public boolean exists(String email) {
            return USERS.containsKey(email);
        }

        @Override
        public String dniOf(String email) {
            return USERS.get(email);
        }
    };

    static final String FLOW = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T002;pedro@correo.com;2027-04-20",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "SPEEDPASS CREATE;SP01;T001;1",
            "SPEEDPASS CREATE;SP02;T002;10",
            "SPECIALPASS CREATE;PE01;T001",
            "SPEEDPASS CREATE;SP05;TG01:12345678Z;1",
            "SPEEDPASS CREATE;SP06;TG01:87654321X;1",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION ENTER;A04;T001;SPEED",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION STATUS;A03",
            "SPEEDPASS DELETE;SP02:3",
            "SPECIALPASS DELETE;PE01",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "HELP EXTENDED",
            "EXIT",
            "GROUP CREATE;G99;After exit");

    @TempDir
    Path dir;

    @Test
    void fullFlowThenRestart() throws IOException {
        ParkController park = ParkController.getInstance();
        park.reset();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        App.registerCommands(processor, park, out);
        park.setUserDirectory(DIRECTORY);
        ParkStorage storage = ParkStorage.inDirectory(dir.resolve("data"));
        park.setRepository(storage);
        processor.setAfterCommand(park::saveState);
        processor.run(new BufferedReader(new StringReader(FLOW)));
        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("GROUP TICKET CREATED. TOTAL PRICE: 80.00"), text);
        assertTrue(text.contains("SPEEDPASS PURCHASED: 10 pass(es). TOTAL PRICE: 80.00"));
        assertTrue(text.contains("SPECIALPASS PURCHASED. PRICE: 40.00"));
        assertTrue(text.contains("ERROR: Attraction A02 (INDIVIDUAL) does not accept group tickets"));
        assertTrue(text.contains("CONSUMED PASS: SpeedPass SP01:1"));
        assertTrue(text.contains("CONSUMED PASS: Special Pass PE01"));
        assertTrue(text.contains("does not accept SpeedPasses") || text.contains("is already inside"));
        assertTrue(text.contains("CONSUMED PASS: SpeedPass SP05:1"));
        assertTrue(text.contains("CONSUMED PASS: SpeedPass SP06:1"));
        assertTrue(text.contains("  TYPE: GROUP"));
        assertTrue(text.contains("SPEEDPASS REFUNDED"));
        assertTrue(text.contains("ERROR: Pass PE01 has been consumed and cannot be refunded"));
        assertTrue(text.contains("INVOICE - USER"));
        assertTrue(text.contains("INVOICE - GROUP"));
        assertTrue(text.contains("Available commands (extended):"));
        assertTrue(text.endsWith("Exiting application." + System.lineSeparator()));

        park.reset();
        park.setUserDirectory(DIRECTORY);
        park.setRepository(storage);
        assertEquals(List.of(), park.loadState());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(park.findPass("PE01").isConsumed());
        assertTrue(park.findPass("SP05:1").isConsumed());
        assertEquals(new BigDecimal("8.00"), park.findPass("SP02:1").getPrice());
        assertEquals(9, park.getPasses().stream().filter(p -> p.getId().startsWith("SP02:")).count());
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertEquals(null, park.findGroup("G99"));
    }

    @Test
    void phaseThreeFileInFlyModeLeavesNoData() throws IOException {
        Path input = dir.resolve("input_fase3.txt");
        Files.writeString(input, FLOW);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[] {"--fly", input.toString()}, new BufferedReader(new StringReader("")),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir);
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("ATTRACTION CREATED"));
        assertTrue(text.contains("Exiting application."));
        assertFalse(Files.exists(dir.resolve("data")));
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
