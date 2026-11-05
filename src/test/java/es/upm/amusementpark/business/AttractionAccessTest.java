package es.upm.amusementpark.business;

import es.upm.amusementpark.presentation.help.HelpCommands;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.AttractionCommands;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionAccessTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(email -> Set.of("ana@correo.com", "pedro@correo.com", "luis@correo.com").contains(email));
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.createAttraction("A02", "Torre del Cielo", 1);
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        park.createIndividualTicket("T002", "pedro@correo.com", TODAY.plusDays(1));
        park.createIndividualTicket("T003", "pedro@correo.com", TODAY);
        park.createIndividualTicket("T004", "ana@correo.com", TODAY);
    }

    @Test
    void validTicketAdmitsUser() {
        assertEquals(List.of("ana@correo.com"), park.enterAttraction("A01", "T001"));
        assertTrue(park.findAttraction("A01").contains("ana@correo.com"));
    }

    @Test
    void ticketNotValidTodayRejected() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T002"));
        park.setCurrentDate(TODAY.plusDays(2));
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void fullAttractionRejected() {
        park.enterAttraction("A02", "T001");
        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "T003"));
        assertEquals(Set.of("ana@correo.com"), park.findAttraction("A02").getOccupants());
    }

    @Test
    void unknownTicketOrAttractionRejected() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A99", "T001"));
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T999"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void userAlreadyInsideRejected() {
        park.enterAttraction("A01", "T001");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T004"));
        assertEquals(1, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void presenceSurvivesDateChange() {
        park.enterAttraction("A01", "T001");
        park.setCurrentDate(TODAY.plusDays(5));
        assertTrue(park.findAttraction("A01").contains("ana@correo.com"));
    }

    @Test
    void holdersUseCommonAbstraction() {
        Ticket ticket = park.findTicket("T001");
        assertEquals(List.of("ana@correo.com"), ticket.holders());
    }

    @Test
    void enterCommandMessages() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        HelpCommands.registerDefault(processor, out);
        AttractionCommands.register(processor, park, out);
        processor.execute("ATTRACTION ENTER;A01;T001");
        processor.execute("ATTRACTION ENTER;A01;T002");
        processor.execute("ATTRACTION ENTER;A01");
        processor.execute("HELP");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("ENTERED ATTRACTION A01: ana@correo.com"));
        assertTrue(text.contains("ERROR: Ticket T002 is not valid on 2027-04-15"));
        assertTrue(text.contains("ERROR: Invalid syntax"));
        assertTrue(text.contains("ATTRACTION ENTER;<attraction>;<ticket>"));
    }
}
