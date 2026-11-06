package es.upm.park;

import es.upm.park.controller.ParkController;
import es.upm.park.controller.ParkControllerTestSupport;
import es.upm.park.controller.TestUsers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end Delivery 2 session through the packaged entry point. */
class Delivery2IntegrationTest {

    private static final String SESSION = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "SYSTEM PRICE;40.00",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;ana@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;1",
            "TICKET CREATE;T001;luis@correo.com;2027-04-15",
            "TICKET CREATE;T002;luis@correo.com;2027-04-20",
            "TICKET CREATE;T003;luis@correo.com;2027-04-14",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "GROUP REMOVE;G01;pedro@correo.com",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION ENTER;A01;T001",
            "ATTRACTION EXIT;A01;pedro@correo.com",
            "ATTRACTION LIST;A01",
            "ATTRACTION STATUS;A01",
            "TICKET DELETE;T001",
            "TICKET DELETE;T002",
            "GROUPTICKET DELETE;TG01",
            "GROUP DELETE;G01",
            "HELP",
            "EXIT",
            "GROUP CREATE;G99;Never");

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setUserDirectory(TestUsers.withDnis(Map.of(
                "ana@correo.com", "12345678Z", "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H")));
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new BufferedReader(new StringReader("")),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()));
        return out.toString(StandardCharsets.UTF_8);
    }

    private Path sessionFile() throws IOException {
        return Files.writeString(base.resolve("input_fase2.txt"), SESSION, StandardCharsets.UTF_8);
    }

    private static void assertSessionOutput(String out) {
        assertTrue(out.contains("SYSTEM DATE UPDATED: 2027-04-15"));
        assertTrue(out.contains("ERROR: User ana@correo.com already belongs to group G01"));
        assertTrue(out.contains("TICKET CREATED. PRICE: 40.00"));
        assertTrue(out.contains("ERROR: Tickets cannot be created for a date before 2027-04-15"));
        assertTrue(out.contains("GROUP TICKET CREATED. TOTAL PRICE: 80.00"));
        assertTrue(out.contains("ERROR: Group G01 has an active group ticket and cannot be modified"));
        assertTrue(out.contains("ERROR: Attraction A02 has 1 free places for 2 users"));
        assertTrue(out.contains("ENTERED A01: ana@correo.com, pedro@correo.com"));
        assertTrue(out.contains("ENTERED A01: luis@correo.com"));
        assertTrue(out.contains("EXITED A01: pedro@correo.com"));
        assertTrue(out.contains("INSIDE: 2 | AVAILABLE: 18"));
        assertTrue(out.contains("ERROR: Ticket T001 is dated today or earlier and is kept as a historical record"));
        assertTrue(out.contains("TICKET DELETED"));
        assertTrue(out.contains("ERROR: Group ticket TG01 is dated today or earlier"));
        assertTrue(out.contains("ERROR: Group G01 has a group ticket for today or a later date and cannot be deleted"));
        assertTrue(out.contains("Available commands:"));
        assertTrue(out.contains("Exiting the application."));
        assertFalse(out.contains("G99"));
    }

    @Test
    void normalRunPersistsAndRestartsWithEmptyAttractions() throws IOException {
        assertSessionOutput(run(sessionFile().toString()));
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setUserDirectory(TestUsers.of("ana@correo.com"));
        Path check = Files.writeString(base.resolve("check.txt"), String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION STATUS;A01",
                "GROUP DELETE;G01",
                "GROUPTICKET CREATE;TG01;G01;2027-04-16",
                "TICKET CREATE;T001;ana@correo.com;2027-04-16",
                "EXIT"), StandardCharsets.UTF_8);
        String out = run(check.toString());
        assertTrue(out.contains("INSIDE: 0 | AVAILABLE: 20"), "attractions restart empty");
        assertTrue(out.contains("cannot be deleted"), "group ticket TG01 restored and still locks G01");
        assertTrue(out.contains("A ticket with identifier TG01 already exists"));
        assertTrue(out.contains("A ticket with identifier T001 already exists"));
    }

    @Test
    void flyRunExecutesSameFileAndLeavesNoData() throws IOException {
        assertSessionOutput(run("--fly", sessionFile().toString()));
        Path tmp = base.resolve("tmp");
        if (Files.exists(tmp)) {
            try (Stream<Path> entries = Files.list(tmp)) {
                assertEquals(0, entries.count());
            }
        }
        assertFalse(Files.exists(base.resolve("data")));
    }
}
