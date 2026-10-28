package es.upm.etsisi.amusementpark.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.IndividualTicket;
import es.upm.etsisi.amusementpark.model.User;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ParkStorageTest {

    private final ParkController controller = ParkController.getInstance();

    @TempDir
    Path directory;

    private String reload() {
        new ParkStorage(directory).save(controller);
        controller.reset();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new ParkStorage(directory).load(controller, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @BeforeEach
    void setUp() {
        controller.reset();
        controller.setCurrentDate("2027-04-01");
        controller.addUser(new User("12345678Z", "ana@correo.com"));
        controller.addUser(new User("87654321X", "pedro@correo.com"));
        controller.createGroup("G01", "Familia Garcia");
        controller.addUserToGroup("G01", "ana@correo.com");
        controller.addUserToGroup("G01", "pedro@correo.com");
        controller.setCurrentPrice("45.50");
        controller.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
        controller.createGroupTicket("TG01", "G01", "2027-04-15");
        controller.createAttraction("A01", "Dragon Rojo", "20");
    }

    @Test
    void roundTripKeepsElementsAndRelationships() {
        assertEquals("", reload());
        User ana = controller.findUser("ana@correo.com").orElseThrow();
        assertEquals("12345678Z", ana.getDni());
        assertEquals(2, controller.findGroup("G01").orElseThrow().getMembers().size());
        IndividualTicket t001 = (IndividualTicket) controller.findTicket("T001").orElseThrow();
        assertSame(ana, t001.getUser());
        assertEquals(new BigDecimal("45.50"), t001.getPrice());
        assertEquals(LocalDate.of(2027, 4, 15), t001.getDate());
        GroupTicket tg01 = (GroupTicket) controller.findTicket("TG01").orElseThrow();
        assertSame(controller.findGroup("G01").orElseThrow(), tg01.getGroup());
        assertEquals(2, tg01.getIndividualTickets().size());
        assertEquals("TG01:12345678Z", tg01.getIndividualTickets().get(0).getIdentifier());
        assertSame(ana, tg01.getIndividualTickets().get(0).getUser());
        assertEquals(new BigDecimal("91.00"), tg01.getPrice());
        assertEquals(20, controller.findAttraction("A01").orElseThrow().getCapacity());
        controller.setCurrentDate("2027-04-01");
        assertThrows(IllegalStateException.class, () -> controller.removeUserFromGroup("G01", "ana@correo.com"));
        assertTrue(controller.hasActiveTickets(ana));
    }

    @Test
    void attractionsStartEmptyAndSystemValuesReset() {
        controller.setCurrentDate("2027-04-15");
        controller.enterAttraction("A01", "T001");
        reload();
        assertTrue(controller.findAttraction("A01").orElseThrow().getOccupants().isEmpty());
        assertEquals(new BigDecimal("40.00"), controller.getCurrentPrice());
        assertEquals(LocalDate.now(), controller.getCurrentDate());
    }

    @Test
    void historicalTicketsOfDeletedUserAndGroupSurvive() {
        controller.setCurrentDate("2027-04-16");
        controller.deleteGroup("G01");
        controller.deleteUser("ana@correo.com");
        reload();
        assertTrue(controller.findUser("ana@correo.com").isEmpty());
        IndividualTicket t001 = (IndividualTicket) controller.findTicket("T001").orElseThrow();
        assertEquals("12345678Z", t001.getUser().getDni());
        assertEquals("ana@correo.com", t001.getUser().getEmail());
        GroupTicket tg01 = (GroupTicket) controller.findTicket("TG01").orElseThrow();
        assertNull(tg01.getGroup());
        assertEquals(2, tg01.getIndividualTickets().size());
        assertSame(t001.getUser(), tg01.getIndividualTickets().get(0).getUser());
    }

    @Test
    void malformedLinesAreReportedAndSkipped() throws Exception {
        new ParkStorage(directory).save(controller);
        Files.writeString(directory.resolve("attractions.txt"), "A01;Dragon Rojo;20\nbroken line\nA02;Noria;x\n");
        controller.reset();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new ParkStorage(directory).load(controller, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(2, out.split("ERROR", -1).length - 1, out);
        assertTrue(controller.findAttraction("A01").isPresent());
        assertEquals(2, controller.getUsers().size());
    }

    @Test
    void textFilesAreWritten() throws Exception {
        new ParkStorage(directory).save(controller);
        assertEquals("12345678Z;ana@correo.com", Files.readAllLines(directory.resolve("users.txt")).get(0));
        assertEquals("G01;Familia Garcia;ana@correo.com;pedro@correo.com",
                Files.readAllLines(directory.resolve("groups.txt")).get(0));
        assertEquals("A01;Dragon Rojo;20", Files.readAllLines(directory.resolve("attractions.txt")).get(0));
        assertEquals("I;T001;2027-04-15;45.50;12345678Z;ana@correo.com",
                Files.readAllLines(directory.resolve("tickets.txt")).get(0));
    }
}
