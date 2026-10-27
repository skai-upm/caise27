package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassPersistenceTest {

    private static final String TODAY = LocalDate.now().toString();
    private static final String LATER = LocalDate.now().plusDays(3).toString();

    @TempDir
    Path data;

    private String run(String... lines) {
        return runWith(new String[0], lines);
    }

    private String runWith(String[] args, String... lines) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Main.run(args, new ByteArrayInputStream((String.join("\n", lines) + "\n").getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(new ByteArrayOutputStream()), data);
        return out.toString(StandardCharsets.UTF_8);
    }

    private void setUpPark() {
        run("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;634567890;1111222233334444",
            "TICKET CREATE;T001;ana@correo.com;" + TODAY,
            "TICKET CREATE;T002;pedro@correo.com;" + LATER,
            "GROUP CREATE;G01;F",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "GROUPTICKET CREATE;TG01;G01;" + LATER,
            "SPEEDPASS CREATE;SP02;T001;10",
            "SPECIALPASS CREATE;PE01;TG01:12345678Z",
            "SPEEDPASS CREATE;SP05;T002;1",
            "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL");
    }

    @Test
    void passesSurviveRestartWithPriceAndTicket() throws IOException {
        setUpPark();
        List<String> lines = Files.readAllLines(data.resolve("passes.txt"));
        assertEquals(12, lines.size());
        assertTrue(lines.contains("SPEED;SP02:1;T001;8.00;false"));
        assertTrue(lines.contains("SPECIAL;PE01;TG01:12345678Z;36.00;false"));
        String out = run("INVOICE CREATE;USER;ana@correo.com", "SPECIALPASS CREATE;PE02;TG01:12345678Z");
        assertTrue(out.contains("SP02:10 (ticket T001): 8.00 EUR"), out);
        assertTrue(out.contains("PE01 (ticket TG01:12345678Z): 36.00 EUR"), out);
        assertTrue(out.contains("ERROR: Ticket already has a Special Pass"), out);
    }

    @Test
    void consumedStateSurvivesRestart() throws IOException {
        setUpPark();
        assertTrue(run("ATTRACTION ENTER;A01;T001;SPEED").contains("consumed SpeedPass SP02:1"));
        assertTrue(Files.readAllLines(data.resolve("passes.txt")).contains("SPEED;SP02:1;T001;8.00;true"));
        String out = run("ATTRACTION ENTER;A01;T001;SPEED", "SPEEDPASS DELETE;SP05:1");
        assertTrue(out.startsWith("ENTERED A01: ana@correo.com (consumed SpeedPass SP02:2)"), out);
        assertTrue(Files.readAllLines(data.resolve("passes.txt")).contains("SPEED;SP02:1;T001;8.00;true"));
    }

    @Test
    void refundsAndCascadesPersist() throws IOException {
        setUpPark();
        run("SPEEDPASS DELETE;SP05:1", "GROUPTICKET DELETE;TG01");
        run();
        String passes = Files.readString(data.resolve("passes.txt"));
        assertFalse(passes.contains("SP05:1"));
        assertFalse(passes.contains("PE01"));
        assertTrue(passes.contains("SP02:1"));
    }

    @Test
    void flyModeLeavesNoPasses() throws IOException {
        setUpPark();
        String before = Files.readString(data.resolve("passes.txt"));
        String out = runWith(new String[] {"--fly"},
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "TICKET CREATE;T009;ana@correo.com;" + LATER, "SPEEDPASS CREATE;SPX;T009;10");
        assertTrue(out.contains("SPEEDPASS CREATED - Total price: 80.00 EUR"), out);
        assertEquals(before, Files.readString(data.resolve("passes.txt")));
        assertFalse(Files.exists(data.resolve("tmp")));
    }
}
