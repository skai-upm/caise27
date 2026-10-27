package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.repository.UserRepository;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private UserRepository users;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        ParkController.getInstance().start(users);
        processor = new CommandProcessor(ParkController.getInstance());
    }

    private static String create(String dni, String email, String password, String card) {
        return "USER CREATE;Ana;Garcia Lopez;" + dni + ";" + email + ";" + password + ";612345678;" + card;
    }

    @Test
    void createsValidUser() {
        assertEquals("USER CREATED", processor.process(ANA));
        assertEquals(1, users.size());
        assertNotNull(users.findByDni("12345678Z"));
        assertEquals("612345678", users.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void rejectsMissingOrEmptyFields() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z").startsWith("ERROR"));
        assertTrue(processor.process(create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456") + ";extra")
                .startsWith("ERROR"));
        assertTrue(processor.process("USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456")
                .startsWith("ERROR"));
        assertEquals(0, users.size());
    }

    @Test
    void rejectsInvalidData() {
        assertEquals("ERROR: Invalid DNI", processor.process(create("12345678A", "ana@correo.com", "Clave1!", "1234567890123456")));
        assertEquals("ERROR: Invalid email", processor.process(create("12345678Z", "ana@correo.es", "Clave1!", "1234567890123456")));
        assertEquals("ERROR: Invalid password", processor.process(create("12345678Z", "ana@correo.com", "Clave12", "1234567890123456")));
        assertEquals("ERROR: Invalid card number", processor.process(create("12345678Z", "ana@correo.com", "Clave1!", "1234 5678 9012 3456")));
        assertEquals(0, users.size());
    }

    @Test
    void rejectsDuplicateDni() {
        processor.process(ANA);
        assertEquals("ERROR: DNI already registered", processor.process(create("12345678Z", "otra@correo.com", "Clave1!", "1234567890123456")));
        assertEquals(1, users.size());
    }

    @Test
    void rejectsDuplicateEmail() {
        processor.process(ANA);
        assertEquals("ERROR: Email already registered", processor.process(create("87654321X", "ana@correo.com", "Clave1!", "1234567890123456")));
        assertEquals(1, users.size());
    }

    @Test
    void unknownCommandsAreReportedWithoutFailing() {
        assertEquals("ERROR: Unknown command", processor.process("FOO;bar"));
        assertEquals("ERROR: Unknown command", processor.process("user create;x"));
        assertEquals("USER CREATED", processor.process(ANA));
    }

    @Test
    void deletesUserWithCorrectPassword() {
        processor.process(ANA);
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(0, users.size());
        assertEquals(null, users.findByDni("12345678Z"));
    }

    @Test
    void deleteRejectsUnknownUserWrongPasswordAndBadFormat() {
        processor.process(ANA);
        assertEquals("ERROR: User not found", processor.process("USER DELETE;nadie@correo.com;Clave1!"));
        assertEquals("ERROR: Incorrect password", processor.process("USER DELETE;ana@correo.com;Otra1!"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;").startsWith("ERROR"));
        assertEquals(1, users.size());
    }

    private static final String PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @Test
    void updatesSimpleFields() {
        processor.process(ANA);
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;SURNAME;Garcia"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        var ana = users.findByDni("12345678Z");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("Garcia", ana.getSurnames());
        assertEquals("699999999", ana.getPhone());
    }

    @Test
    void updateRejectsBadFormatFieldsAndDni() {
        processor.process(ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE").startsWith("ERROR: Invalid format"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR: Field"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR: Field"));
        assertEquals("12345678Z", users.findByEmail("ana@correo.com").getDni());
    }

    @Test
    void updateRequiresIdentification() {
        processor.process(ANA);
        assertEquals("ERROR: User not found", processor.process("USER UPDATE;nadie@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("ERROR: Incorrect password", processor.process("USER UPDATE;ana@correo.com;Mala1!;PHONE;699999999"));
        assertEquals("612345678", users.findByDni("12345678Z").getPhone());
    }

    @Test
    void updateValidatesEmail() {
        processor.process(ANA);
        processor.process(PEDRO);
        assertEquals("ERROR: Email already registered", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com"));
        assertEquals("ERROR: Invalid email", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.com"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("12345678Z", users.findByEmail("ana.nueva@correo.com").getDni());
        assertEquals(null, users.findByEmail("ana@correo.com"));
    }

    @Test
    void updateValidatesPasswordAndCard() {
        processor.process(ANA);
        assertEquals("ERROR: Invalid password", processor.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;nueva2#"));
        assertEquals("Clave1!", users.findByDni("12345678Z").getPassword());
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertEquals("ERROR: Invalid card number", processor.process("USER UPDATE;ana@correo.com;Nueva2#;CARD;1234"));
        assertEquals("USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Nueva2#;CARD;9999888877776666"));
        assertEquals("9999888877776666", users.findByDni("12345678Z").getCardNumber());
    }

    @Test
    void blankLinesProduceNoOutput() {
        assertEquals(null, processor.process(""));
        assertEquals(null, processor.process("   \t "));
    }

    @Test
    void helpListsAvailableCommands() {
        String help = processor.process("HELP");
        for (String expected : new String[] {
                "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
                "USER DELETE;<email>;<password>",
                "USER UPDATE;<email>;<password>;<field>;<value>",
                "\nHELP", "\nEXIT"}) {
            assertTrue(help.contains(expected), expected);
        }
    }

    @Test
    void exitSetsFlag() {
        assertEquals(false, processor.isExitRequested());
        assertEquals(null, processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void unexpectedFailuresAreContained() {
        ParkController.getInstance().start(new UserRepository() {
            @Override
            public es.upm.amusementpark.model.User findByEmail(String email) {
                throw new IllegalStateException("boom");
            }
        });
        CommandProcessor failing = new CommandProcessor(ParkController.getInstance());
        assertEquals("ERROR: Command could not be processed", failing.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(failing.process("HELP").startsWith("Available commands"));
    }

    @Test
    void systemDateChangesCurrentDate() {
        assertEquals("SYSTEM DATE UPDATED", processor.process("SYSTEM DATE;2027-04-15"));
        assertEquals(java.time.LocalDate.of(2027, 4, 15), ParkController.getInstance().getCurrentDate());
        assertEquals("SYSTEM DATE UPDATED", processor.process("SYSTEM DATE;2028-01-01"));
        assertEquals(java.time.LocalDate.of(2028, 1, 1), ParkController.getInstance().getCurrentDate());
    }

    @Test
    void invalidSystemDateIsRejected() {
        processor.process("SYSTEM DATE;2027-04-15");
        for (String bad : new String[] {"SYSTEM DATE;2027-02-30", "SYSTEM DATE;15/04/2027", "SYSTEM DATE",
                "SYSTEM DATE;2027-4-15", "SYSTEM DATE;+2027-04-15"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(java.time.LocalDate.of(2027, 4, 15), ParkController.getInstance().getCurrentDate());
    }

    @Test
    void systemPriceChangesBasePrice() {
        assertEquals(new java.math.BigDecimal("40.00"), ParkController.getInstance().getCurrentPrice());
        assertEquals("SYSTEM PRICE UPDATED", processor.process("SYSTEM PRICE;45.50"));
        assertEquals(new java.math.BigDecimal("45.50"), ParkController.getInstance().getCurrentPrice());
        assertEquals("SYSTEM PRICE UPDATED", processor.process("SYSTEM PRICE;50"));
        assertEquals(new java.math.BigDecimal("50.00"), ParkController.getInstance().getCurrentPrice());
    }

    @Test
    void invalidSystemPriceIsRejected() {
        for (String bad : new String[] {"SYSTEM PRICE;0", "SYSTEM PRICE;0.00", "SYSTEM PRICE;-5", "SYSTEM PRICE;abc",
                "SYSTEM PRICE;45.505", "SYSTEM PRICE;1e3", "SYSTEM PRICE;45,50", "SYSTEM PRICE;45.", "SYSTEM PRICE"}) {
            assertTrue(processor.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(new java.math.BigDecimal("40.00"), ParkController.getInstance().getCurrentPrice());
    }

    @Test
    void helpListsSystemCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(help.contains("SYSTEM PRICE;<Price>"));
    }
}
