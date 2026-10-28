package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.model.AttractionType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Delivery3IntegrationTest {

    private static final String NL = System.lineSeparator();

    private static String run(Path workDir, String... args) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(args, new ByteArrayInputStream("EXIT\n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), workDir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String fragment) {
        int count = 0;
        for (int i = text.indexOf(fragment); i >= 0; i = text.indexOf(fragment, i + 1)) {
            count++;
        }
        return count;
    }

    @Test
    void allDeliveriesWorkTogether(@TempDir Path workDir) throws Exception {
        Path data = Files.createDirectories(workDir.resolve("data"));
        Files.writeString(data.resolve("users.txt"), "1A;ana@correo.com;;Ana;Garcia;600000001;1111222233334444\n"
                + "2B;pedro@correo.com;;Pedro;Lopez;600000002;1111222233334445\n"
                + "3C;luis@correo.com;;Luis;Perez;600000003;1111222233334446\n");
        Path input = workDir.resolve("input_fase3.txt");
        Files.writeString(input, String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "SYSTEM PRICE;40",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "SPEEDPASS CREATE;SPA;TG01:1A;1",
                "SPEEDPASS CREATE;SPB;TG01:2B;1",
                "SPEEDPASS CREATE;SPC;TG01:3C;10",
                "SPECIALPASS CREATE;PE01;T001",
                "SPECIALPASS CREATE;PE02;T001",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "ATTRACTION ENTER;A02;TG01",
                "ATTRACTION ENTER;A03;TG01;SPEED",
                "ATTRACTION ENTER;A04;T001;SPEED",
                "ATTRACTION ENTER;A04;T001",
                "ATTRACTION EXIT;A03;pedro@correo.com",
                "ATTRACTION STATUS;A03",
                "SPEEDPASS DELETE;SPC:2",
                "GROUP REMOVE;G01;luis@correo.com",
                "INVOICE CREATE;USER;ana@correo.com",
                "INVOICE CREATE;GROUP;G01",
                "HELP EXTENDED",
                "EXIT",
                "TICKET CREATE;T999;ana@correo.com;2027-04-20"));
        String out = run(workDir, input.toString());
        assertTrue(out.contains("GROUPTICKET CREATED. TOTAL PRICE: 108.00"), out);
        assertTrue(out.contains("SPEEDPASS CREATED: 10 pass(es). TOTAL PRICE: 72.00"), out);
        assertTrue(out.contains("SPECIALPASS CREATED. PRICE: 40.00"), out);
        assertTrue(out.contains("ATTRACTION ENTERED: 3 user(s) in A03" + NL + "SPEEDPASS CONSUMED: SPA:1" + NL
                + "SPEEDPASS CONSUMED: SPB:1" + NL + "SPEEDPASS CONSUMED: SPC:1"), out);
        assertTrue(out.contains("ATTRACTION ENTERED: 1 user(s) in A04" + NL + "SPECIALPASS CONSUMED: PE01"), out);
        assertTrue(out.contains("USERS INSIDE: 2" + NL + "AVAILABLE PLACES: 22"), out);
        String userInvoice = out.substring(out.indexOf("INVOICE USER"), out.indexOf("INVOICE GROUP"));
        assertTrue(userInvoice.contains("TOTAL: 87.20"), userInvoice);
        assertTrue(out.substring(out.indexOf("INVOICE GROUP")).contains("TOTAL: 108.00"), out);
        assertTrue(out.contains("Available commands (extended):"), out);
        assertEquals(5, count(out, "ERROR"), out);
        assertFalse(out.contains("T999"));

        run(workDir);
        ParkController park = ParkController.getInstance();
        assertTrue(park.findAttraction("A03").orElseThrow().getOccupants().isEmpty());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").orElseThrow().getType());
        assertTrue(park.findPass("PE01").orElseThrow().isConsumed());
        assertTrue(park.findPass("SPA:1").orElseThrow().isConsumed());
        assertFalse(park.findPass("SPC:2").orElseThrow().isConsumed());
        assertTrue(park.findTicket("T999").isEmpty());
    }

    @Test
    void sameOutputWithAndWithoutFly(@TempDir Path normalDir, @TempDir Path flyDir) throws Exception {
        String commands = String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "SYSTEM PRICE;45.50",
                "GROUP CREATE;G01;Amigos",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "ATTRACTION CREATE;A05;Noria;5;VIP",
                "ATTRACTION STATUS;A04",
                "SPEEDPASS CREATE;SP01;NOPE;1",
                "INVOICE CREATE;GROUP;G01",
                "HELP",
                "EXIT");
        Files.writeString(normalDir.resolve("input_fase3.txt"), commands);
        Files.writeString(flyDir.resolve("input_fase3.txt"), commands);
        String normal = run(normalDir, normalDir.resolve("input_fase3.txt").toString());
        String fly = run(flyDir, "--fly", flyDir.resolve("input_fase3.txt").toString());
        assertEquals(normal, fly);
        assertTrue(Files.exists(normalDir.resolve("data").resolve("attractions.txt")));
        assertFalse(Files.exists(flyDir.resolve("data")));
        try (Stream<Path> left = Files.list(flyDir.resolve("tmp"))) {
            assertEquals(0, left.count());
        }
    }
}
