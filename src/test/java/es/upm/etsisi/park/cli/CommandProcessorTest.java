package es.upm.etsisi.park.cli;

import es.upm.etsisi.park.core.Park;
import es.upm.etsisi.park.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserRepository repository;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        Park.getInstance().load(dir);
        repository = Park.getInstance().getUsers();
        processor = new CommandProcessor(Park.getInstance(), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void runsAssignmentExampleSequence() throws IOException {
        String input = String.join("\n", CREATE_ANA, CREATE_PEDRO,
                "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999",
                "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com",
                "USER DELETE;pedro@correo.com;Otra2#",
                "HELP",
                "EXIT");

        new CommandRunner(processor).run(new BufferedReader(new StringReader(input)));

        String[] lines = output().split("\n");
        assertEquals("USER CREATED", lines[0].trim());
        assertEquals("USER CREATED", lines[1].trim());
        assertEquals("USER UPDATED", lines[2].trim());
        assertEquals("USER UPDATED", lines[3].trim());
        assertEquals("USER DELETED", lines[4].trim());
        assertTrue(output().contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findByEmail("ana.nueva@correo.com").isPresent());
    }

    @Test
    void unknownCommandReportsErrorAndContinues() {
        assertTrue(processor.process("FOO BAR;1"));
        assertTrue(output().startsWith("ERROR:"));
    }

    @Test
    void wrongArgumentCountReportsSyntax() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia"));

        assertTrue(output().contains("Usage: USER CREATE;"));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void rejectedOperationReportsError() {
        processor.process(CREATE_ANA);
        processor.process("USER DELETE;ana@correo.com;Wrong1!");

        assertTrue(output().contains("ERROR: Incorrect password"));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void commandNamesAreCaseInsensitive() {
        processor.process("user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");

        assertTrue(output().contains("USER CREATED"));
    }

    @Test
    void runsDeliveryTwoExampleSequence() throws IOException {
        String input = String.join("\n",
                "SYSTEM DATE;2027-04-15",
                "SYSTEM PRICE;40.00",
                CREATE_ANA, CREATE_PEDRO,
                "USER CREATE;Laura;Martin Ruiz;11111111H;laura@correo.com;Laura3$;634567890;1111222233334444",
                "USER CREATE;Marta;Sanchez Perez;00000000T;marta@correo.com;Marta4%;645678901;2222333344445555",
                "GROUP CREATE;G01;Familia Garcia",
                "GROUP ADD;G01;ana@correo.com",
                "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;laura@correo.com",
                "ATTRACTION CREATE;A01;Dragon Rojo;20",
                "ATTRACTION CREATE;A02;Torre del Cielo;12",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION ENTER;A01;TG01",
                "ATTRACTION LIST;A01",
                "ATTRACTION STATUS;A01",
                "ATTRACTION EXIT;A01;ana@correo.com",
                "ATTRACTION EXIT;A01;pedro@correo.com",
                "ATTRACTION EXIT;A01;laura@correo.com",
                "ATTRACTION STATUS;A01",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "ATTRACTION ENTER;A02;T001",
                "ATTRACTION LIST;A02",
                "ATTRACTION STATUS;A02",
                "ATTRACTION EXIT;A02;ana@correo.com",
                "ATTRACTION STATUS;A02",
                "SYSTEM PRICE;45.50",
                "TICKET CREATE;T002;marta@correo.com;2027-04-20",
                "TICKET DELETE;T002",
                "USER DELETE;marta@correo.com;Marta4%",
                "GROUP CREATE;G02;Visita Mayo",
                "GROUP ADD;G02;pedro@correo.com",
                "GROUP ADD;G02;laura@correo.com",
                "GROUPTICKET CREATE;TG02;G02;2027-04-20",
                "GROUPTICKET DELETE;TG02",
                "GROUP REMOVE;G02;laura@correo.com",
                "GROUP DELETE;G02",
                "HELP",
                "EXIT");

        new CommandRunner(processor).run(new BufferedReader(new StringReader(input)));

        String text = output();
        assertFalse(text.contains("ERROR"), text);
        assertTrue(text.contains("GROUP TICKET CREATED. Members: 3. Discount: 10%. Total price: 108.00 EUR"));
        assertTrue(text.contains("Inside: 3\nAvailable: 17"));
        assertTrue(text.contains("TICKET CREATED. Price: 40.00 EUR"));
        assertTrue(text.contains("TICKET CREATED. Price: 45.50 EUR"));
        assertTrue(text.contains("GROUP TICKET CREATED. Members: 2. Discount: 0%. Total price: 91.00 EUR"));
        assertTrue(text.contains("USER DELETED"));
        assertTrue(text.contains("GROUP DELETED"));
        assertTrue(repository.findByEmail("marta@correo.com").isEmpty());
    }

    @Test
    void helpListsEveryCommand() {
        processor.process("HELP");

        String text = output();
        for (String syntax : new String[]{
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
                "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT"}) {
            assertTrue(text.contains(syntax), syntax);
        }
    }

    @Test
    void emptyAttractionListIsExplicit() {
        processor.process("ATTRACTION CREATE;A02;Torre del Cielo;12");
        processor.process("ATTRACTION LIST;A02");

        assertTrue(output().contains("Users in A02:\n  (none)"));
    }

    @Test
    void exitStopsProcessing() throws IOException {
        assertFalse(processor.process("EXIT"));

        String input = String.join("\n", CREATE_ANA, "", "   ", "EXIT", CREATE_PEDRO);
        new CommandRunner(processor).run(new BufferedReader(new StringReader(input)));

        assertEquals(1, repository.findAll().size());
        assertFalse(output().contains("ERROR"));
    }
}
