package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.AttractionCommands;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassConsumptionTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X", "luis@correo.com", "11111111H");
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(new UserDirectory() {
            @Override
            public boolean exists(String email) {
                return USERS.containsKey(email);
            }

            @Override
            public String dniOf(String email) {
                return USERS.get(email);
            }
        });
        park.createAttraction("A01", "Dragon Rojo", 20, AttractionType.GENERAL);
        park.createAttraction("A02", "Individual", 1, AttractionType.INDIVIDUAL);
        park.createAttraction("A03", "Rafting", 24, AttractionType.GROUP);
        park.createAttraction("A04", "Terror", 15, AttractionType.SPECIAL);
        park.createAttraction("A05", "Terror pequeno", 1, AttractionType.SPECIAL);
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.createIndividualTicket("T002", "pedro@correo.com", TODAY);
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
    }

    private long consumed() {
        return park.getPasses().stream().filter(Pass::isConsumed).count();
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        park.buySpeedPasses("SP01", "T001", 1);
        park.buySpeedPasses("SP02", "T001", 1);
        Attraction.Admission admission = park.enterAttraction("A01", "T001", true);
        assertEquals(1, admission.toConsume().size());
        assertTrue(admission.toConsume().get(0).isConsumed());
        assertEquals(1, consumed());
        assertTrue(park.findAttraction("A01").contains("ana@correo.com"));
    }

    @Test
    void speedEntryWithoutAvailablePassRejected() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T002", true));
        park.buySpeedPasses("SP03", "T002", 1);
        park.markPassConsumed("SP03:1");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T002", true));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void failedSpeedEntryConsumesNothing() {
        park.buySpeedPasses("SP01", "T001", 1);
        park.buySpeedPasses("SP02", "T002", 1);
        park.enterAttraction("A02", "T002", true);
        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "T001", true));
        assertFalse(park.findPass("SP01:1").isConsumed());
    }

    @Test
    void normalEntryNeverConsumesSpeedPasses() {
        park.buySpeedPasses("SP01", "T001", 1);
        park.enterAttraction("A01", "T001");
        assertEquals(0, consumed());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        park.buySpeedPasses("SP05", "TG01:12345678Z", 1);
        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "TG01", true));
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        assertEquals(0, consumed());
        park.buySpeedPasses("SP06", "TG01:87654321X", 10);
        Attraction.Admission admission = park.enterAttraction("A03", "TG01", true);
        assertEquals(2, admission.holders().size());
        assertEquals(2, consumed());
        assertTrue(park.findPass("SP05:1").isConsumed());
        assertEquals(1, park.getPasses().stream().filter(p -> p.getId().startsWith("SP06") && p.isConsumed()).count());
    }

    @Test
    void specialPassConsumedOnlyOnSuccess() {
        park.buySpecialPass("PE01", "T001");
        park.buySpecialPass("PE02", "T002");
        park.enterAttraction("A05", "T001");
        assertTrue(park.findPass("PE01").isConsumed());
        assertThrows(ParkException.class, () -> park.enterAttraction("A05", "T002"));
        assertFalse(park.findPass("PE02").isConsumed());
        park.leaveAttraction("A05", "ana@correo.com");
        assertThrows(ParkException.class, () -> park.enterAttraction("A05", "T001"));
    }

    @Test
    void speedNotAllowedAtSpecialAttractions() {
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP01", "T001", 1);
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001", true));
        assertEquals(0, consumed());
        assertTrue(park.findAttraction("A04").getOccupants().isEmpty());
    }

    @Test
    void commandOutputNamesConsumedPasses() {
        park.buySpeedPasses("SP01", "T001", 1);
        park.buySpecialPass("PE02", "T002");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        AttractionCommands.register(processor, park, out);
        processor.execute("ATTRACTION ENTER;A01;T001;SPEED");
        processor.execute("ATTRACTION ENTER;A04;T002");
        processor.execute("ATTRACTION ENTER;A01;T002;FAST");
        processor.execute("ATTRACTION ENTER;A04;T002;SPEED");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("ENTERED ATTRACTION A01: ana@correo.com"));
        assertTrue(text.contains("CONSUMED PASS: SpeedPass SP01:1"));
        assertTrue(text.contains("CONSUMED PASS: Special Pass PE02"));
        assertTrue(text.contains("ERROR: Invalid syntax. Usage: ATTRACTION ENTER"));
        assertTrue(text.contains("ERROR: Attraction A04 (SPECIAL) does not accept SpeedPasses")
                || text.contains("ERROR: User pedro@correo.com is already inside"));
    }
}
