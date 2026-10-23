package es.upm.etsisi.park.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.etsisi.park.ApplicationContext;
import es.upm.etsisi.park.business.controller.UserController;
import es.upm.etsisi.park.support.TestContext;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        TestContext.initialize(dir);
        processor = ApplicationContext.commandProcessor(out);
    }

    private static UserController users() {
        return UserController.getInstance();
    }

    private String output() {
        String text = buffer.toString(StandardCharsets.UTF_8);
        buffer.reset();
        return text;
    }

    @Test
    void createsUser() {
        assertTrue(processor.process(CREATE_ANA));
        assertEquals("USER CREATED", output().trim());
        assertNotNull(users().findByEmail("ana@correo.com"));
    }

    @Test
    void keywordIsCaseAndSpaceInsensitive() {
        processor.process(" user  create ;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertEquals("USER CREATED", output().trim());
    }

    @Test
    void updatesAndDeletes() {
        processor.process(CREATE_ANA);
        output();
        processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999");
        assertEquals("USER UPDATED", output().trim());
        assertEquals("699999999", users().findByEmail("ana@correo.com").getPhone());
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        assertEquals("USER DELETED", output().trim());
        assertNull(users().findByEmail("ana@correo.com"));
    }

    @Test
    void reportsValidationErrors() {
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(output().startsWith("ERROR: Invalid DNI"));
        assertTrue(users().getUsers().isEmpty());
    }

    @Test
    void wrongArgumentCountShowsUsage() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia"));
        String text = output();
        assertTrue(text.startsWith("ERROR: Wrong number of arguments"));
        assertTrue(text.contains("USER CREATE;<firstName>"));
        assertTrue(processor.process("USER DELETE;ana@correo.com"));
        assertTrue(output().startsWith("ERROR"));
        assertTrue(processor.process("HELP;extra"));
        assertTrue(output().startsWith("ERROR"));
    }

    @Test
    void dniCannotBeUpdated() {
        processor.process(CREATE_ANA);
        output();
        processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        String text = output();
        assertTrue(text.startsWith("ERROR: Field 'DNI' cannot be modified"));
        assertTrue(text.contains("NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"));
        assertEquals("12345678Z", users().findByEmail("ana@correo.com").getDni());
    }

    @Test
    void unknownCommandKeepsRunning() {
        assertTrue(processor.process("USER LIST"));
        String text = output();
        assertTrue(text.startsWith("ERROR: Unknown command 'USER LIST'"));
        assertTrue(text.contains("HELP"));
    }

    @Test
    void blankLineIsIgnored() {
        assertTrue(processor.process("   "));
        assertEquals("", output());
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("help"));
        String text = output();
        for (String syntax : new String[] {
                "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
                "USER DELETE;<email>;<password>", "USER UPDATE;<email>;<password>;<field>;<value>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<price>", "GROUP CREATE;<identifier>;<name>",
                "GROUP ADD;<identifier>;<email>", "GROUP REMOVE;<identifier>;<email>", "GROUP DELETE;<identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attraction>", "HELP", "EXIT"}) {
            assertTrue(text.contains(syntax), syntax);
        }
    }

    @Test
    void parkCommandsProduceConfirmations() {
        String[] commands = {
                "SYSTEM DATE;2027-04-15", "SYSTEM PRICE;40.00", CREATE_ANA,
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "USER CREATE;Laura;Martin Ruiz;11111111H;laura@correo.com;Laura3$;634567890;1111222233334444",
                "GROUP CREATE;G01;Familia Garcia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;laura@correo.com", "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "ATTRACTION ENTER;A01;TG01", "ATTRACTION LIST;A01",
                "ATTRACTION STATUS;A01", "ATTRACTION EXIT;A01;ana@correo.com", "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "SYSTEM PRICE;45.50", "TICKET CREATE;T002;ana@correo.com;2027-04-20", "TICKET DELETE;T002",
                "GROUP REMOVE;G01;laura@correo.com"};
        for (String command : commands) {
            assertTrue(processor.process(command));
        }
        String text = output();
        assertTrue(text.contains("SYSTEM DATE SET TO 2027-04-15"));
        assertTrue(text.contains("SYSTEM PRICE SET TO 40.00 EUR"));
        assertTrue(text.contains("GROUP CREATED"));
        assertTrue(text.contains("USER ADDED TO GROUP"));
        assertTrue(text.contains("ATTRACTION CREATED"));
        assertTrue(text.contains("GROUP TICKET CREATED: TG01 for 2027-04-15, 3 member(s), 10% discount, total 108.00 EUR"));
        assertTrue(text.contains("ATTRACTION ENTERED: 3 user(s) entered A01"));
        assertTrue(text.contains("ana@correo.com\npedro@correo.com\nlaura@correo.com"));
        assertTrue(text.contains("Attraction: A01\nName: Dragon Rojo\nType: GENERAL\nCapacity: 20\nInside: 3\nAvailable: 17\n"
                + "Users: ana@correo.com, pedro@correo.com, laura@correo.com"));
        assertTrue(text.contains("ATTRACTION EXITED: ana@correo.com left A01"));
        assertTrue(text.contains("TICKET CREATED: T001 for 2027-04-15, price 40.00 EUR"));
        assertTrue(text.contains("TICKET CREATED: T002 for 2027-04-20, price 45.50 EUR"));
        assertTrue(text.contains("TICKET DELETED"));
        assertTrue(text.contains("ERROR: Group G01 is locked by an active group ticket"));
    }

    @Test
    void emptyAttractionListing() {
        processor.process("ATTRACTION CREATE;A02;Torre del Cielo;12");
        output();
        processor.process("ATTRACTION LIST;A02");
        assertEquals("ATTRACTION A02 IS EMPTY", output().trim());
        processor.process("ATTRACTION STATUS;A02");
        assertTrue(output().contains("Users: none"));
    }

    @Test
    void parkCommandErrorsKeepRunning() {
        for (String command : new String[] {"SYSTEM DATE", "SYSTEM PRICE;abc", "GROUP CREATE;G01",
                "TICKET CREATE;T1;x@correo.com", "ATTRACTION ENTER;A01", "ATTRACTION LIST;A99",
                "GROUPTICKET DELETE;TG99", "GROUP DELETE;G99"}) {
            assertTrue(processor.process(command), command);
            assertTrue(output().startsWith("ERROR: "), command);
        }
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }

    @Test
    void delivery3CommandsProduceConfirmations() {
        String[] commands = {
                "SYSTEM DATE;2027-04-15", CREATE_ANA,
                "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "ATTRACTION CREATE;A02;Montana Individual;12;INDIVIDUAL", "ATTRACTION CREATE;A03;Rafting;12;group",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL", "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "SPEEDPASS CREATE;SP01;T001;10", "SPECIALPASS CREATE;PE01;T001",
                "SPEEDPASS CREATE;SP02;TG01:12345678Z;1", "SPEEDPASS CREATE;SP03;TG01:87654321X;1",
                "ATTRACTION ENTER;A02;T001;SPEED", "ATTRACTION EXIT;A02;ana@correo.com",
                "ATTRACTION ENTER;A03;TG01;SPEED", "ATTRACTION EXIT;A03;ana@correo.com",
                "ATTRACTION EXIT;A03;pedro@correo.com", "ATTRACTION ENTER;A04;T001", "ATTRACTION STATUS;A04",
                "INVOICE CREATE;USER;ana@correo.com", "INVOICE CREATE;GROUP;G01"};
        for (String command : commands) {
            assertTrue(processor.process(command), command);
        }
        String text = output();
        assertFalse(text.contains("ERROR"), text);
        assertTrue(text.contains("SPEEDPASS CREATED: SP01:1 to SP01:10 for T001, 10 x 8.00 EUR, total 80.00 EUR"));
        assertTrue(text.contains("SPECIALPASS CREATED: PE01 for T001, price 40.00 EUR"));
        assertTrue(text.contains("SPEEDPASS CREATED: SP02:1 for TG01:12345678Z, 1 x 8.00 EUR, total 8.00 EUR"));
        assertTrue(text.contains("SPEEDPASS CONSUMED: SP01:1 (ticket T001)"));
        assertTrue(text.contains("ATTRACTION ENTERED: 2 user(s) entered A03\nSPEEDPASS CONSUMED: SP02:1"));
        assertTrue(text.contains("SPECIAL PASS CONSUMED: PE01 (ticket T001)"));
        assertTrue(text.contains("Type: SPECIAL"));
        assertTrue(text.contains("INVOICE - USER"));
        assertTrue(text.contains("INVOICE - GROUP"));
    }

    @Test
    void delivery3CommandErrors() {
        for (String command : new String[] {"SPEEDPASS CREATE;SP;T1", "SPEEDPASS DELETE;X",
                "SPECIALPASS CREATE;PE;T999", "SPECIALPASS DELETE;PE", "INVOICE CREATE;SHOP;x",
                "ATTRACTION CREATE;A9;X;3;VIP", "ATTRACTION ENTER;A9;T1;SPEED;X"}) {
            assertTrue(processor.process(command), command);
            assertTrue(output().startsWith("ERROR: "), command);
        }
    }
}
