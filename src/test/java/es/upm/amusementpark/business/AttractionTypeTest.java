package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.persistence.ParkStorage;
import es.upm.amusementpark.presentation.AttractionCommands;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypeTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private static final Map<String, String> USERS = Map.of("ana@correo.com", "12345678Z",
            "pedro@correo.com", "87654321X");
    private ParkController park;

    @TempDir
    Path dir;

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
        park.createAttraction("A02", "Montana Rusa Individual", 12, AttractionType.INDIVIDUAL);
        park.createAttraction("A03", "Rafting Familiar", 24, AttractionType.GROUP);
        park.createAttraction("A04", "Pasaje del Terror", 15, AttractionType.SPECIAL);
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.createIndividualTicket("T002", "pedro@correo.com", TODAY);
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
    }

    @Test
    void acceptanceMatrix() {
        Ticket single = park.findTicket("T001");
        Ticket group = park.findTicket("TG01");
        assertTrue(park.findAttraction("A01").accepts(single));
        assertTrue(park.findAttraction("A01").accepts(group));
        assertTrue(park.findAttraction("A02").accepts(single));
        assertFalse(park.findAttraction("A02").accepts(group));
        assertFalse(park.findAttraction("A03").accepts(single));
        assertTrue(park.findAttraction("A03").accepts(group));
        assertTrue(park.findAttraction("A04").accepts(single));
        assertFalse(park.findAttraction("A04").accepts(group));
    }

    @Test
    void genericBoundsMatchCapabilities() {
        assertEquals(GeneralAccess.class, boundOf(GeneralAttraction.class));
        assertEquals(IndividualAccess.class, boundOf(IndividualAttraction.class));
        assertEquals(GroupAccess.class, boundOf(GroupAttraction.class));
        assertEquals(IndividualAccess.class, boundOf(SpecialAttraction.class));
        assertTrue(IndividualAccess.class.isAssignableFrom(IndividualTicket.class));
        assertTrue(GroupAccess.class.isAssignableFrom(GroupTicket.class));
        assertEquals(IndividualAccess.class, park.findAttraction("A04").getAccessType());
    }

    private static Class<?> boundOf(Class<?> type) {
        return (Class<?>) ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Test
    void entryRespectsKinds() {
        park.enterAttraction("A01", "T001");
        park.enterAttraction("A01", "T002");
        assertEquals(2, park.findAttraction("A01").getOccupants().size());
        ParkException e = assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG01"));
        assertEquals("Attraction A02 (INDIVIDUAL) does not accept group tickets", e.getMessage());
        assertThrows(ParkException.class, () -> park.enterAttraction("A03", "T001"));
        assertTrue(park.findAttraction("A02").getOccupants().isEmpty());
        assertTrue(park.findAttraction("A03").getOccupants().isEmpty());
        park.enterAttraction("A03", "TG01");
        assertEquals(2, park.findAttraction("A03").getOccupants().size());
    }

    @Test
    void specialRequiresUnconsumedSpecialPass() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T001"));
        park.buySpecialPass("PE01", "T001");
        park.enterAttraction("A04", "T001");
        assertTrue(park.findAttraction("A04").contains("ana@correo.com"));
        park.buySpecialPass("PE02", "T002");
        park.markPassConsumed("PE02");
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "T002"));
        assertThrows(ParkException.class, () -> park.enterAttraction("A04", "TG01"));
        assertEquals(1, park.findAttraction("A04").getOccupants().size());
    }

    @Test
    void commandsCreateTypedAttractionsAndShowType() {
        park.reset();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        AttractionCommands.register(processor, park, out);
        processor.execute("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.execute("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        processor.execute("ATTRACTION CREATE;A05;Noria;10;VIP");
        processor.execute("ATTRACTION CREATE;A06;Noria;10;GROUP;x");
        processor.execute("ATTRACTION STATUS;A04");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(AttractionType.GENERAL, park.findAttraction("A01").getType());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertTrue(text.contains("ERROR: Invalid attraction type"));
        assertTrue(text.contains("ERROR: Invalid syntax"));
        assertTrue(text.contains("  TYPE: SPECIAL"));
        assertEquals(null, park.findAttraction("A05"));
    }

    @Test
    void typesPersistAndUntypedLinesAreGeneral() throws IOException {
        ParkStorage storage = ParkStorage.inDirectory(dir);
        park.setRepository(storage);
        park.saveState();
        Files.writeString(dir.resolve("attractions.txt"), "A09;Antigua;5\n", java.nio.file.StandardOpenOption.APPEND);
        park.reset();
        park.setRepository(storage);
        assertEquals(List.of(), park.loadState());
        assertEquals(AttractionType.SPECIAL, park.findAttraction("A04").getType());
        assertEquals(AttractionType.GROUP, park.findAttraction("A03").getType());
        assertEquals(AttractionType.GENERAL, park.findAttraction("A09").getType());
    }
}
