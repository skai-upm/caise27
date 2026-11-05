package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.ParkController;

/**
 * Runs a complete Phase 1 + Phase 2 command file through the real entry point,
 * in normal mode (with a restart) and in temporary --fly mode.
 */
class Delivery2IntegrationTest {

    static final List<String> INPUT_FASE2 = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave1!;600000002;1234567890123458",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "SYSTEM DATE;2027-04-15",
            "SYSTEM PRICE;45.50",
            "GROUP CREATE;G01;Familia Garcia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "TICKET CREATE;T001;luis@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "GROUPTICKET CREATE;TG02;G01;2027-05-01",
            "GROUP REMOVE;G01;luis@correo.com",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Torre del Cielo;2",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;TG01",
            "ATTRACTION ENTER;A01;T001",
            "ATTRACTION EXIT;A01;pedro@correo.com",
            "ATTRACTION LIST;A01",
            "ATTRACTION STATUS;A01",
            "TICKET DELETE;T001",
            "GROUPTICKET DELETE;TG02",
            "USER DELETE;luis@correo.com;Clave1!",
            "HELP",
            "EXIT",
            "USER CREATE;Nadie;X;22222222J;nadie@correo.com;Clave1!;600;1234567890123459");

    private static String execute(String[] args, Path workDir) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.execute(args, workDir, new ByteArrayInputStream(new byte[0]), new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    private static void assertExpectedResponses(String out) {
        assertTrue(out.contains("discount 10%), total 122.85 EUR"), out);
        assertTrue(out.contains("has room for 2 user(s) but 3 need to enter; nobody entered"), out);
        assertTrue(out.contains("OK: 3 user(s) entered attraction A01"), out);
        assertTrue(out.contains("users of ticket T001 are already inside"), out);
        assertTrue(out.contains("group G01 has an active group ticket and cannot be modified"), out);
        assertTrue(out.contains("capacity 20, inside 2, available 18"), out);
        assertTrue(out.contains("ticket T001 is for today or an earlier date"), out);
        assertTrue(out.contains("OK: group ticket TG02 deleted"), out);
        assertTrue(out.contains("has a ticket for today or a later date and cannot be deleted"), out);
        assertTrue(out.contains("ATTRACTION STATUS;<attractionIdentifier>"), out);
        assertTrue(out.trim().endsWith("OK: exiting application"), out);
        assertFalse(out.contains("22222222J"), out);
    }

    @Test
    void normalRunPersistsAndRestartRestoresState(@TempDir Path work) throws Exception {
        Path input = work.resolve("input_fase2.txt");
        Files.write(input, INPUT_FASE2, StandardCharsets.UTF_8);
        assertExpectedResponses(execute(new String[] {input.toString()}, work));

        Path restart = work.resolve("restart.txt");
        Files.write(restart, List.of("ATTRACTION STATUS;A01", "SYSTEM DATE;2027-04-15", "GROUP ADD;G01;luis@correo.com"));
        String out = execute(new String[] {restart.toString()}, work);
        assertTrue(out.contains("capacity 20, inside 0, available 20"), out);
        assertTrue(out.contains("active group ticket"), out);
        ParkController park = ParkController.getInstance();
        GroupTicket tg01 = (GroupTicket) park.getTickets().findById("TG01");
        assertEquals("122.85", tg01.getPrice().toPlainString());
        assertEquals(3, tg01.getComposition().size());
        assertTrue(park.getTickets().findById("TG02") == null);
        assertEquals(3, park.getUsers().size());
    }

    @Test
    void flyRunBehavesTheSameAndLeavesNoData(@TempDir Path work) throws Exception {
        Path input = work.resolve("input_fase2.txt");
        Files.write(input, INPUT_FASE2, StandardCharsets.UTF_8);
        assertExpectedResponses(execute(new String[] {"--fly", input.toString()}, work));
        for (String file : new String[] {"users.txt", "groups.txt", "tickets.txt", "attractions.txt"}) {
            assertFalse(Files.exists(work.resolve(file)), file);
        }
        try (Stream<Path> left = Files.list(work.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
