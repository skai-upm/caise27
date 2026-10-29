package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AttractionTest extends ParkTestSupport {

    private void groupTicketToday(String ticketId, int members) {
        park.createGroup("G" + ticketId, "Group");
        for (int i = 1; i <= members; i++) {
            user(i);
            park.addToGroup("G" + ticketId, email(i));
        }
        park.createGroupTicket(ticketId, "G" + ticketId, TODAY.toString());
    }

    @Test
    void createAttraction() {
        park.createAttraction("A01", "Dragon Rojo", "20");
        Attraction a = park.getAttraction("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> park.createAttraction("A01", "Other", "5"));
        assertThrows(IllegalArgumentException.class, () -> park.createAttraction("A02", "X", "0"));
        assertThrows(IllegalArgumentException.class, () -> park.createAttraction("A02", "X", "abc"));
        assertThrows(IllegalArgumentException.class, () -> park.createAttraction("A02", "X", "-1"));
    }

    @Test
    void individualEntry() {
        User u = user(1);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", email(1), TODAY.toString());
        park.enterAttraction("A01", "T001");
        assertEquals(List.of(u), park.getAttraction("A01").getOccupants());
    }

    @Test
    void entryRequiresTicketForToday() {
        user(1);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", email(1), "2027-04-15");
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A01", "T001"));
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A01", "T404"));
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A99", "T001"));
        assertTrue(park.getAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void groupEntryAndExit() {
        groupTicketToday("TG01", 3);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.enterAttraction("A01", "TG01");
        assertEquals(3, park.getAttraction("A01").getOccupants().size());
        park.exitAttraction("A01", email(1));
        assertEquals(2, park.getAttraction("A01").getOccupants().size());
        assertEquals(18, park.getAttraction("A01").getAvailable());
        assertThrows(IllegalArgumentException.class, () -> park.exitAttraction("A01", email(1)));
    }

    @Test
    void groupEntryIsAtomic() {
        groupTicketToday("TG01", 3);
        park.createAttraction("A02", "Torre del Cielo", "2");
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A02", "TG01"));
        assertTrue(park.getAttraction("A02").getOccupants().isEmpty());
    }

    @Test
    void alreadyInsideRejectsWholeEntry() {
        groupTicketToday("TG01", 2);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.enterAttraction("A01", "TG01:" + dni(1));
        assertThrows(IllegalArgumentException.class, () -> park.enterAttraction("A01", "TG01"));
        assertEquals(1, park.getAttraction("A01").getOccupants().size());
    }

    @Test
    void presenceIndependentOfDateChanges() {
        user(1);
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createTicket("T001", email(1), TODAY.toString());
        park.enterAttraction("A01", "T001");
        park.setCurrentDate(LocalDate.of(2027, 5, 1));
        assertEquals(1, park.getAttraction("A01").getOccupants().size());
    }

    @Test
    void listAndStatusCommands() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        CommandProcessor processor = new CommandProcessor(park, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        groupTicketToday("TG01", 3);
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION LIST;A01");
        processor.process("ATTRACTION ENTER;A01;TG01");
        processor.process("ATTRACTION LIST;A01");
        processor.process("ATTRACTION STATUS;A01");
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(out.contains("NO USERS"));
        assertTrue(out.contains("ATTRACTION ENTERED;3"));
        assertTrue(out.contains(email(1) + System.lineSeparator() + email(2) + System.lineSeparator() + email(3)));
        assertTrue(out.contains("ATTRACTION;A01"));
        assertTrue(out.contains("NAME;Dragon Rojo"));
        assertTrue(out.contains("CAPACITY;20"));
        assertTrue(out.contains("INSIDE;3"));
        assertTrue(out.contains("AVAILABLE;17"));
        assertTrue(out.contains("USERS;" + email(1) + "," + email(2) + "," + email(3)));
        assertEquals(3, park.getAttraction("A01").getOccupants().size(), "list/status are read-only");
    }
}
