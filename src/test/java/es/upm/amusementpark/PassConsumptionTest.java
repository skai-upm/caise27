package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Entry;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassConsumptionTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate("2027-04-15");
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        park.registerUser(new User("pedro@correo.com", "87654321X"));
        park.registerUser(new User("luis@correo.com", "11111111H"));
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        park.createIndividualTicket("T002", "luis@correo.com", "2027-04-15");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createAttraction("A01", "Dragon Rojo", "20", "GENERAL");
        park.createAttraction("A03", "Rafting Familiar", "24", "GROUP");
        park.createAttraction("A04", "Pasaje del Terror", "15", "SPECIAL");
        park.createAttraction("A05", "Mini", "1", "GENERAL");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private long consumed() {
        return park.getPasses().stream().filter(Pass::isConsumed).count();
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() throws IOException {
        park.buySpeedPasses("SP02", "T001", "10");
        String out = run("ATTRACTION ENTER;A01;T001;SPEED\n");
        assertTrue(out.contains("USER ana@correo.com ENTERED ATTRACTION A01"));
        assertTrue(out.contains("SPEEDPASS SP02:1 CONSUMED"));
        assertEquals(1, consumed());
    }

    @Test
    void noSpeedPassAvailable() throws IOException {
        String out = run("ATTRACTION ENTER;A01;T001;SPEED\n");
        assertTrue(out.contains("Error: ticket T001 has no unconsumed speed pass"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
        park.buySpeedPasses("SP01", "T001", "1");
        park.enterAttraction("A01", "T001", true);
        park.findAttraction("A01").removeOccupant(park.findUser("ana@correo.com"));
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001", true));
    }

    @Test
    void failedEntryLeavesSpeedPassesUntouched() {
        park.buySpeedPasses("SP01", "T001", "1");
        park.enterAttraction("A05", "T002");
        assertThrows(ParkException.class, () -> park.enterAttraction("A05", "T001", true));
        assertEquals(0, consumed());
    }

    @Test
    void groupSpeedEntryAllOrNothing() {
        park.buySpeedPasses("SPA", "TG01:12345678Z", "1");
        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "TG01", true));
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        assertEquals(0, consumed());

        park.buySpeedPasses("SPB", "TG01:87654321X", "10");
        Entry entry = park.enterAttraction("A03", "TG01", true);
        assertEquals(2, entry.entrants().size());
        assertEquals(2, entry.consumedPasses().size());
        assertEquals(2, consumed());
        assertTrue(park.findPass("SPA:1").isConsumed());
        assertTrue(park.findPass("SPB:1").isConsumed());
        assertFalse(park.findPass("SPB:2").isConsumed());
    }

    @Test
    void groupSpeedEntryRejectedWithoutCapacity() {
        park.buySpeedPasses("SPA", "TG01:12345678Z", "1");
        park.buySpeedPasses("SPB", "TG01:87654321X", "1");
        assertThrows(ParkException.class, () -> park.enterAttraction("A05", "TG01", true));
        assertEquals(0, consumed());
    }

    @Test
    void specialPassConsumedOnlyOnSuccess() throws IOException {
        park.buySpecialPass("PE01", "T001");
        String out = run("ATTRACTION ENTER;A04;T001;SPEED\nATTRACTION ENTER;A04;T001\n"
                + "ATTRACTION EXIT;A04;ana@correo.com\nATTRACTION ENTER;A04;T001\nATTRACTION ENTER;A04;TG01\n");
        assertTrue(out.contains("Error: special attraction A04 does not accept speed passes"));
        assertTrue(out.contains("SPECIALPASS PE01 CONSUMED"));
        assertTrue(out.contains("Error: ticket T001 has no unconsumed special pass"));
        assertTrue(out.contains("of type SPECIAL does not accept ticket TG01"));
        assertTrue(park.findPass("PE01").isConsumed());
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void normalEntryDoesNotConsumeSpeedPasses() {
        park.buySpeedPasses("SP01", "T001", "1");
        park.enterAttraction("A01", "T001");
        assertEquals(0, consumed());
    }

    @Test
    void invalidFourthFieldAndHelp() throws IOException {
        String out = run("ATTRACTION ENTER;A01;T001;FAST\nHELP\n");
        assertTrue(out.contains("Error: invalid arguments for ATTRACTION ENTER"));
        assertTrue(out.contains("ATTRACTION ENTER;<attraction>;<ticket>;SPEED"));
    }
}
