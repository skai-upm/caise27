package es.upm.park;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Delivery 3 elements survive restarts and are isolated in --fly runs. */
class Delivery3PersistenceTest {

    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H");

    private static final String SESSION = String.join("\n",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting;24;GROUP",
            "ATTRACTION CREATE;A04;Terror;15;SPECIAL",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T002;ana@correo.com;2027-04-20",
            "GROUP CREATE;G01;Amigos",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "SPEEDPASS CREATE;SP01;T001;1",
            "SPEEDPASS CREATE;SP02;T001;1",
            "SPECIALPASS CREATE;PE01;T001",
            "SPECIALPASS CREATE;PE02;TG01:87654321X",
            "SPEEDPASS CREATE;SP03;T002;10",
            "SPECIALPASS CREATE;PE03;T002",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "TICKET DELETE;T002",
            "EXIT");

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

    private Path file(String name, String content) throws IOException {
        return Files.writeString(base.resolve(name), content, StandardCharsets.UTF_8);
    }

    private static Pass pass(String id) {
        return ParkController.getInstance().findPass(id).orElseThrow();
    }

    @Test
    void restartRestoresTypesPassesAndStates() throws IOException {
        String first = run(file("session.txt", SESSION).toString());
        assertTrue(first.contains("ENTERED A01: ana@correo.com (CONSUMED: SP01:1)"), first);
        assertTrue(first.contains("ENTERED A04: ana@correo.com (CONSUMED: PE01)"), first);
        assertTrue(first.contains("TICKET DELETED"), first);

        ParkControllerTestSupport.reset();
        String second = run(file("check.txt", "SYSTEM DATE;2027-04-15\nATTRACTION STATUS;A04\nEXIT\n").toString());
        ParkController park = ParkController.getInstance();
        assertTrue(second.contains("TYPE: SPECIAL") && second.contains("INSIDE: 0"), second);
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A02").orElseThrow().getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A03").orElseThrow().getType());
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").orElseThrow().getType());

        assertTrue(pass("SP01:1").isConsumed(), "consumed SpeedPass kept");
        assertFalse(pass("SP02:1").isConsumed());
        assertTrue(pass("PE01") instanceof SpecialPass && pass("PE01").isConsumed());
        assertEquals(new BigDecimal("8.00"), pass("SP02:1").getPrice());
        assertTrue(pass("SP02:1") instanceof SpeedPass);
        assertEquals("T001", pass("SP02:1").getTicket().getId());
        assertEquals("TG01:87654321X", pass("PE02").getTicket().getId());
        assertEquals(new BigDecimal("40.00"), pass("PE02").getPrice());
        assertTrue(park.findPass("SP03:1").isEmpty(), "cascaded with T002");
        assertTrue(park.findPass("PE03").isEmpty(), "cascaded with T002");
        assertTrue(park.findTicket("T002").isEmpty());
        assertTrue(park.findPass("SP01:1").orElseThrow().getTicket() == park.findTicket("T001").orElseThrow(),
                "pass references the restored ticket object");
    }

    @Test
    void flyRunHandlesDelivery3ElementsAndLeavesNothing() throws IOException {
        run(file("seed.txt", "ATTRACTION CREATE;A09;Normal;5\nEXIT\n").toString());
        Path data = base.resolve("data");
        String before = Files.readString(data.resolve("attractions.txt"));
        ParkControllerTestSupport.reset();
        String out = run("--fly", file("session.txt", SESSION).toString());
        assertTrue(out.contains("(CONSUMED: SP01:1)") && out.contains("(CONSUMED: PE01)"), out);
        Path tmp = base.resolve("tmp");
        if (Files.exists(tmp)) {
            try (Stream<Path> entries = Files.list(tmp)) {
                assertEquals(0, entries.count());
            }
        }
        assertEquals(before, Files.readString(data.resolve("attractions.txt")));
        assertFalse(Files.exists(data.resolve("passes.txt")) && Files.readString(data.resolve("passes.txt")).contains("SP01"));
    }
}
