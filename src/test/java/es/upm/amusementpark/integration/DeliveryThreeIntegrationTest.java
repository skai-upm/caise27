package es.upm.amusementpark.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DeliveryThreeIntegrationTest {

    @TempDir
    Path dir;

    static List<String> scenario() {
        String today = LocalDate.now().toString();
        return List.of(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
                "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "TICKET CREATE;T001;ana@correo.com;" + today,
                "GROUPTICKET CREATE;TG01;G01;" + today,
                "SPEEDPASS CREATE;SP01;T001;1",
                "SPEEDPASS CREATE;SP02;TG01:12345678Z;1",
                "SPEEDPASS CREATE;SP03;TG01:87654321X;1",
                "SPECIALPASS CREATE;PE01;T001",
                "ATTRACTION ENTER;A02;T001;SPEED",
                "ATTRACTION ENTER;A03;TG01;SPEED",
                "ATTRACTION EXIT;A02;ana@correo.com",
                "ATTRACTION ENTER;A04;T001",
                "ATTRACTION STATUS;A03",
                "INVOICE CREATE;USER;ana@correo.com",
                "INVOICE CREATE;GROUP;G01",
                "HELP EXTENDED",
                "EXIT");
    }

    private String run(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeliveryOneIntegrationTest.runApp(args, "", out, new ByteArrayOutputStream(), dir.resolve("users.txt"));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sameFileInFlyAndNormalMode() throws IOException {
        Path input = dir.resolve("input_fase3.txt");
        Files.write(input, scenario(), StandardCharsets.UTF_8);
        String fly = run("--fly", input.toString());
        try (Stream<Path> left = Files.list(dir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
        assertFalse(Files.exists(dir.resolve("users.txt")));
        String normal = run(input.toString());
        assertEquals(fly, normal);
        assertFalse(normal.contains("ERROR"), normal);
        assertTrue(normal.contains("SpeedPass consumed: SP02:1, SP03:1"));
        assertTrue(normal.contains("Special Pass consumed: PE01"));
        for (String f : List.of("users.txt", "groups.txt", "tickets.txt", "attractions.txt", "passes.txt")) {
            assertTrue(Files.exists(dir.resolve(f)), f);
        }
    }

    @Test
    void consumedPassesKeptAfterRestart() throws IOException {
        Path input = dir.resolve("input_fase3.txt");
        Files.write(input, scenario(), StandardCharsets.UTF_8);
        run(input.toString());
        ApplicationInitializer.initialize(Repositories.files(dir));
        for (String id : List.of("SP01:1", "SP02:1", "SP03:1", "PE01")) {
            assertTrue(Park.getInstance().passes().findById(id).orElseThrow().isConsumed(), id);
        }
    }

    @Test
    void helpListsDeliveryThreeSyntaxInOrder() {
        CommandProcessor p = ApplicationInitializer.initialize(Repositories.inMemory());
        List<String> help = p.process("HELP").lines().map(String::trim).toList();
        String[] expected = {"ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>", "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED", "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>", "INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP", "HELP EXTENDED", "EXIT"};
        int from = 0;
        for (String syntax : expected) {
            int at = -1;
            for (int i = from; i < help.size(); i++) {
                if (help.get(i).startsWith(syntax) && (help.get(i).length() == syntax.length() || help.get(i).charAt(syntax.length()) == ' ')) {
                    at = i;
                    break;
                }
            }
            assertTrue(at >= 0, syntax + " missing, not on its own line or out of order");
            from = at + 1;
        }
        String ext = p.process("HELP EXTENDED");
        p.catalogue().forEach(c -> assertTrue(ext.contains(c.syntax()) && !c.purpose().isEmpty(), c.syntax()));
    }
}
