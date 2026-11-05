package es.upm.amusementpark.business;

import es.upm.amusementpark.presentation.help.HelpCommands;
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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupAccessAndStatusTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 4, 15);
    private ParkController park;
    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    private static final UserDirectory USERS = new UserDirectory() {
        @Override
        public boolean exists(String email) {
            return Set.of("ana@correo.com", "pedro@correo.com", "luis@correo.com", "eva@correo.com").contains(email);
        }

        @Override
        public String dniOf(String email) {
            return exists(email) ? email.substring(0, email.indexOf('@')).toUpperCase() : null;
        }
    };

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setCurrentDate(TODAY);
        park.setUserDirectory(USERS);
        park.createAttraction("A01", "Dragon Rojo", 20);
        park.createAttraction("A02", "Torre del Cielo", 2);
        park.createGroup("G01", "Familia");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.addUserToGroup("G01", "luis@correo.com");
        park.addUserToGroup("G01", "eva@correo.com");
        park.createGroupTicket("TG01", "G01", TODAY);
        park.createGroupTicket("TG02", "G01", TODAY.plusDays(1));
        park.createIndividualTicket("T001", "ana@correo.com", TODAY);
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        processor = new CommandProcessor(out);
        HelpCommands.registerDefault(processor, out);
        AttractionCommands.register(processor, park, out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void wholeGroupEnters() {
        park.enterAttraction("A01", "TG01");
        assertEquals(Set.of("pedro@correo.com", "luis@correo.com", "eva@correo.com"),
                park.findAttraction("A01").getOccupants());
    }

    @Test
    void insufficientCapacityAdmitsNobody() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A02", "TG01"));
        assertTrue(park.findAttraction("A02").getOccupants().isEmpty());
    }

    @Test
    void groupTicketNotValidTodayRejected() {
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "TG02"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void membersLeaveOneByOne() {
        park.enterAttraction("A01", "TG01");
        park.leaveAttraction("A01", "luis@correo.com");
        assertEquals(Set.of("pedro@correo.com", "eva@correo.com"), park.findAttraction("A01").getOccupants());
        assertEquals(18, park.findAttraction("A01").getAvailablePlaces());
    }

    @Test
    void leavingRejectedWhenNotInsideOrUnknownAttraction() {
        assertThrows(ParkException.class, () -> park.leaveAttraction("A01", "luis@correo.com"));
        assertThrows(ParkException.class, () -> park.leaveAttraction("A99", "luis@correo.com"));
    }

    @Test
    void statusReflectsEntriesAndExits() {
        processor.execute("ATTRACTION ENTER;A01;T001");
        park.leaveAttraction("A01", "ana@correo.com");
        processor.execute("ATTRACTION ENTER;A01;T001");
        processor.execute("ATTRACTION ENTER;A01;TG01");
        processor.execute("ATTRACTION STATUS;A01");
        String first = output();
        assertTrue(first.contains("ATTRACTION A01"));
        assertTrue(first.contains("NAME: Dragon Rojo"));
        assertTrue(first.contains("CAPACITY: 20"));
        assertTrue(first.contains("INSIDE: 4"));
        assertTrue(first.contains("AVAILABLE: 16"));
        assertTrue(first.contains("    ana@correo.com"));
        assertTrue(first.contains("    eva@correo.com"));
        processor.execute("ATTRACTION EXIT;A01;ana@correo.com");
        processor.execute("ATTRACTION STATUS;A01");
        String after = output().substring(first.length());
        assertTrue(after.contains("USER ana@correo.com LEFT ATTRACTION A01"));
        assertTrue(after.contains("INSIDE: 3"));
        assertTrue(after.contains("AVAILABLE: 17"));
        assertFalse(after.contains("    ana@correo.com"));
    }

    @Test
    void statusOfEmptyAndUnknownAttractions() {
        processor.execute("ATTRACTION STATUS;A02");
        processor.execute("ATTRACTION STATUS;A99");
        processor.execute("ATTRACTION EXIT;A01;nobody@correo.com");
        processor.execute("ATTRACTION EXIT;A01");
        assertTrue(output().contains("USERS: (none)"));
        assertTrue(output().contains("ERROR: Attraction A99 does not exist"));
        assertTrue(output().contains("ERROR: User nobody@correo.com is not inside attraction A01"));
        assertTrue(output().contains("ERROR: Invalid syntax"));
    }
}
