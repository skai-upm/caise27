package es.upm.amusementpark;

import es.upm.amusementpark.business.exception.ParkException;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.presentation.CommandProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkAttractionAccessTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    private final User ana = new User("ana@correo.com", "12345678Z");
    private final User pedro = new User("pedro@correo.com", "87654321X");

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(ana);
        park.registerUser(pedro);
        park.setCurrentDate("2027-04-15");
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void admittedUsersThroughAbstraction() {
        Ticket individual = park.findTicket("T001");
        assertEquals(List.of(ana), individual.admittedUsers());
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        Ticket group = park.createGroupTicket("TG01", "G01", "2027-04-15");
        assertEquals(List.of(ana, pedro), group.admittedUsers());
    }

    @Test
    void enterWithTodaysTicket() throws IOException {
        String out = run("ATTRACTION ENTER;A01;T001\nATTRACTION LIST;A01\n");
        assertTrue(out.contains("USER ana@correo.com ENTERED ATTRACTION A01"));
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void ticketForAnotherDateRejected() throws IOException {
        park.setCurrentDate("2027-04-14");
        run("ATTRACTION ENTER;A01;T001\n");
        park.setCurrentDate("2027-04-16");
        String out = run("ATTRACTION ENTER;A01;T001\n");
        assertEquals(2, out.split("is valid on 2027-04-15", -1).length - 1);
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void fullAttractionRejected() {
        park.createAttraction("A03", "Mini", "1");
        park.createIndividualTicket("T002", "pedro@correo.com", "2027-04-15");
        park.enterAttraction("A03", "T001");
        ParkException e = assertThrows(ParkException.class,
                () -> park.enterAttraction("A03", "T002"));
        assertTrue(e.getMessage().contains("does not have enough capacity"));
        assertEquals(List.of(ana), park.findAttraction("A03").getOccupants());
    }

    @Test
    void unknownAttractionOrTicketRejected() throws IOException {
        String out = run("ATTRACTION ENTER;A99;T001\nATTRACTION ENTER;A01;T999\nATTRACTION ENTER;A01\n");
        assertTrue(out.contains("Error: attraction not found: A99"));
        assertTrue(out.contains("Error: ticket not found: T999"));
        assertTrue(out.contains("Error: invalid arguments for ATTRACTION ENTER"));
    }

    @Test
    void duplicateEntryRejected() {
        park.enterAttraction("A01", "T001");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "T001"));
        assertEquals(1, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void composedTicketsNotUsableIndividually() {
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        ParkException composed = assertThrows(ParkException.class,
                () -> park.enterAttraction("A01", "TG01:87654321X"));
        assertTrue(composed.getMessage().contains("use the group ticket"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void presenceIndependentOfLaterDateChanges() {
        park.enterAttraction("A01", "T001");
        park.setCurrentDate("2027-04-16");
        assertTrue(park.findAttraction("A01").isInside(ana));
    }

    @Test
    void helpListsAttractionEnter() throws IOException {
        assertTrue(run("HELP\n").contains("ATTRACTION ENTER;<attraction>;<ticket>"));
    }
}
