package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.TextFileRepositories;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PassesTest {

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park.reset();
        park.setCurrentDate("2027-04-01");
        park.registerUser(new User("ana@correo.com", "12345678Z"));
        for (int i = 1; i <= 6; i++) {
            park.registerUser(new User("u" + i + "@correo.com", "0000000" + i + "X"));
        }
        park.createIndividualTicket("T001", "ana@correo.com", "2027-04-15");
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String fragment) {
        return text.split(java.util.regex.Pattern.quote(fragment), -1).length - 1;
    }

    @Test
    void pricesComeFromTheTicket() {
        IndividualTicket ticket = park.findIndividualTicket("T001");
        park.setBasePrice("50");
        assertEquals(new BigDecimal("8.00"), park.buySpeedPasses("SP01", "T001", "1").get(0).getPrice());
        assertEquals(new BigDecimal("40.00"), park.buySpecialPass("PE01", "T001").getPrice());
        assertEquals(new BigDecimal("9.11"),
                SpeedPass.unitPrice(new IndividualTicket("X", ticket.getUser(), ticket.getDate(), new BigDecimal("45.55"))));
    }

    @Test
    void discountedComponentPrice() {
        park.createGroup("G01", "Seis");
        for (int i = 1; i <= 6; i++) {
            park.addUserToGroup("G01", "u" + i + "@correo.com");
        }
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        SpeedPass pass = park.buySpeedPasses("SP09", "TG01:00000001X", "1").get(0);
        assertEquals(new BigDecimal("6.40"), pass.getPrice());
        SpecialPass special = park.buySpecialPass("PE09", "TG01:00000002X");
        assertEquals(new BigDecimal("32.00"), special.getPrice());
    }

    @Test
    void packageOfTenAndSingleNaming() throws IOException {
        String out = run("SPEEDPASS CREATE;SP02;T001;10\nSPEEDPASS CREATE;SP01;T001;1\n");
        assertTrue(out.contains("SPEEDPASS PURCHASED (10). TOTAL PRICE: 80.00 EUR"));
        assertTrue(out.contains("SPEEDPASS PURCHASED (1). TOTAL PRICE: 8.00 EUR"));
        for (int n = 1; n <= 10; n++) {
            assertNotNull(park.findPass("SP02:" + n));
        }
        assertNotNull(park.findPass("SP01:1"));
        assertSame(park.findIndividualTicket("T001"), park.findPass("SP02:7").getTicket());
    }

    @Test
    void rejectedPurchasesCreateNothing() throws IOException {
        park.createIndividualTicket("T002", "ana@correo.com", "2027-04-01");
        park.setCurrentDate("2027-04-02");
        park.buySpeedPasses("SP03", "T001", "1");
        int before = park.getPasses().size();
        String out = run("SPEEDPASS CREATE;SP02;T001;5\nSPEEDPASS CREATE;SP02;T999;1\n"
                + "SPEEDPASS CREATE;SP02;T002;1\nSPECIALPASS CREATE;PE05;T002\n");
        assertEquals(4, count(out, "Error:"));
        park.restorePass(new SpeedPass("SP04:3", park.findIndividualTicket("T001")));
        assertThrows(ParkException.class, () -> park.buySpeedPasses("SP04", "T001", "10"));
        assertNull(park.findPass("SP04:1"));
        assertEquals(before + 1, park.getPasses().size());
    }

    @Test
    void secondSpecialPassRejectedEvenIfConsumed() throws IOException {
        park.buySpecialPass("PE01", "T001").consume();
        String out = run("SPECIALPASS CREATE;PE02;T001\nSPECIALPASS CREATE;PE01;T001\n");
        assertEquals(2, count(out, "Error:"));
        assertTrue(out.contains("already has special pass PE01"));
        assertNull(park.findPass("PE02"));
    }

    @Test
    void refunds() throws IOException {
        park.buySpeedPasses("SP02", "T001", "10");
        park.buySpecialPass("PE01", "T001");
        String out = run("SPEEDPASS DELETE;SP02:3\nSPECIALPASS DELETE;SP02:4\nSPEEDPASS DELETE;PE01\n"
                + "SPEEDPASS DELETE;SP99:1\nSPECIALPASS DELETE;PE01\n");
        assertTrue(out.contains("SPEEDPASS REFUNDED"));
        assertTrue(out.contains("SPECIALPASS REFUNDED"));
        assertEquals(3, count(out, "Error:"));
        assertNull(park.findPass("SP02:3"));
        assertNotNull(park.findPass("SP02:4"));
        assertEquals(9, park.passesOf(park.findIndividualTicket("T001")).size());
    }

    @Test
    void refundDeniedWhenConsumedOrTicketNotFuture() {
        park.buySpeedPasses("SP02", "T001", "10");
        Pass consumed = park.findPass("SP02:1");
        consumed.consume();
        assertThrows(ParkException.class, () -> park.refundSpeedPass("SP02:1"));
        park.setCurrentDate("2027-04-15");
        assertFalse(park.findPass("SP02:2").isRefundableOn(LocalDate.of(2027, 4, 15)));
        assertThrows(ParkException.class, () -> park.refundSpeedPass("SP02:2"));
        assertNotNull(park.findPass("SP02:1"));
        assertNotNull(park.findPass("SP02:2"));
    }

    @Test
    void ticketDeletionRemovesItsPasses() throws IOException {
        park.createIndividualTicket("T003", "ana@correo.com", "2027-04-15");
        park.buySpeedPasses("SP01", "T001", "1");
        park.buySpeedPasses("SP02", "T001", "10");
        park.buySpecialPass("PE01", "T001");
        park.buySpeedPasses("SP05", "T003", "1");
        run("TICKET DELETE;T001\n");
        assertNull(park.findTicket("T001"));
        assertEquals(1, park.getPasses().size());
        assertNotNull(park.findPass("SP05:1"));
    }

    @Test
    void groupTicketDeletionRemovesComponentPasses() {
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "u1@correo.com");
        park.addUserToGroup("G01", "u2@correo.com");
        park.createGroupTicket("TG01", "G01", "2027-04-15");
        park.buySpeedPasses("SP01", "TG01:00000001X", "1");
        park.buySpecialPass("PE01", "TG01:00000002X");
        park.deleteGroupTicket("TG01");
        assertNull(park.findTicket("TG01"));
        assertNull(park.findIndividualTicket("TG01:00000001X"));
        assertTrue(park.getPasses().isEmpty());
    }

    @Test
    void failedTicketDeletionKeepsPasses() {
        park.buySpeedPasses("SP01", "T001", "1");
        park.setCurrentDate("2027-04-15");
        assertThrows(ParkException.class, () -> park.deleteTicket("T001"));
        assertNotNull(park.findPass("SP01:1"));
    }

    @Test
    void passesSurviveRestart() {
        park.buySpeedPasses("SP02", "T001", "10");
        park.buySpecialPass("PE01", "T001").consume();
        park.configure(TextFileRepositories.create(dir));
        park.persist();
        park.reset();
        park.configure(TextFileRepositories.create(dir));
        assertTrue(park.load().isEmpty());
        assertEquals(11, park.getPasses().size());
        Pass special = park.findPass("PE01");
        assertTrue(special instanceof SpecialPass);
        assertTrue(special.isConsumed());
        assertEquals(new BigDecimal("40.00"), special.getPrice());
        assertSame(park.findTicket("T001"), special.getTicket());
        assertFalse(park.findPass("SP02:10").isConsumed());
    }

    @Test
    void helpListsPassCommands() throws IOException {
        String out = run("HELP\n");
        assertTrue(out.contains("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>"));
        assertTrue(out.contains("SPEEDPASS DELETE;<speedPassIdentifier>"));
        assertTrue(out.contains("SPECIALPASS CREATE;<identifier>;<individualTicket>"));
        assertTrue(out.contains("SPECIALPASS DELETE;<specialPassIdentifier>"));
    }
}
