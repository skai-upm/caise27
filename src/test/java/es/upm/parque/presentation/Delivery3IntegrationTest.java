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

import es.upm.parque.business.ParkController;
import es.upm.parque.business.model.AttractionType;

/**
 * Runs a command file mixing Delivery 1, 2 and 3 commands through the real entry point, in normal mode (with a
 * restart) and in temporary --fly mode.
 */
class Delivery3IntegrationTest {

    static final List<String> INPUT_FASE3 = List.of(
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "USER CREATE;Luis;Ruiz;11111111H;luis@correo.com;Clave3$;600000002;1234567890123458",
            "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
            "USER UPDATE;ana@correo.com;Mala1!;PHONE;1",
            "SYSTEM DATE;2027-04-01",
            "SYSTEM PRICE;50",
            "GROUP CREATE;G01;Familia",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "GROUP ADD;G01;luis@correo.com",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "TICKET CREATE;T002;pedro@correo.com;2027-04-15",
            "TICKET CREATE;T003;luis@correo.com;2027-04-20",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "ATTRACTION CREATE;A01;Dragon Rojo;20",
            "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "SPEEDPASS CREATE;SP01;T001;10",
            "SPEEDPASS CREATE;SP02;TG01:12345678Z;1",
            "SPEEDPASS CREATE;SP03;TG01:87654321X;1",
            "SPEEDPASS CREATE;SP04;TG01:11111111H;1",
            "SPECIALPASS CREATE;PE01;T001",
            "SPECIALPASS CREATE;PE02;T001",
            "SPEEDPASS CREATE;SP05;T003;1",
            "SPEEDPASS DELETE;SP01:10",
            "SYSTEM DATE;2027-04-15",
            "ATTRACTION ENTER;A02;TG01",
            "ATTRACTION ENTER;A01;T001;SPEED",
            "ATTRACTION ENTER;A03;TG01;SPEED",
            "ATTRACTION ENTER;A04;T001",
            "ATTRACTION ENTER;A04;T002",
            "SPEEDPASS DELETE;SP01:2",
            "ATTRACTION EXIT;A04;ana@correo.com",
            "ATTRACTION STATUS;A03",
            "INVOICE CREATE;USER;ana@correo.com",
            "INVOICE CREATE;GROUP;G01",
            "TICKET DELETE;T003",
            "USER DELETE;luis@correo.com;Clave3$",
            "HELP",
            "HELP EXTENDED",
            "EXIT",
            "USER CREATE;Nadie;X;22222222J;nadie@correo.com;Clave1!;600;1234567890123459");

    private static String execute(String[] args, Path workDir) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.execute(args, workDir, new ByteArrayInputStream(new byte[0]),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    private static void assertExpectedResponses(String out) {
        assertTrue(out.contains("ERROR: incorrect password"), out);
        assertTrue(out.contains("discount 10%), total 135.00 EUR"), out);
        assertTrue(out.contains("created with capacity 20, type GENERAL"), out);
        assertTrue(out.contains("created with capacity 15, type SPECIAL"), out);
        assertTrue(out.contains("OK: 10 SpeedPasses created for ticket T001 (SP01:1 to SP01:10), unit price 10.00 EUR,"
                + " total 100.00 EUR"), out);
        assertTrue(out.contains("OK: 1 SpeedPass created for ticket TG01:12345678Z (SP02:1), unit price 9.00 EUR"), out);
        assertTrue(out.contains("OK: Special Pass PE01 created for ticket T001, price 50.00 EUR"), out);
        assertTrue(out.contains("ERROR: ticket T001 already has Special Pass PE01"), out);
        assertTrue(out.contains("OK: SpeedPass SP01:10 refunded, 10.00 EUR"), out);
        assertTrue(out.contains("ERROR: attraction A02 (INDIVIDUAL) does not accept group tickets"), out);
        assertTrue(out.contains("ana@correo.com; SpeedPass consumed: SP01:1"), out);
        assertTrue(out.contains("OK: 3 user(s) entered attraction A03 (Rafting Familiar): ana@correo.com,"
                + " pedro@correo.com, luis@correo.com; SpeedPasses consumed: SP02:1, SP03:1, SP04:1"), out);
        assertTrue(out.contains("(Pasaje del Terror): ana@correo.com; Special Pass consumed: PE01"), out);
        assertTrue(out.contains("ERROR: ticket T002 has no Special Pass"), out);
        assertTrue(out.contains("ERROR: SpeedPass SP01:2 belongs to ticket T001"), out);
        assertTrue(out.contains("capacity 24, inside 3, available 21"), out);
        assertTrue(out.contains("Special Pass PE01 for ticket T001 (consumed)  50.00 EUR"), out);
        assertTrue(out.contains("TOTAL: 199.00 EUR"), out);
        assertTrue(out.contains("Group ticket TG01 for 2027-04-15 (3 tickets, discount 10%)  135.00 EUR"), out);
        assertTrue(out.contains("OK: ticket T003 deleted and 1 pass"), out);
        assertTrue(out.contains("ERROR: user luis@correo.com has a ticket for today or a later date"), out);
        for (String syntax : CommandCatalog.syntaxes()) {
            assertTrue(out.contains("  " + syntax + System.lineSeparator()), syntax);
        }
        assertTrue(out.contains("Available commands (extended):"), out);
        assertTrue(out.trim().endsWith("OK: exiting application"), out);
        assertFalse(out.contains("22222222J"), out);
    }

    @Test
    void normalRunPersistsAndRestartRestoresState(@TempDir Path work) throws Exception {
        Path input = work.resolve("input_fase3.txt");
        Files.write(input, INPUT_FASE3, StandardCharsets.UTF_8);
        assertExpectedResponses(execute(new String[] {input.toString()}, work));
        assertFalse(Files.readString(work.resolve("users.txt")).contains("Clave1!"));

        Path restart = work.resolve("restart.txt");
        Files.write(restart, List.of("SYSTEM DATE;2027-04-15", "ATTRACTION STATUS;A01", "ATTRACTION ENTER;A04;T001",
                "ATTRACTION ENTER;A01;T001;SPEED", "ATTRACTION ENTER;A03;T002",
                "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria", "SPECIALPASS CREATE;PE09;T001"));
        String out = execute(new String[] {restart.toString()}, work);
        assertTrue(out.contains("capacity 20, inside 0, available 20"), out);
        assertTrue(out.contains("the Special Pass PE01 of ticket T001 has already been consumed"), out);
        assertTrue(out.contains("SpeedPass consumed: SP01:2"), out);
        assertTrue(out.contains("attraction A03 (GROUP) does not accept individual tickets"), out);
        assertTrue(out.contains("OK: user 12345678Z updated"), out);
        assertTrue(out.contains("ERROR: ticket T001 already has Special Pass PE01 (consumed)"), out);
        ParkController park = ParkController.getInstance();
        assertEquals(AttractionType.SPECIAL, park.getAttractions().findById("A04").getType());
        assertTrue(park.getPasses().findById("SP02:1").isConsumed());
        assertTrue(park.getPasses().findById("SP05:1") == null, "cascaded with T003");
        assertEquals(3, park.getUsers().size());
    }

    @Test
    void flyRunBehavesTheSameAndLeavesNoData(@TempDir Path normal, @TempDir Path work) throws Exception {
        Path normalInput = normal.resolve("input_fase3.txt");
        Files.write(normalInput, INPUT_FASE3, StandardCharsets.UTF_8);
        String normalOut = execute(new String[] {normalInput.toString()}, normal);

        Path input = work.resolve("input_fase3.txt");
        Files.write(input, INPUT_FASE3, StandardCharsets.UTF_8);
        String flyOut = execute(new String[] {"--fly", input.toString()}, work);
        assertExpectedResponses(flyOut);
        assertEquals(normalOut, flyOut);
        for (String file : new String[] {"users.txt", "groups.txt", "tickets.txt", "attractions.txt", "passes.txt"}) {
            assertFalse(Files.exists(work.resolve(file)), file);
        }
        try (Stream<Path> left = Files.list(work.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
