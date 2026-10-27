package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.service.ParkController;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.presentation.help.CommandCatalog;
import es.upm.amusementpark.presentation.help.CommandInfo;
import es.upm.amusementpark.presentation.help.HelpGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        ParkController.getInstance().initialize(new UserRepository(), LocalDate.of(2026, 10, 27));
        buffer = new ByteArrayOutputStream();
        processor = new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpListsRequiredCommandsAndContinues() {
        assertTrue(processor.process("HELP"));
        String out = output();
        assertTrue(out.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertTrue(out.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(out.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        assertTrue(out.contains("SYSTEM DATE;<yyyy-MM-dd>"));
        assertTrue(out.contains("SYSTEM PRICE;<Price>"));
        assertTrue(out.contains("GROUP CREATE;<Identifier>;<Name>"));
        assertTrue(out.contains("GROUP ADD;<Identifier>;<Email>"));
        assertTrue(out.contains("GROUP REMOVE;<Identifier>;<Email>"));
        assertTrue(out.contains("GROUP DELETE;<Identifier>"));
        assertTrue(out.contains("TICKET CREATE;<identifier>;<email>;<date>"));
        assertTrue(out.contains("TICKET DELETE;<identifier>"));
        assertTrue(out.contains("GROUPTICKET CREATE;<identifier>;<group>;<date>"));
        assertTrue(out.contains("GROUPTICKET DELETE;<identifier>"));
        assertTrue(out.contains("ATTRACTION CREATE;<identifier>;<name>;<capacity>"));
        assertTrue(out.contains("ATTRACTION ENTER;<attraction>;<ticket>"));
        assertTrue(out.contains("ATTRACTION EXIT;<attraction>;<email>"));
        assertTrue(out.contains("ATTRACTION LIST;<attraction>"));
        assertTrue(out.contains("ATTRACTION STATUS;<attractionIdentifier>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void helpShowsTheCompleteDelivery2Syntax() {
        processor.process("HELP");
        String out = output();
        String[] required = {
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
            "SPEEDPASS DELETE;<speedPassIdentifier>",
            "SPECIALPASS CREATE;<identifier>;<individualTicket>",
            "SPECIALPASS DELETE;<specialPassIdentifier>",
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP",
            "HELP EXTENDED",
            "EXIT"};
        for (String syntax : required) {
            assertTrue(out.contains(syntax), syntax);
        }
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }

    @Test
    void unknownCommandIsRejectedWithoutStopping() {
        assertTrue(processor.process("FOO"));
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void userCreateCreatesUser() {
        assertTrue(processor.process(ANA));
        assertTrue(output().contains("USER CREATED"));
    }

    @Test
    void duplicateUserCreateIsRejected() {
        processor.process(ANA);
        buffer.reset();
        assertTrue(processor.process(ANA));
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void userCreateWithWrongElementCountIsRejected() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z"));
        assertTrue(output().startsWith("ERROR"));
        assertFalse(output().contains("USER CREATED"));
    }

    @Test
    void invalidUserDataIsRejected() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertTrue(output().contains("DNI"));
        assertFalse(output().contains("USER CREATED"));
    }

    @Test
    void malformedCommandDoesNotStopLaterCommands() {
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(processor.process("HELP"));
        assertTrue(output().contains("USER DELETE;<Email>;<Password>"));
    }

    @Test
    void userDeleteWithCorrectPasswordDeletesUser() {
        processor.process(ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(output().contains("USER DELETED"));
        buffer.reset();
        processor.process(ANA);
        assertTrue(output().contains("USER CREATED"));
    }

    @Test
    void userDeleteWithWrongPasswordIsRejected() {
        processor.process(ANA);
        buffer.reset();
        processor.process("USER DELETE;ana@correo.com;Otra1!");
        assertTrue(output().startsWith("ERROR"));
        buffer.reset();
        processor.process(ANA);
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void userDeleteOfUnknownUserIsRejected() {
        processor.process("USER DELETE;nadie@correo.com;Clave1!");
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void userDeleteWithWrongElementCountIsRejected() {
        processor.process("USER DELETE;ana@correo.com");
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void userUpdateChangesField() {
        processor.process(ANA);
        buffer.reset();
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertTrue(output().contains("USER UPDATED"));
    }

    @Test
    void userUpdateOfDniIsRejected() {
        processor.process(ANA);
        buffer.reset();
        processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(output().startsWith("ERROR"));
        buffer.reset();
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        assertTrue(output().contains("USER CREATED"));
    }

    @Test
    void userUpdateOfUnknownFieldIsRejected() {
        processor.process(ANA);
        buffer.reset();
        processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30");
        assertTrue(output().startsWith("ERROR"));
        assertTrue(output().contains("PHONE"));
    }

    @Test
    void userUpdateWithWrongElementCountIsRejected() {
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE");
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void unexpectedErrorIsReportedAndProcessingContinues() {
        UserRepository failing = new UserRepository() {
            @Override
            public void save(User user) {
                throw new IllegalStateException("boom");
            }
        };
        ParkController.getInstance().initialize(failing, LocalDate.of(2026, 10, 27));
        assertTrue(processor.process(ANA));
        assertTrue(output().startsWith("ERROR"));
        assertTrue(processor.process("HELP"));
        assertTrue(output().contains("USER DELETE;<Email>;<Password>"));
    }

    @Test
    void systemDateReplacesCurrentDate() {
        assertTrue(processor.process("SYSTEM DATE;2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), ParkController.getInstance().getCurrentDate());
        processor.process("SYSTEM DATE;2027-05-01");
        assertEquals(LocalDate.of(2027, 5, 1), ParkController.getInstance().getCurrentDate());
        assertFalse(output().contains("ERROR"));
    }

    @Test
    void invalidSystemDatesAreRejected() {
        for (String command : new String[] {"SYSTEM DATE;2027-02-30", "SYSTEM DATE;15-04-2027", "SYSTEM DATE"}) {
            buffer.reset();
            assertTrue(processor.process(command));
            assertTrue(output().startsWith("ERROR"), command);
        }
        assertEquals(LocalDate.of(2026, 10, 27), ParkController.getInstance().getCurrentDate());
    }

    @Test
    void systemPriceReplacesCurrentPrice() {
        assertTrue(processor.process("SYSTEM PRICE;45.50"));
        assertEquals(new BigDecimal("45.50"), ParkController.getInstance().getCurrentPrice());
        assertTrue(output().contains("45.50"));
    }

    @Test
    void invalidSystemPricesAreRejected() {
        for (String command : new String[] {"SYSTEM PRICE;0", "SYSTEM PRICE;-5", "SYSTEM PRICE;abc",
                "SYSTEM PRICE;10.123", "SYSTEM PRICE;1e2", "SYSTEM PRICE;10,5", "SYSTEM PRICE"}) {
            buffer.reset();
            assertTrue(processor.process(command));
            assertTrue(output().startsWith("ERROR"), command);
        }
        assertEquals(new BigDecimal("40.00"), ParkController.getInstance().getCurrentPrice());
    }

    @Test
    void groupCommandsFollowSourceExamples() {
        processor.process(ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        buffer.reset();
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP REMOVE;G01;pedro@correo.com");
        processor.process("GROUP DELETE;G01");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("GROUP CREATED", lines[0]);
        assertEquals("USER ADDED TO GROUP", lines[1]);
        assertEquals("USER ADDED TO GROUP", lines[2]);
        assertTrue(lines[3].startsWith("ERROR"));
        assertEquals("USER REMOVED FROM GROUP", lines[4]);
        assertEquals("GROUP DELETED", lines[5]);
    }

    @Test
    void groupCommandsWithWrongElementCountAreRejected() {
        for (String command : new String[] {"GROUP CREATE;G01", "GROUP ADD;G01", "GROUP REMOVE;G01",
                "GROUP DELETE", "GROUP DELETE;G01;x"}) {
            buffer.reset();
            processor.process(command);
            assertTrue(output().startsWith("ERROR"), command);
        }
    }

    @Test
    void ticketCommandsCreateShowPriceAndDelete() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        buffer.reset();
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("SYSTEM PRICE;45.50");
        processor.process("TICKET CREATE;T002;ana@correo.com;2027-04-15");
        processor.process("TICKET DELETE;T001");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", lines[0]);
        assertEquals("TICKET CREATED. PRICE: 45.50 EUR", lines[2]);
        assertEquals("TICKET DELETED", lines[3]);
    }

    @Test
    void invalidTicketCommandsAreRejected() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        for (String command : new String[] {"TICKET CREATE;T002;ana@correo.com;2027-04-14",
                "TICKET CREATE;T002;nadie@correo.com;2027-04-15", "TICKET CREATE;T002;ana@correo.com;15/04/2027",
                "TICKET CREATE;T002;ana@correo.com", "TICKET CREATE;T001;ana@correo.com;2027-04-16",
                "TICKET DELETE;T001", "TICKET DELETE;T999", "TICKET DELETE",
                "USER DELETE;ana@correo.com;Clave1!"}) {
            buffer.reset();
            processor.process(command);
            assertTrue(output().startsWith("ERROR"), command);
        }
    }

    @Test
    void groupTicketCommandsShowTotalAndDelete() {
        processor.process(ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2027-04-01");
        buffer.reset();
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("TICKET DELETE;TG01:12345678Z");
        processor.process("GROUPTICKET DELETE;TG01");
        processor.process("GROUPTICKET DELETE;TG01");
        processor.process("GROUPTICKET CREATE;TG02;G01");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("GROUP TICKET CREATED. TOTAL PRICE: 80.00 EUR", lines[0]);
        assertTrue(lines[1].startsWith("ERROR"));
        assertTrue(lines[2].startsWith("ERROR"));
        assertEquals("GROUP TICKET DELETED", lines[3]);
        assertTrue(lines[4].startsWith("ERROR"));
        assertTrue(lines[5].startsWith("ERROR"));
    }

    @Test
    void attractionsAreCreatedAndListed() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A02;Torre del Cielo;12");
        processor.process("ATTRACTION LIST;A01");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("ATTRACTION CREATED", lines[0]);
        assertEquals("ATTRACTION CREATED", lines[1]);
        assertEquals("No users inside attraction A01.", lines[2]);
        assertEquals(12, ParkController.getInstance().getAttractions().findById("A02").orElseThrow().getCapacity());
    }

    @Test
    void attractionListShowsOccupantEmailsWithoutChangingThem() {
        processor.process(ANA);
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        Attraction<?> a01 = ParkController.getInstance().getAttractions().findById("A01").orElseThrow();
        a01.enter(List.of(ParkController.getInstance().getUsers().findByEmail("ana@correo.com").orElseThrow()));
        buffer.reset();
        processor.process("ATTRACTION LIST;A01");
        assertEquals("ana@correo.com", output().trim());
        assertEquals(1, a01.getOccupants().size());
    }

    @Test
    void invalidAttractionCommandsAreRejected() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        for (String command : new String[] {"ATTRACTION CREATE;A01;Otra;5", "ATTRACTION CREATE;A03;Noria;0",
                "ATTRACTION CREATE;A03;Noria;-2", "ATTRACTION CREATE;A03;Noria;abc", "ATTRACTION CREATE;A03;Noria",
                "ATTRACTION LIST;A99", "ATTRACTION LIST"}) {
            buffer.reset();
            processor.process(command);
            assertTrue(output().startsWith("ERROR"), command);
        }
        assertTrue(ParkController.getInstance().getAttractions().findById("A03").isEmpty());
    }

    @Test
    void attractionEnterWithIndividualTicket() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        buffer.reset();
        processor.process("ATTRACTION ENTER;A01;T001");
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("ATTRACTION ENTER;A01;T001");
        processor.process("ATTRACTION LIST;A01");
        processor.process("ATTRACTION ENTER;A01");
        String[] lines = output().split(System.lineSeparator());
        assertTrue(lines[0].startsWith("ERROR"));
        assertEquals("ENTERED ATTRACTION A01: ana@correo.com", lines[2]);
        assertEquals("ana@correo.com", lines[3]);
        assertTrue(lines[4].startsWith("ERROR"));
    }

    @Test
    void attractionStatusReflectsMixedEntriesAndExits() {
        processor.process(ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("USER CREATE;Luis;Perez;00000000T;luis@correo.com;Clave3$;600000000;1111222233334444");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("TICKET CREATE;T003;luis@correo.com;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("ATTRACTION ENTER;A01;TG01");
        processor.process("ATTRACTION ENTER;A01;T003");
        buffer.reset();
        processor.process("ATTRACTION STATUS;A01");
        assertEquals(String.join(System.lineSeparator(),
                "ATTRACTION A01 - Dragon Rojo",
                "Capacity: 20",
                "Users inside: 3",
                "Available places: 17",
                "Inside: ana@correo.com, pedro@correo.com, luis@correo.com"), output().trim());
        buffer.reset();
        processor.process("ATTRACTION EXIT;A01;ana@correo.com");
        processor.process("ATTRACTION EXIT;A01;ana@correo.com");
        processor.process("ATTRACTION STATUS;A99");
        processor.process("ATTRACTION EXIT;A01");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("EXITED ATTRACTION A01: ana@correo.com", lines[0]);
        assertTrue(lines[1].startsWith("ERROR"));
        assertTrue(lines[2].startsWith("ERROR"));
        assertTrue(lines[3].startsWith("ERROR"));
    }

    @Test
    void invoiceCommandsPrintInvoices() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("GROUP CREATE;G01;Familia Garcia");
        buffer.reset();
        processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(output().startsWith("INVOICE - USER"));
        assertTrue(output().contains("TOTAL: 40.00 EUR"));
        buffer.reset();
        processor.process("INVOICE CREATE;GROUP;G01");
        assertTrue(output().startsWith("INVOICE - GROUP"));
        assertTrue(output().contains("TOTAL: 0.00 EUR"));
        for (String command : new String[] {"INVOICE CREATE;COMPANY;X", "INVOICE CREATE;USER",
                "INVOICE CREATE;USER;nadie@correo.com", "INVOICE CREATE;GROUP;G99"}) {
            buffer.reset();
            processor.process(command);
            assertTrue(output().startsWith("ERROR"), command);
        }
    }

    @Test
    void helpExtendedShowsPurposeOfEachCommand() {
        assertTrue(processor.process("HELP EXTENDED"));
        assertTrue(output().startsWith("Available commands (extended):"));
        for (CommandInfo command : CommandCatalog.commands()) {
            assertTrue(output().contains(command.description()), command.syntax());
        }
    }

    @Test
    void injectedHelpGeneratorsAreUsed() {
        HelpGenerator fixed = new HelpGenerator(commands -> List.of("custom help"));
        CommandProcessor custom = new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8),
                fixed, fixed);
        custom.process("HELP");
        custom.process("HELP EXTENDED");
        assertEquals("custom help" + System.lineSeparator() + "custom help", output().trim());
    }

    @Test
    void everyCatalogueCommandIsRecognised() {
        for (CommandInfo command : CommandCatalog.commands()) {
            buffer.reset();
            String name = command.syntax().split(";")[0];
            if (!name.equals("EXIT")) {
                processor.process(command.syntax().contains(";") ? name + ";x" : name);
                assertFalse(output().contains("unrecognised command"), name);
            }
        }
    }

    @Test
    void passCommandsBuyAndRefund() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        buffer.reset();
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        processor.process("SPEEDPASS CREATE;SP02;T001;10");
        processor.process("SPECIALPASS CREATE;PE01;T001");
        processor.process("SPEEDPASS DELETE;SP02:3");
        processor.process("SPECIALPASS DELETE;PE01");
        processor.process("SPEEDPASS CREATE;SP03;T001;5");
        processor.process("SPECIALPASS DELETE;SP01:1");
        processor.process("SPEEDPASS CREATE;SP04;T001");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("SPEEDPASS CREATED: SP01:1. TOTAL PRICE: 8.00 EUR", lines[0]);
        assertEquals("SPEEDPASS CREATED: SP02:1..SP02:10. TOTAL PRICE: 80.00 EUR", lines[1]);
        assertEquals("SPECIAL PASS CREATED. PRICE: 40.00 EUR", lines[2]);
        assertEquals("SPEEDPASS REFUNDED", lines[3]);
        assertEquals("SPECIAL PASS REFUNDED", lines[4]);
        assertTrue(lines[5].startsWith("ERROR"));
        assertTrue(lines[6].startsWith("ERROR"));
        assertTrue(lines[7].startsWith("ERROR"));
    }

    @Test
    void attractionCreateAcceptsAnOptionalType() {
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL");
        processor.process("ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP");
        processor.process("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        processor.process("ATTRACTION CREATE;A05;Noria;10;VIP");
        String[] lines = output().split(System.lineSeparator());
        for (int i = 0; i < 4; i++) {
            assertEquals("ATTRACTION CREATED", lines[i]);
        }
        assertTrue(lines[4].startsWith("ERROR"));
        assertEquals(AttractionType.GENERAL,
                ParkController.getInstance().getAttractions().findById("A01").orElseThrow().getType());
        assertEquals(AttractionType.SPECIAL,
                ParkController.getInstance().getAttractions().findById("A04").orElseThrow().getType());
        assertTrue(ParkController.getInstance().getAttractions().findById("A05").isEmpty());
    }

    @Test
    void attractionEnterReportsConsumedPasses() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        processor.process("SPECIALPASS CREATE;PE01;T001");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        processor.process("SYSTEM DATE;2027-04-15");
        buffer.reset();
        processor.process("ATTRACTION ENTER;A01;T001;SPEED");
        processor.process("ATTRACTION ENTER;A04;T001;SPEED");
        processor.process("ATTRACTION ENTER;A04;T001");
        processor.process("ATTRACTION ENTER;A01;T001;FAST");
        String[] lines = output().split(System.lineSeparator());
        assertEquals("ENTERED ATTRACTION A01: ana@correo.com", lines[0]);
        assertEquals("SPEEDPASS CONSUMED: SP01:1", lines[1]);
        assertTrue(lines[2].startsWith("ERROR"));
        assertEquals("ENTERED ATTRACTION A04: ana@correo.com", lines[3]);
        assertEquals("SPECIAL PASS CONSUMED: PE01", lines[4]);
        assertTrue(lines[5].startsWith("ERROR"));
    }

    @Test
    void blankLineIsIgnored() {
        assertTrue(processor.process("   "));
        assertTrue(output().isEmpty());
    }
}
