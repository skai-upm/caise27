package es.upm.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Everything added in Delivery 3 is stored and rebuilt: attraction types,
 * passes with prices and consumed flags, and password hashes.
 */
class Delivery3PersistenceTest {

    @TempDir
    Path dir;

    @Test
    void fullRoundTrip() throws IOException {
        CommandProcessor processor = new CommandProcessor(TestParks.open(dir));
        for (String line : new String[] {
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
            "GROUP CREATE;G01;Pareja",
            "GROUP ADD;G01;ana@correo.com",
            "GROUP ADD;G01;pedro@correo.com",
            "ATTRACTION CREATE;A02;Montana Individual;12;INDIVIDUAL",
            "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
            "TICKET CREATE;T001;ana@correo.com;2027-04-15",
            "GROUPTICKET CREATE;TG01;G01;2027-04-15",
            "SPEEDPASS CREATE;SP01;T001;10",
            "SPECIALPASS CREATE;PE01;T001",
            "SPECIALPASS CREATE;PE02;TG01:87654321X",
            "ATTRACTION ENTER;A02;T001;SPEED",
            "ATTRACTION EXIT;A02;ana@correo.com",
            "ATTRACTION ENTER;A04;T001"}) {
            String output = processor.process(line).output();
            assertFalse(output.startsWith("ERROR"), line + " -> " + output);
        }

        Park park = TestParks.open(dir);
        assertEquals(AttractionType.INDIVIDUAL, park.findAttraction("A02").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());

        assertEquals(12, park.passes().getPasses().size());
        Pass first = park.findPass("SP01:1");
        assertTrue(first instanceof SpeedPass && first.isConsumed());
        assertFalse(park.findPass("SP01:2").isConsumed());
        assertTrue(first.getTicket() == park.findTicket("T001"));
        assertEquals(new BigDecimal("8.00"), first.getPrice());
        Pass special = park.findPass("PE01");
        assertTrue(special instanceof SpecialPass && special.isConsumed());
        Pass member = park.findPass("PE02");
        assertEquals(new BigDecimal("40.00"), member.getPrice(), "2 members: no group discount");
        assertTrue(member.getTicket() == park.findTicket("TG01:87654321X"));
        assertFalse(member.isConsumed());

        assertTrue(TestParks.HASHER.matches("Clave1!", park.findUserByEmail("ana@correo.com").getPasswordHash()));
        assertFalse(Files.readString(dir.resolve("users.txt")).contains("Clave1!"));
        assertEquals("ATTRACTION ENTERED (1 user). SpeedPass consumed: SP01:2",
                new CommandProcessor(park).process("ATTRACTION ENTER;A02;T001;SPEED").output());
    }
}
