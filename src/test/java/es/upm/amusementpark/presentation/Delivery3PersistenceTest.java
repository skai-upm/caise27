package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.SpeedPass;

import org.junit.jupiter.api.Test;

class Delivery3PersistenceTest extends ParkTestSupport {

    private IndividualTicket ticket(String id) {
        return park.findIndividualTicket(id);
    }

    @Test
    void passesAndStatesSurviveRestart() throws IOException {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("SPEEDPASS CREATE;T001;10");
        ok("SPECIALPASS CREATE;T001");
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION ENTER;A01;T001;SPEED");
        restart();

        IndividualTicket ticket = ticket("T001");
        List<SpeedPass> speed = ticket.getSpeedPasses();
        assertEquals(10, speed.size());
        assertTrue(speed.get(0).isConsumed());
        assertFalse(speed.get(1).isConsumed());
        assertEquals(new BigDecimal("8.00"), speed.get(1).getPrice());
        assertEquals("T001-SPECIAL", ticket.getSpecialPass().getId());
        assertEquals(new BigDecimal("40.00"), ticket.getSpecialPass().getPrice());
        assertFalse(ticket.getSpecialPass().isConsumed());
        assertTrue(ticket.isSpecialPassIssued());
    }

    @Test
    void consumedSpecialPassAndRefundHistorySurviveRestart() throws IOException {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-01");
        ok("TICKET CREATE;T002;" + email(1) + ";2026-10-20");
        ok("SPECIALPASS CREATE;T001");
        ok("SPECIALPASS CREATE;T002");
        ok("SPECIALPASS DELETE;T002-SPECIAL");
        ok("ATTRACTION CREATE;A02;Casa;5;SPECIAL");
        ok("ATTRACTION ENTER;A02;T001");
        restart();
        assertTrue(ticket("T001").getSpecialPass().isConsumed());
        assertNull(ticket("T002").getSpecialPass());
        error("SPECIALPASS CREATE;T002");
        error("SPECIALPASS CREATE;T001");
    }

    @Test
    void constituentPassesSurviveRestart() throws IOException {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-2;1");
        restart();
        GroupTicket groupTicket = (GroupTicket) park.findTicket("TG01");
        assertEquals(2, groupTicket.getTickets().size());
        assertEquals("TG01-2-SP1", groupTicket.getTickets().get(1).getSpeedPasses().get(0).getId());
        assertEquals(new BigDecimal("8.00"), groupTicket.getTickets().get(1).getSpeedPasses().get(0).getPrice());
    }

    @Test
    void attractionTypeSurvivesRestart() throws IOException {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION CREATE;A02;Casa;5;SPECIAL");
        ok("ATTRACTION CREATE;A03;Solo;5;INDIVIDUAL");
        ok("ATTRACTION CREATE;A04;Familias;8;GROUP");
        restart();
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A02").getType());
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A03").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A04").getType());
    }

    @Test
    void delivery2FilesStillLoad() throws IOException {
        stopPark();
        Files.write(tempDir.resolve("users.txt"),
                List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        Files.write(tempDir.resolve("tickets.txt"),
                List.of("I;T001;2026-10-15;40.00;12345678Z;Ana;Garcia Lopez;ana@correo.com"));
        Files.write(tempDir.resolve("attractions.txt"), List.of("A01;Tornado;20"));
        startPark();
        assertNotNull(ticket("T001"));
        assertFalse(ticket("T001").isSpecialPassIssued());
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        ok("SPECIALPASS CREATE;T001");
    }

    @Test
    void ticketDeletionCascadesToPasses() throws IOException {
        createUsers(1);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        ok("SPEEDPASS CREATE;T001;10");
        ok("SPECIALPASS CREATE;T001");
        ok("TICKET DELETE;T001");
        restart();
        assertNull(ticket("T001"));
        assertTrue(Files.readAllLines(tempDir.resolve("tickets.txt")).isEmpty());
    }

    @Test
    void groupTicketDeletionCascadesToConstituentPasses() throws IOException {
        createUsers(2);
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;10");
        ok("SPECIALPASS CREATE;TG01-2");
        ok("GROUPTICKET DELETE;TG01");
        restart();
        assertNull(park.findTicket("TG01"));
        assertNull(ticket("TG01-1"));
        assertTrue(Files.readAllLines(tempDir.resolve("tickets.txt")).isEmpty());
        error("SPEEDPASS DELETE;TG01-1-SP1");
    }

    @Test
    void failedDeletionsLeaveEverythingInPlace() throws IOException {
        createUsers(2);
        ok("TICKET CREATE;T001;" + email(1) + ";2026-10-15");
        ok("SPEEDPASS CREATE;T001;1");
        createGroupWithUsers("G01", 2);
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        ok("SPEEDPASS CREATE;TG01-1;1");
        List<String> before = Files.readAllLines(tempDir.resolve("tickets.txt"));

        ok("SYSTEM DATE;2026-10-15");
        error("TICKET DELETE;T001");
        error("GROUPTICKET DELETE;TG01");
        assertEquals(before, Files.readAllLines(tempDir.resolve("tickets.txt")));

        ok("SYSTEM DATE;2026-10-01");
        Path tickets = tempDir.resolve("tickets.txt");
        Files.delete(tickets);
        Files.createDirectory(tickets);
        Files.writeString(tickets.resolve("blocker"), "x");
        error("TICKET DELETE;T001");
        error("GROUPTICKET DELETE;TG01");
        assertEquals(1, ticket("T001").getSpeedPasses().size());
        assertEquals(2, ((GroupTicket) park.findTicket("TG01")).getTickets().size());
        assertEquals(1, ticket("TG01-1").getSpeedPasses().size());
        assertFalse(Files.exists(tempDir.resolve("tickets.txt.tmp")));
    }

    @Test
    void flyIsolatesDelivery3Data() throws IOException {
        Path base = Files.createDirectory(tempDir.resolve("base"));
        String setup = String.join("\n",
                "SYSTEM DATE;2026-10-01",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "TICKET CREATE;T001;ana@correo.com;2026-10-01",
                "SPEEDPASS CREATE;T001;1",
                "ATTRACTION CREATE;A01;Tornado;20",
                "EXIT") + "\n";
        stopPark();
        runApp(new String[0], setup, base);
        List<String> before = snapshot(base);

        String fly = String.join("\n",
                "SYSTEM DATE;2026-10-01",
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "TICKET CREATE;T001;ana@correo.com;2026-10-01",
                "SPEEDPASS CREATE;T001;10",
                "SPECIALPASS CREATE;T001",
                "ATTRACTION CREATE;A02;Casa;5;SPECIAL",
                "ATTRACTION ENTER;A02;T001",
                "EXIT") + "\n";
        String output = runApp(new String[] {"--fly"}, fly, base);
        assertTrue(output.contains("(consumed: Special Pass T001-SPECIAL)"), output);
        assertEquals(before, snapshot(base));
        assertFalse(Files.exists(base.resolve("tmp")));
        startPark();
    }

    private static String runApp(String[] args, String input, Path baseDir) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8), baseDir);
        return out.toString(StandardCharsets.UTF_8);
    }

    private static List<String> snapshot(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(Files::isRegularFile).map(path -> {
                try {
                    return dir.relativize(path) + ":" + Files.readString(path);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }).sorted().toList();
        }
    }
}
