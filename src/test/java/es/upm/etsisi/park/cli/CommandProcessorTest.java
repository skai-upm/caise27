package es.upm.etsisi.park.cli;

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

import es.upm.etsisi.park.Park;
import es.upm.etsisi.park.persistence.ParkStorage;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private Park service;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        service = Park.getInstance();
        service.open(new ParkStorage(dir, out));
        processor = new CommandProcessor(service, out);
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
        assertNotNull(service.findUser("ana@correo.com"));
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
        assertEquals("699999999", service.findUser("ana@correo.com").getPhone());
        processor.process("USER DELETE;ana@correo.com;Clave1!");
        assertEquals("USER DELETED", output().trim());
        assertNull(service.findUser("ana@correo.com"));
    }

    @Test
    void reportsValidationErrors() {
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(output().startsWith("ERROR: Invalid DNI"));
        assertTrue(service.getUsers().isEmpty());
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
        assertEquals("12345678Z", service.findUser("ana@correo.com").getDni());
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
        assertTrue(text.contains("Attraction: A01\nName: Dragon Rojo\nCapacity: 20\nInside: 3\nAvailable: 17\n"
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
}
