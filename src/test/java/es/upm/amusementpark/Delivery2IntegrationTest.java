package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** All Delivery 2 features working together. */
class Delivery2IntegrationTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        park.reset();
        for (int i = 1; i <= 6; i++) {
            park.registerUser(new User("u" + i + "@correo.com", "0000000" + i + "X"));
        }
        park.registerUser(new User("ana@correo.com", "12345678Z"));
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String fragment) {
        return text.split(java.util.regex.Pattern.quote(fragment), -1).length - 1;
    }

    @Test
    void fullDelivery2Flow() throws IOException {
        StringBuilder script = new StringBuilder();
        script.append("SYSTEM DATE;2027-04-15\n").append("SYSTEM PRICE;40.00\n")
                .append("GROUP CREATE;G02;Seis\n");
        for (int i = 1; i <= 6; i++) {
            script.append("GROUP ADD;G02;u").append(i).append("@correo.com\n");
        }
        script.append("GROUPTICKET CREATE;TG02;G02;2027-04-15\n")
                .append("GROUP REMOVE;G02;u1@correo.com\n")
                .append("TICKET CREATE;T001;ana@correo.com;2027-04-15\n")
                .append("TICKET CREATE;T002;ana@correo.com;2027-04-20\n")
                .append("ATTRACTION CREATE;A01;Dragon Rojo;20\n")
                .append("ATTRACTION CREATE;A02;Torre del Cielo;6\n")
                .append("ATTRACTION ENTER;A02;TG02\n")
                .append("ATTRACTION ENTER;A02;T001\n")
                .append("ATTRACTION ENTER;A01;T001\n")
                .append("ATTRACTION EXIT;A02;u1@correo.com\n")
                .append("ATTRACTION STATUS;A02\n")
                .append("ATTRACTION LIST;A01\n")
                .append("TICKET DELETE;T001\n")
                .append("TICKET DELETE;T002\n")
                .append("GROUPTICKET DELETE;TG02\n")
                .append("GROUP DELETE;G02\n")
                .append("HELP\n")
                .append("EXIT\n")
                .append("GROUP CREATE;G99;Never\n");
        String out = run(script.toString());

        assertTrue(out.contains("GROUP TICKET CREATED. TOTAL PRICE: 192.00 EUR"));
        assertTrue(out.contains("cannot be modified while it has an active group ticket"));
        assertTrue(out.contains("TICKET CREATED. PRICE: 40.00 EUR"));
        assertEquals(6, count(out, "ENTERED ATTRACTION A02"));
        assertTrue(out.contains("does not have enough capacity"));
        assertTrue(out.contains("USER ana@correo.com ENTERED ATTRACTION A01"));
        assertTrue(out.contains("USER u1@correo.com LEFT ATTRACTION A02"));
        assertTrue(out.contains("USERS INSIDE: 5"));
        assertTrue(out.contains("AVAILABLE PLACES: 1"));
        assertTrue(out.contains("must be kept"));
        assertTrue(out.contains("TICKET DELETED"));
        assertTrue(out.contains("has a group ticket for the current date or later"));
        assertTrue(out.contains("Available commands:"));
        assertNull(park.findGroup("G99"));
        assertTrue(park.findTicket("T001") != null && park.findTicket("TG02") != null);
        assertNull(park.findTicket("T002"));
    }

    @Test
    void historicalGroupTicketReleasesLockAndGroupDeletion() throws IOException {
        String out = run("SYSTEM DATE;2027-04-15\nGROUP CREATE;G01;F\nGROUP ADD;G01;ana@correo.com\n"
                + "GROUPTICKET CREATE;TG01;G01;2027-04-15\nSYSTEM DATE;2027-04-16\n"
                + "GROUP ADD;G01;u1@correo.com\nGROUP DELETE;G01\n");
        assertTrue(out.contains("USER ADDED TO GROUP"));
        assertTrue(out.contains("GROUP DELETED"));
        assertNull(((GroupTicket) park.findTicket("TG01")).getGroup());
        assertEquals(1, ((GroupTicket) park.findTicket("TG01")).getComposition().size());
    }

    private String app(String stdin, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void bothExecutionModesWithCommandFile() throws IOException {
        Path input = base.resolve("input_fase2.txt");
        Files.writeString(input, "SYSTEM DATE;2027-04-15\nGROUP CREATE;G01;Familia Garcia\n"
                + "ATTRACTION CREATE;A01;Dragon Rojo;20\nTICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "ATTRACTION STATUS;A01\nHELP\nEXIT\n", StandardCharsets.UTF_8);

        String fly = app("", App.FLY, input.toString());
        assertTrue(fly.contains("GROUP CREATED"));
        assertTrue(fly.contains("Error: user not found: ana@correo.com"));
        try (Stream<Path> left = Files.list(base.resolve(App.TEMP_DIRECTORY))) {
            assertFalse(left.findAny().isPresent());
        }
        assertFalse(Files.exists(base.resolve(App.DATA_DIRECTORY)));

        String normal = app("", input.toString());
        assertTrue(normal.contains("ATTRACTION CREATED"));
        String restarted = app("ATTRACTION STATUS;A01\nGROUP CREATE;G01;Otro\nEXIT\n");
        assertTrue(restarted.contains("NAME: Dragon Rojo"));
        assertTrue(restarted.contains("Error: group already exists: G01"));
    }
}
