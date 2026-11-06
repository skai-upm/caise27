package es.upm.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.park.persistence.UserFileStore;
import es.upm.park.persistence.repository.UserRepository;
import es.upm.park.business.controller.ParkController;
import es.upm.park.business.model.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 6);

    private UserRepository repo;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        ParkController.getInstance().start(repo, TODAY);
        processor = new CommandProcessor(ParkController.getInstance());
    }

    @Test
    void createsUserFromCommand() {
        assertEquals("User created: 12345678Z", processor.process(ANA));
        assertEquals("Garcia Lopez", repo.findByDni("12345678Z").orElseThrow().getSurnames());
    }

    @Test
    void commandNameIgnoresSurroundingWhitespace() {
        assertEquals("User created: 12345678Z", processor.process("  " + ANA));
    }

    @Test
    void rejectsWrongFieldCount() {
        assertEquals("Error: USER CREATE expects 7 fields", processor.process("USER CREATE;Ana;Garcia Lopez"));
        assertEquals("Error: USER CREATE expects 7 fields", processor.process(ANA + ";extra"));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void reportsUnknownCommand() {
        assertEquals("Error: unknown command 'FOO BAR'", processor.process("FOO BAR;1"));
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void reportsDomainErrorsAsMessages() {
        processor.process(ANA);
        assertEquals("Error: a user with email ana@correo.com already exists", processor.process(
                "USER CREATE;Luis;Perez;00000000T;ana@correo.com;Clave1!;600000000;1111222233334444"));
        assertEquals("Error: invalid DNI", processor.process(
                "USER CREATE;Luis;Perez;00000000A;luis@correo.com;Clave1!;600000000;1111222233334444"));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void ignoresBlankLines() {
        assertNull(processor.process("   "));
        assertNull(processor.process(null));
    }

    @Test
    void reportsPersistenceFailureAsMessage(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("users.txt");
        UserRepository persistent = new UserRepository(new UserFileStore(file));
        Files.createDirectory(file);
        ParkController.getInstance().start(persistent, TODAY);
        String result = processor.process(ANA);
        assertEquals("Error: cannot write " + file, result);
        assertEquals(0, persistent.findAll().size());
    }

    @Test
    void deletesUserAndAllowsRecreation() {
        processor.process(ANA);
        assertEquals("User deleted: ana@correo.com", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(0, repo.findAll().size());
        assertEquals("User created: 12345678Z", processor.process(ANA));
    }

    @Test
    void rejectsDeleteWithWrongCredentialsOrFields() {
        processor.process(ANA);
        assertEquals("Error: incorrect password", processor.process("USER DELETE;ana@correo.com;Otra2#"));
        assertEquals("Error: no user with email nadie@correo.com",
                processor.process("USER DELETE;nadie@correo.com;Clave1!"));
        assertEquals("Error: USER DELETE expects 2 fields", processor.process("USER DELETE;ana@correo.com"));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void updatesUserFromCommand() {
        processor.process(ANA);
        assertEquals("User updated: 12345678Z NAME",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", repo.findByDni("12345678Z").orElseThrow().getFirstName());
        assertEquals("Error: USER UPDATE expects 4 fields", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertEquals("Error: field DNI cannot be modified",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
    }

    @Test
    void helpListsEveryCommandSyntax() {
        processor.process(ANA);
        String help = processor.process("HELP");
        for (String syntax : new String[] {
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD",
            "HELP", "EXIT"}) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertEquals(1, repo.findAll().size());
        assertFalse(processor.isExitRequested());
    }

    @Test
    void exitRequestsTermination() {
        assertFalse(processor.isExitRequested());
        assertEquals("Bye", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void helpAndExitRejectFields() {
        assertEquals("Error: EXIT expects 0 fields", processor.process("EXIT;now"));
        assertFalse(processor.isExitRequested());
        assertEquals("Error: HELP expects 0 fields", processor.process("HELP;me"));
    }

    @Test
    void unexpectedFailureBecomesErrorMessage() {
        UserRepository failing = new UserRepository() {
            @Override
            public Optional<User> findByEmail(String email) {
                throw new IllegalStateException("boom");
            }
        };
        ParkController.getInstance().start(failing, TODAY);
        assertEquals("Error: command could not be processed", processor.process("USER DELETE;a@b.com;x"));
        assertEquals("System price set to 45.00", processor.process("SYSTEM PRICE;45"));
    }

    @Test
    void systemDateChangesCurrentDate() {
        ParkController park = ParkController.getInstance();
        assertEquals(TODAY, park.getCurrentDate());
        assertEquals("System date set to 2027-04-15", processor.process("SYSTEM DATE;2027-04-15"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
        assertEquals("System date set to 2027-05-01", processor.process("SYSTEM DATE;2027-05-01"));
        assertEquals(LocalDate.of(2027, 5, 1), park.getCurrentDate());
    }

    @Test
    void invalidSystemDateIsRejected() {
        assertEquals("Error: invalid date 2027-02-30", processor.process("SYSTEM DATE;2027-02-30"));
        assertEquals("Error: invalid date 15/04/2027", processor.process("SYSTEM DATE;15/04/2027"));
        assertEquals("Error: SYSTEM DATE expects 1 fields", processor.process("SYSTEM DATE"));
        assertEquals(TODAY, ParkController.getInstance().getCurrentDate());
    }

    @Test
    void systemPriceChangesBasePrice() {
        ParkController park = ParkController.getInstance();
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
        assertEquals("System price set to 45.50", processor.process("SYSTEM PRICE;45.50"));
        assertEquals(new BigDecimal("45.50"), park.getBasePrice());
        assertEquals("System price set to 50.00", processor.process("SYSTEM PRICE;50"));
    }

    @Test
    void invalidSystemPriceIsRejected() {
        for (String bad : new String[] {"0", "0.00", "-5", "abc", "10.999", "", ".5", "5.", "1e3", "+5", "4.5.1"}) {
            assertEquals("Error: invalid price " + bad, processor.process("SYSTEM PRICE;" + bad), bad);
        }
        assertEquals("Error: SYSTEM PRICE expects 1 fields", processor.process("SYSTEM PRICE;1;2"));
        assertEquals(new BigDecimal("40.00"), ParkController.getInstance().getBasePrice());
    }

    @Test
    void groupCommands() {
        processor.process(ANA);
        assertEquals("Group created: G01", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("User ana@correo.com added to group G01", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("Error: user ana@correo.com already in group G01", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("User ana@correo.com removed from group G01", processor.process("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals("Error: GROUP DELETE expects 1 fields", processor.process("GROUP DELETE"));
        assertEquals("Group deleted: G01", processor.process("GROUP DELETE;G01"));
        assertEquals("Error: no group G01", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("HELP").contains("GROUP REMOVE;<id>;<email>"));
    }

    @Test
    void ticketCommandsShowPriceAndRespectDates() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("SYSTEM PRICE;45.50");
        assertEquals("Ticket created: T001 price 45.50", processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        assertEquals("Error: ticket T001 already exists", processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        assertEquals("Error: invalid date 2027-13-01", processor.process("TICKET CREATE;T002;ana@correo.com;2027-13-01"));
        assertEquals("Error: user ana@correo.com has an active ticket", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals("Ticket deleted: T001", processor.process("TICKET DELETE;T001"));
        assertEquals("User deleted: ana@correo.com", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(processor.process("HELP").contains("TICKET CREATE;<id>;<email>;<yyyy-MM-dd>"));
    }

    @Test
    void groupTicketCommandsShowTotal() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-01");
        processor.process("GROUP CREATE;G01;F");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("Group ticket created: TG01 total 40.00", processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        assertEquals("Error: group G01 has an active group ticket", processor.process("GROUP DELETE;G01"));
        assertEquals("Group ticket deleted: TG01", processor.process("GROUPTICKET DELETE;TG01"));
        assertEquals("Group deleted: G01", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("HELP").contains("GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>"));
    }

    @Test
    void attractionCommands() {
        assertEquals("Attraction created: A01", processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("Attraction A01 has no users inside", processor.process("ATTRACTION LIST;A01"));
        assertEquals("Attraction A01 has no users inside", processor.process("ATTRACTION LIST;A01"));
        processor.process(ANA);
        ParkController.getInstance().getAttractionService().find("A01")
                .enter(repo.findByDni("12345678Z").orElseThrow());
        assertEquals("Users in A01:" + System.lineSeparator() + "ana@correo.com", processor.process("ATTRACTION LIST;A01"));
        assertEquals("Error: invalid capacity 0", processor.process("ATTRACTION CREATE;A02;Torre;0"));
        assertEquals("Error: no attraction A99", processor.process("ATTRACTION LIST;A99"));
        assertTrue(processor.process("HELP").contains("ATTRACTION LIST;<id>"));
    }

    @Test
    void attractionEnterCommand() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("Entered A01: ana@correo.com", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals("Users in A01:" + System.lineSeparator() + "ana@correo.com", processor.process("ATTRACTION LIST;A01"));
        assertEquals("Error: no ticket T9", processor.process("ATTRACTION ENTER;A01;T9"));
        assertTrue(processor.process("HELP").contains("ATTRACTION ENTER;<attraction>;<ticket>"));
    }

    @Test
    void attractionExitAndStatusCommands() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("ATTRACTION ENTER;A01;T001");
        assertTrue(processor.process("ATTRACTION STATUS;A01").contains("Inside: 1"));
        assertEquals("Exited A01: ana@correo.com", processor.process("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals("Error: user ana@correo.com is not inside attraction A01",
                processor.process("ATTRACTION EXIT;A01;ana@correo.com"));
        assertTrue(processor.process("HELP").contains("ATTRACTION STATUS;<id>"));
    }

    @Test
    void invoiceCommands() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("GROUP CREATE;G01;F");
        String user = processor.process("INVOICE CREATE;USER;ana@correo.com");
        assertTrue(user.startsWith("INVOICE (USER)"));
        assertTrue(user.endsWith("TOTAL: 40.00"));
        assertTrue(processor.process("INVOICE CREATE;GROUP;G01").endsWith("TOTAL: 0.00"));
        assertEquals("Error: unknown invoice type COMPANY", processor.process("INVOICE CREATE;COMPANY;X"));
        assertEquals("Error: INVOICE CREATE expects 2 fields", processor.process("INVOICE CREATE;USER"));
        assertTrue(processor.process("HELP").contains("INVOICE CREATE;GROUP;<group>"));
    }

    @Test
    void helpExtendedAndInjectedGenerators() {
        assertTrue(processor.process("HELP EXTENDED").contains("      - Registers a new user"));
        assertEquals("Error: HELP EXTENDED expects 0 fields", processor.process("HELP EXTENDED;x"));
        es.upm.park.presentation.help.HelpGenerator fixed =
                new es.upm.park.presentation.help.HelpGenerator(c -> "custom help", java.util.List.of());
        CommandProcessor custom = new CommandProcessor(ParkController.getInstance(), fixed, fixed);
        assertEquals("custom help", custom.process("HELP"));
        assertEquals("custom help", custom.process("HELP EXTENDED"));
    }

    @Test
    void passCommands() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-20");
        assertEquals("SpeedPasses created: SP02:1..SP02:10 total 80.00", processor.process("SPEEDPASS CREATE;SP02;T001;10"));
        assertEquals("SpeedPasses created: SP01:1 total 8.00", processor.process("SPEEDPASS CREATE;SP01;T001;1"));
        assertEquals("Special pass created: PE01 price 40.00", processor.process("SPECIALPASS CREATE;PE01;T001"));
        assertEquals("SpeedPass refunded: SP02:3", processor.process("SPEEDPASS DELETE;SP02:3"));
        assertEquals("Special pass refunded: PE01", processor.process("SPECIALPASS DELETE;PE01"));
        assertEquals("Error: quantity must be 1 or 10", processor.process("SPEEDPASS CREATE;SP03;T001;2"));
        assertEquals("Ticket deleted: T001", processor.process("TICKET DELETE;T001"));
        assertEquals("Error: no speed pass SP01:1", processor.process("SPEEDPASS DELETE;SP01:1"));
        assertTrue(processor.process("HELP").contains("SPECIALPASS CREATE;<id>;<ticket>"));
    }

    @Test
    void attractionCreateWithOptionalType() {
        assertEquals("Attraction created: A01", processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("Attraction created: A04", processor.process("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL"));
        assertEquals("Error: invalid attraction type VIP", processor.process("ATTRACTION CREATE;A05;X;10;VIP"));
        assertEquals("Error: ATTRACTION CREATE expects 3 to 4 fields", processor.process("ATTRACTION CREATE;A06;X"));
        assertEquals(es.upm.park.business.model.AttractionType.GENERAL,
                ParkController.getInstance().getAttractionService().find("A01").getType());
        assertTrue(processor.process("HELP").contains("ATTRACTION CREATE;<id>;<name>;<capacity>;<type>"));
    }

    @Test
    void attractionEnterWithSpeedOption() {
        processor.process(ANA);
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        processor.process("ATTRACTION CREATE;A04;Terror;15;SPECIAL");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        processor.process("SPECIALPASS CREATE;PE01;T001");
        assertEquals("Error: invalid entry option FAST", processor.process("ATTRACTION ENTER;A01;T001;FAST"));
        assertEquals("Entered A01: ana@correo.com; consumed SP01:1", processor.process("ATTRACTION ENTER;A01;T001;SPEED"));
        assertEquals("Entered A04: ana@correo.com; consumed PE01", processor.process("ATTRACTION ENTER;A04;T001"));
        assertEquals("Error: ATTRACTION ENTER expects 2 to 3 fields", processor.process("ATTRACTION ENTER;A01"));
        assertTrue(processor.process("HELP").contains("ATTRACTION ENTER;<attraction>;<ticket>;SPEED"));
    }
}
