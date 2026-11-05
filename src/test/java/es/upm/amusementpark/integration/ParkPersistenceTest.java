package es.upm.amusementpark.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import es.upm.amusementpark.business.control.Park;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkPersistenceTest {

    private static final String TODAY = LocalDate.now().toString();
    private static final String FUTURE = LocalDate.now().plusDays(10).toString();

    @TempDir
    Path dir;

    private String run(Path base, String... lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeliveryOneIntegrationTest.runApp(new String[0], String.join("\n", lines) + "\n", out,
                new ByteArrayOutputStream(), base.resolve("users.txt"));
        return out.toString(StandardCharsets.UTF_8);
    }

    private String runArgs(Path base, String[] args, String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeliveryOneIntegrationTest.runApp(args, stdin, out, new ByteArrayOutputStream(), base.resolve("users.txt"));
        return out.toString(StandardCharsets.UTF_8);
    }

    private void setUpState() {
        String out = run(dir,
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER CREATE;Luis;Martin;00000011B;luis@correo.com;Clave1!;6;1111222233334444",
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "SYSTEM PRICE;45.50",
                "TICKET CREATE;T001;ana@correo.com;" + FUTURE,
                "GROUPTICKET CREATE;TG01;G01;" + FUTURE,
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "TICKET CREATE;T002;luis@correo.com;" + TODAY,
                "ATTRACTION ENTER;A01;T002");
        assertFalse(out.contains("ERROR"), out);
    }

    @Test
    void stateReconstructedAfterRestart() {
        setUpState();
        String out = run(dir, "ATTRACTION STATUS;A01", "GROUP ADD;G01;luis@correo.com",
                "TICKET CREATE;TG01:12345678Z;ana@correo.com;" + FUTURE, "EXIT");
        Park park = Park.getInstance();
        var g01 = park.groups().findById("G01").orElseThrow();
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), g01.getMembers().stream().map(u -> u.getEmail()).toList());
        IndividualTicket t001 = (IndividualTicket) park.tickets().findById("T001").orElseThrow();
        assertSame(park.users().findByDni("12345678Z").orElseThrow(), t001.getUser());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());
        GroupTicket tg01 = (GroupTicket) park.tickets().findById("TG01").orElseThrow();
        assertSame(g01, tg01.getGroup());
        assertEquals(List.of("TG01:12345678Z", "TG01:87654321X"), tg01.getComposition().stream().map(t -> t.getIdentifier()).toList());
        assertEquals(new BigDecimal("91.00"), tg01.getPrice());
        assertEquals(20, park.attractions().findById("A01").orElseThrow().getCapacity());
        assertTrue(out.contains("Inside: 0"), out);
        assertEquals(2, out.lines().filter(l -> l.startsWith("ERROR")).count(), out);
    }

    @Test
    void historicalRecordsOfDeletedUserAndGroup() {
        String past = LocalDate.now().minusDays(1).toString();
        String out = run(dir,
                "SYSTEM DATE;" + LocalDate.now().minusDays(5),
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com",
                "TICKET CREATE;T001;ana@correo.com;" + past,
                "GROUPTICKET CREATE;TG01;G01;" + past,
                "SYSTEM DATE;" + TODAY,
                "GROUP DELETE;G01",
                "USER DELETE;ana@correo.com;Clave1!",
                "GROUP CREATE;G01;Nuevo");
        assertFalse(out.contains("ERROR"), out);
        run(dir, "EXIT");
        Park park = Park.getInstance();
        assertTrue(park.users().findByDni("12345678Z").isEmpty());
        IndividualTicket t001 = (IndividualTicket) park.tickets().findById("T001").orElseThrow();
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals("ana@correo.com", t001.getUser().getEmail());
        GroupTicket tg01 = (GroupTicket) park.tickets().findById("TG01").orElseThrow();
        assertNull(tg01.getGroup());
        assertEquals("G01", tg01.getGroupIdentifier());
        assertEquals(1, tg01.getComposition().size());
        assertEquals(new BigDecimal("40.00"), tg01.getPrice());
    }

    @Test
    void flyModeLeavesPermanentDataUntouchedAndCleansUp() throws IOException {
        run(dir, "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        List<String> before = Files.readAllLines(dir.resolve("users.txt"));
        String out = runArgs(dir, new String[] {"--fly"},
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\nEXIT\n");
        assertTrue(out.startsWith("USER CREATED"), out);
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "GROUP CREATE;G01;F", "ATTRACTION CREATE;A01;X;5"));
        assertTrue(runArgs(dir, new String[] {"--fly", input.toString()}, "").startsWith("USER CREATED"));
        assertEquals(before, Files.readAllLines(dir.resolve("users.txt")));
        assertFalse(Files.exists(dir.resolve("groups.txt")) && !Files.readAllLines(dir.resolve("groups.txt")).isEmpty());
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }

    @Test
    void normalFormsStillUsePermanentFiles() throws IOException {
        Path input = dir.resolve("input.txt");
        Files.write(input, List.of("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        runArgs(dir, new String[] {input.toString()}, "");
        assertEquals(List.of("A01;Dragon Rojo;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
        assertFalse(Files.exists(dir.resolve("tmp")));
    }
}
