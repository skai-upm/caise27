package es.upm.amusementpark.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.persistence.InMemoryUserStorage;

class CommandProcessorTest {

    static final String ANA = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private final ParkController park = ParkController.getInstance();
    private CommandProcessor processor;

    @BeforeEach
    void freshPark() throws Exception {
        park.initialize(new InMemoryUserStorage());
        processor = new CommandProcessor(park);
    }

    @Test
    void createsUserFromSemicolonSeparatedCommand() {
        assertEquals("USER CREATED", processor.process(ANA));
        var ana = park.users().findByDni("12345678Z").orElseThrow();
        assertEquals("Ana", ana.getFirstName());
        assertEquals("Garcia Lopez", ana.getSurnames());
        assertEquals("ana@correo.com", ana.getEmail());
        assertEquals("Clave1!", ana.getPassword());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCardNumber());
    }

    @Test
    void unknownCommandIsAnError() {
        assertTrue(processor.process("FOO;bar").startsWith("ERROR: Unknown command"));
    }

    @Test
    void wrongArgumentCountIsAnError() {
        assertTrue(processor.process("USER CREATE;Ana").startsWith("ERROR: Wrong number of arguments"));
        assertTrue(processor.process(ANA + ";extra").startsWith("ERROR: Wrong number of arguments"));
        assertTrue(park.users().findAll().isEmpty());
    }

    @Test
    void rejectedDataIsReportedAndProcessingContinues() {
        assertTrue(processor.process(ANA.replace("12345678Z", "12345678A")).startsWith("ERROR: Invalid DNI"));
        assertEquals("USER CREATED", processor.process(ANA));
        assertTrue(processor.process(ANA).startsWith("ERROR: A user with this DNI"));
        assertTrue(processor.process(ANA.replace("12345678Z", "87654321X")).startsWith("ERROR: A user with this email"));
    }

    @Test
    void blankLinesProduceNoOutput() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }

    @Test
    void userDeleteRemovesUserAndAllowsReuse() {
        processor.process(ANA);
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(park.users().findAll().isEmpty());
        assertEquals("USER CREATED", processor.process(ANA));
    }

    @Test
    void userDeleteErrors() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;nadie@correo.com;Clave1!").startsWith("ERROR: User not found"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Otra1!").startsWith("ERROR: Incorrect password"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR: Wrong number of arguments"));
        assertEquals(1, park.users().findAll().size());
    }

    @Test
    void userUpdateChangesFieldsAndRejectsDni() {
        processor.process(ANA);
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;DNI;87654321X")
                .startsWith("ERROR: Field cannot be modified"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD")
                .startsWith("ERROR: Wrong number of arguments"));
        var ana = park.users().findByDni("12345678Z").orElseThrow();
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("9999888877776666", ana.getCardNumber());
    }

    @Test
    void helpListsEveryCommandSyntax() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(help.contains("SYSTEM PRICE;<Price>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitSetsTheExitFlag() {
        assertTrue(!processor.isExitRequested());
        assertEquals("Goodbye", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void helpAndExitTakeNoArguments() {
        assertTrue(processor.process("HELP;x").startsWith("ERROR: Wrong number of arguments"));
        assertTrue(processor.process("EXIT;x").startsWith("ERROR: Wrong number of arguments"));
        assertTrue(!processor.isExitRequested());
    }

    @Test
    void unexpectedFailuresAreReportedAndProcessingContinues() throws Exception {
        park.initialize(new InMemoryUserStorage() {
            @Override
            public void save(List<User> users) {
                throw new IllegalStateException("boom");
            }
        });
        CommandProcessor failing = new CommandProcessor(park);
        assertTrue(failing.process(ANA).startsWith("ERROR: Unexpected error"));
        assertTrue(failing.process("HELP").contains("USER CREATE"));
    }

    @Test
    void systemDateCanBeChangedRepeatedly() {
        assertEquals("SYSTEM DATE UPDATED", processor.process("SYSTEM DATE;2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertEquals("SYSTEM DATE UPDATED", processor.process("SYSTEM DATE;2027-05-01"));
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
    }

    @Test
    void invalidDatesAreRejected() {
        processor.process("SYSTEM DATE;2027-04-15");
        for (String bad : List.of("15/04/2027", "2027-02-30", "2027-4-15", "")) {
            assertTrue(processor.process("SYSTEM DATE;" + bad).startsWith("ERROR: Invalid date"), bad);
        }
        assertTrue(processor.process("SYSTEM DATE").startsWith("ERROR: Wrong number of arguments"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
    }

    @Test
    void systemPriceChangesTheBasePrice() {
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        assertEquals("SYSTEM PRICE UPDATED", processor.process("SYSTEM PRICE;45.50"));
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
        assertEquals("SYSTEM PRICE UPDATED", processor.process("SYSTEM PRICE;45"));
        assertEquals(new BigDecimal("45.00"), park.getBasePrice());
    }

    @Test
    void invalidPricesAreRejected() {
        for (String bad : List.of("0", "0.00", "-5", "abc", "12.345", "1e3", "12,50", ".5", "5.", "")) {
            assertTrue(processor.process("SYSTEM PRICE;" + bad).startsWith("ERROR: Invalid price"), bad);
        }
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }
}
