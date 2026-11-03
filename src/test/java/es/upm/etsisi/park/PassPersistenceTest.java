package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassPersistenceTest {

    @TempDir
    Path dir;

    @Test
    void passesRestoredWithTicketPriceAndState() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, "TICKET CREATE;T001;ana@correo.com;2027-04-15", "ATTRACTION CREATE;A01;Dragon;5",
                "SPEEDPASS CREATE;SP01;T001;1", "SPECIALPASS CREATE;PE01;T001", "ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals(List.of("S;SP01:1;T001;8.00;true", "P;PE01;T001;40.00;false"),
                Files.readAllLines(dir.resolve("passes.txt")));
        f.restart();
        Pass speed = f.park.findPass("SP01:1");
        Pass special = f.park.findPass("PE01");
        assertTrue(speed instanceof SpeedPass && speed.isConsumed());
        assertTrue(special instanceof SpecialPass && !special.isConsumed());
        assertSame(f.park.findTicket("T001"), speed.getTicket());
        assertEquals("40.00", special.getPrice().toPlainString());
    }

    @Test
    void malformedPassRecordsSkipped() throws IOException {
        ParkFixture f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, "TICKET CREATE;T001;ana@correo.com;2027-04-15");
        Files.writeString(dir.resolve("passes.txt"), String.join("\n",
                "S;SP01:1;T001;8.00;false", "S;SP02:1;T999;8.00;false", "X;SP03:1;T001;8.00;false",
                "S;SP04:1;T001;abc;false", "S;SP05:1;T001;8.00;maybe", "short", ""));
        f.restart();
        assertEquals(1, f.park.getPasses().size());
    }

    @Test
    void flyModeIncludesPassesAndLeavesNothing() throws IOException {
        System.setProperty("park.data.dir", dir.toString());
        try {
            Path input = dir.resolve("input_fase3.txt");
            Files.writeString(input, String.join("\n", ParkFixture.ANA, "SYSTEM DATE;2027-04-15",
                    "TICKET CREATE;T001;ana@correo.com;2027-04-15", "SPEEDPASS CREATE;SP01;T001;10",
                    "ATTRACTION CREATE;A01;Dragon;5;INDIVIDUAL", "ATTRACTION ENTER;A01;T001;SPEED",
                    "INVOICE CREATE;USER;ana@correo.com"));
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            App.run(new String[] {"--fly", input.toString()}, new java.io.ByteArrayInputStream(new byte[0]),
                    new java.io.PrintStream(out, true, java.nio.charset.StandardCharsets.UTF_8));
            String text = out.toString(java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(text.contains("SPEEDPASS CONSUMED: SP01:1"));
            assertTrue(text.contains("TOTAL: 120.00 EUR"));
            assertFalse(Files.exists(dir.resolve("tmp")));
            assertFalse(Files.exists(dir.resolve("passes.txt")));
        } finally {
            System.clearProperty("park.data.dir");
        }
    }
}
