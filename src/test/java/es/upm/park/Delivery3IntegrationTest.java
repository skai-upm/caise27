package es.upm.park;

import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.model.User;
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

/** End-to-end Delivery 3 session combining the features of all deliveries. */
class Delivery3IntegrationTest {

    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H");

    private static final String SESSION = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "ATTRACTION CREATE;A05;Noria;10",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T002;ana@correo.com;2027-04-20",
            "GROUP CREATE;G01;Amigos",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "SPEEDPASS CREATE;SP01;T001;10",
            "SPECIALPASS CREATE;PE01;T001",
            "SPEEDPASS CREATE;SP02;TG01:87654321X;1",
            "SPEEDPASS CREATE;SP03;TG01:11111111H;1",
            "SPEEDPASS CREATE;SP04;T002;1",
            "SPECIALPASS CREATE;PE02;T001",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A04;T001;SPEED",
            "ATTRACTION ENTER;A02;T001;SPEED",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION EXIT;A04;ana@correo.com",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION STATUS;A03",
            "SPEEDPASS DELETE;SP01:2",
            "SPEEDPASS DELETE;SP04:1",
            "TICKET DELETE;T002",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "HELP",
            "HELP EXTENDED",
            "EXIT",
            "GROUP CREATE;G99;Never");

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, base, new BufferedReader(new StringReader("")),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()),
                configured -> USERS.forEach((email, dni) -> configured.users().save(
                        new User("N", "S", dni, email, "$2a$10$h", "600000000", "1234567890123456"))));
        return out.toString(StandardCharsets.UTF_8);
    }

    private Path sessionFile() throws IOException {
        return Files.writeString(base.resolve("input_fase3.txt"), SESSION, StandardCharsets.UTF_8);
    }

    private static void assertSession(String out) {
        assertTrue(out.contains("GROUP TICKET CREATED. TOTAL PRICE: 80.00"), out);
        assertTrue(out.contains("SPEEDPASS PURCHASED. TOTAL PRICE: 80.00"), out);
        assertTrue(out.contains("SPECIALPASS PURCHASED. PRICE: 40.00"), out);
        assertTrue(out.contains("ERROR: Ticket T001 already has a Special Pass"), out);
        assertTrue(out.contains("ERROR: Attraction A02 (INDIVIDUAL) does not accept ticket TG01"), out);
        assertTrue(out.contains("ERROR: Attraction A04 (SPECIAL) does not accept SpeedPasses"), out);
        assertTrue(out.contains("ENTERED A02: ana@correo.com (CONSUMED: SP01:1)"), out);
        assertTrue(out.contains("ENTERED A03: pedro@correo.com, luis@correo.com (CONSUMED: SP02:1, SP03:1)"), out);
        assertTrue(out.contains("ENTERED A04: ana@correo.com (CONSUMED: PE01)"), out);
        assertTrue(out.contains("ERROR: Ticket T001 needs an unconsumed Special Pass to enter A04"), out);
        assertTrue(out.contains("TYPE: GROUP | CAPACITY: 24 | INSIDE: 2 | AVAILABLE: 22"), out);
        assertTrue(out.contains("ERROR: Pass SP01:2 belongs to a ticket for today or earlier"), out);
        assertTrue(out.contains("SPEEDPASS REFUNDED"), out);
        assertTrue(out.contains("TICKET DELETED"), out);
        assertTrue(out.contains("INVOICE - USER"), out);
        assertTrue(out.contains("INVOICE - GROUP"), out);
        assertTrue(out.contains("Available commands (extended):"), out);
        assertTrue(out.contains("Exiting the application."), out);
        assertFalse(out.contains("G99"), out);
    }

    @Test
    void normalRunAndRestart() throws IOException {
        String out = run(sessionFile().toString());
        assertSession(out);
        // T001 40 + SP01 x10 (one refund attempt rejected) 80 + PE01 40 = 160.00
        assertTrue(out.contains("TOTAL: 160.00"), out);

        ParkControllerTestSupport.reset();
        Path check = Files.writeString(base.resolve("check.txt"), String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "ATTRACTION STATUS;A04",
                "ATTRACTION ENTER;A04;T001",
                "SPECIALPASS DELETE;PE01",
                "INVOICE CREATE;USER;ana@correo.com",
                "EXIT"), StandardCharsets.UTF_8);
        String restarted = run(check.toString());
        assertTrue(restarted.contains("TYPE: SPECIAL | CAPACITY: 15 | INSIDE: 0"), restarted);
        assertTrue(restarted.contains("needs an unconsumed Special Pass"), "PE01 still consumed after restart");
        assertTrue(restarted.contains("ERROR: Pass PE01 has been consumed"), restarted);
        assertTrue(restarted.contains("TOTAL: 160.00"), restarted);
    }

    @Test
    void flyRunLeavesNoData() throws IOException {
        assertSession(run("--fly", sessionFile().toString()));
        Path tmp = base.resolve("tmp");
        if (Files.exists(tmp)) {
            try (Stream<Path> entries = Files.list(tmp)) {
                assertEquals(0, entries.count());
            }
        }
        assertFalse(Files.exists(base.resolve("data")));
    }
}
