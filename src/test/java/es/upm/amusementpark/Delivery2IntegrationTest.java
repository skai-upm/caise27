package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.business.GroupTicket;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.UserDirectory;
import es.upm.amusementpark.persistence.ParkStorage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end Delivery 2 flow through the real command wiring and persistence.
 */
class Delivery2IntegrationTest {

    /** Stand-in for user management, which is not part of the code base yet (see change proposal). */
    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H");
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

    private static final String FLOW = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;2",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUP ADD;G01;ana@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "SYSTEM PRICE;50",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "GROUPTICKET CREATE;TG02;G01;2027-04-20",
            "GROUP REMOVE;G01;luis@correo.com",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION EXIT;A01;pedro@correo.com",
            "ATTRACTION STATUS;A01",
            "ATTRACTION LIST;A02",
            "TICKET DELETE;T001",
            "GROUPTICKET DELETE;TG02",
            "HELP",
            "EXIT",
            "GROUP CREATE;G99;After exit");

    @TempDir
    Path dir;

    @Test
    void fullFlowThenRestart() throws IOException {
        ParkController park = ParkController.getInstance();
        park.reset();
        ParkStorage storage = new ParkStorage(dir.resolve("data"));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        App.registerCommands(processor, park, out);
        park.setUserDirectory(DIRECTORY);
        processor.setAfterCommand(() -> {
            try {
                storage.save(park);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        processor.run(new BufferedReader(new StringReader(FLOW)));
        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("ERROR: User ana@correo.com already belongs to group G01"));
        assertTrue(text.contains("TICKET CREATED. PRICE: 40.00"));
        assertTrue(text.contains("GROUP TICKET CREATED. TOTAL PRICE: 135.00"));
        assertTrue(text.contains("ERROR: Group G01 has an active group ticket"));
        assertTrue(text.contains("does not have capacity for 3 user(s)"));
        assertTrue(text.contains("ENTERED ATTRACTION A01: ana@correo.com, pedro@correo.com, luis@correo.com"));
        assertTrue(text.contains("INSIDE: 2"));
        assertTrue(text.contains("AVAILABLE: 18"));
        assertTrue(text.contains("ATTRACTION A02 USERS: (none)"));
        assertTrue(text.contains("ERROR: Ticket T001 is for today or a past date and must be kept"));
        assertTrue(text.contains("GROUP TICKET DELETED"));
        assertTrue(text.contains("ATTRACTION STATUS;<attractionIdentifier>"));
        assertTrue(text.endsWith("Exiting application." + System.lineSeparator()));
        assertNull(park.findGroup("G99"));

        park.reset();
        assertEquals(java.util.List.of(), storage.load(park));
        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        assertEquals(new BigDecimal("135.00"), tg01.getPrice());
        assertEquals(3, tg01.getComponents().size());
        assertEquals(park.findGroup("G01"), tg01.getGroup());
        assertNull(park.findTicket("TG02"));
        assertEquals(new BigDecimal("40.00"), park.findTicket("T001").getPrice());
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        assertEquals(3, park.findGroup("G01").getMembers().size());
    }

    @Test
    void phaseTwoFileRunsInFlyModeWithoutLeavingData() throws IOException {
        Path input = dir.resolve("input_fase2.txt");
        Files.writeString(input, FLOW);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[] {"--fly", input.toString()}, new BufferedReader(new StringReader("")),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir);
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("SYSTEM DATE UPDATED: 2027-04-15"));
        assertTrue(text.contains("ATTRACTION CREATED"));
        assertTrue(text.contains("GROUP CREATED"));
        assertTrue(text.contains("Exiting application."));
        assertFalse(Files.exists(dir.resolve("data")));
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
