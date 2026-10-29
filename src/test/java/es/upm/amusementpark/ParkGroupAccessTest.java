package es.upm.amusementpark;

import es.upm.amusementpark.business.exception.ParkException;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Attraction;
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

class ParkGroupAccessTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    private final User ana = new User("ana@correo.com", "12345678Z");
    private final User pedro = new User("pedro@correo.com", "87654321X");
    private final User luis = new User("luis@correo.com", "11111111H");

    @BeforeEach
    void setUp() {
        park.reset();
        park.registerUser(ana);
        park.registerUser(pedro);
        park.registerUser(luis);
        park.setCurrentDate("2027-04-15");
        park.createAttraction("A01", "Dragon Rojo", "20");
        park.createGroup("G01", "Familia Garcia");
        park.addUserToGroup("G01", "ana@correo.com");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.createIndividualTicket("T001", "luis@correo.com", "2027-04-15");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void groupEntersTogether() throws IOException {
        String out = run("ATTRACTION ENTER;A01;TG01\n");
        assertTrue(out.contains("USER ana@correo.com ENTERED ATTRACTION A01"));
        assertTrue(out.contains("USER pedro@correo.com ENTERED ATTRACTION A01"));
        assertEquals(List.of(ana, pedro), park.findAttraction("A01").getOccupants());
    }

    @Test
    void insufficientCapacityNobodyEnters() {
        park.createAttraction("A03", "Mini", "2");
        park.enterAttraction("A03", "T001");
        ParkException e = assertThrows(ParkException.class,
                () -> park.enterAttraction("A03", "TG01"));
        assertTrue(e.getMessage().contains("does not have enough capacity"));
        assertEquals(List.of(luis), park.findAttraction("A03").getOccupants());
    }

    @Test
    void memberAlreadyInsideNobodyEnters() {
        park.createIndividualTicket("T002", "ana@correo.com", "2027-04-15");
        park.enterAttraction("A01", "T002");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "TG01"));
        assertEquals(List.of(ana), park.findAttraction("A01").getOccupants());
    }

    @Test
    void groupTicketForAnotherDateRejected() {
        park.setCurrentDate("2027-04-16");
        assertThrows(ParkException.class, () -> park.enterAttraction("A01", "TG01"));
        assertTrue(park.findAttraction("A01").getOccupants().isEmpty());
    }

    @Test
    void groupMembersLeaveIndividually() throws IOException {
        park.enterAttraction("A01", "TG01");
        String out = run("ATTRACTION EXIT;A01;ana@correo.com\n");
        assertTrue(out.contains("USER ana@correo.com LEFT ATTRACTION A01"));
        Attraction<?> attraction = park.findAttraction("A01");
        assertEquals(List.of(pedro), attraction.getOccupants());
        assertEquals(19, attraction.getAvailablePlaces());
    }

    @Test
    void exitRejections() throws IOException {
        park.enterAttraction("A01", "TG01");
        String out = run("ATTRACTION EXIT;A01;luis@correo.com\nATTRACTION EXIT;A99;ana@correo.com\nATTRACTION EXIT;A01\n");
        assertTrue(out.contains("Error: user luis@correo.com is not inside attraction A01"));
        assertTrue(out.contains("Error: attraction not found: A99"));
        assertTrue(out.contains("Error: invalid arguments for ATTRACTION EXIT"));
        assertEquals(2, park.findAttraction("A01").getOccupants().size());
    }

    @Test
    void statusAfterMixedEntriesAndExit() throws IOException {
        String out = run("ATTRACTION ENTER;A01;TG01\n"
                + "ATTRACTION ENTER;A01;T001\n"
                + "ATTRACTION EXIT;A01;ana@correo.com\n"
                + "ATTRACTION STATUS;A01\n");
        assertTrue(out.contains("ATTRACTION: A01"));
        assertTrue(out.contains("NAME: Dragon Rojo"));
        assertTrue(out.contains("CAPACITY: 20"));
        assertTrue(out.contains("USERS INSIDE: 2"));
        assertTrue(out.contains("AVAILABLE PLACES: 18"));
        assertTrue(out.contains("OCCUPANTS: pedro@correo.com, luis@correo.com"));
    }

    @Test
    void statusOfEmptyAndUnknownAttraction() throws IOException {
        String out = run("ATTRACTION STATUS;A01\nATTRACTION STATUS;A99\n");
        assertTrue(out.contains("USERS INSIDE: 0"));
        assertTrue(out.contains("AVAILABLE PLACES: 20"));
        assertTrue(out.contains("OCCUPANTS: none"));
        assertTrue(out.contains("Error: attraction not found: A99"));
    }

    @Test
    void helpListsExitAndStatus() throws IOException {
        String out = run("HELP\n");
        assertTrue(out.contains("ATTRACTION EXIT;<attraction>;<email>"));
        assertTrue(out.contains("ATTRACTION STATUS;<attractionIdentifier>"));
    }
}
