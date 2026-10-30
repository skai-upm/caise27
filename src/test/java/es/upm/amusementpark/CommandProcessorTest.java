package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.*;
import es.upm.amusementpark.business.exception.*;
import es.upm.amusementpark.business.model.*;
import es.upm.amusementpark.business.security.*;
import es.upm.amusementpark.persistence.file.*;
import es.upm.amusementpark.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private es.upm.amusementpark.persistence.UserRepository repository;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = ApplicationInitializer.initialize(dir, ParkTestSupport.HASHER);
        repository = Park.getInstance().getUsers();
    }

    @Test
    void createDeleteUpdateMessages() {
        assertEquals("USER CREATED", processor.process(CREATE_ANA).output());
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999").output());
        assertEquals("699999999", repository.findByEmail("ana@correo.com").getPhone());
        assertEquals("USER UPDATED",
                processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").output());
        assertEquals("Ana Maria", repository.findByEmail("ana@correo.com").getFirstName());
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!").output());
        assertNull(repository.findByEmail("ana@correo.com"));
    }

    @Test
    void rejectedOperationsReportErrors() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process(CREATE_ANA).output().startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Mala1!").output().startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").output()
                .startsWith("ERROR:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;COLOR;red").output()
                .startsWith("ERROR:"));
        assertEquals("12345678Z", repository.findByEmail("ana@correo.com").getDni());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FOO BAR", "USER LIST", "USER", "USER CREATE", ";;;", "USER UPDATE;a;b",
            "USER CREATE;Ana;;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
            "USER DELETE;ana@correo.com;Clave1!;extra", "HELP;now", "help", "exit"})
    void malformedLinesAreErrorsAndDoNotExit(String line) {
        CommandProcessor.Result result = processor.process(line);
        assertTrue(result.output().startsWith("ERROR:"), result.output());
        assertFalse(result.exit());
    }

    @Test
    void helpListsEveryCommand() {
        CommandProcessor.Result result = processor.process("HELP");
        assertFalse(result.exit());
        String help = result.output();
        for (String syntax : new String[] {
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
                "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
                "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
                "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
                "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "SPEEDPASS DELETE;<speedPassIdentifier>", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "SPECIALPASS DELETE;<specialPassIdentifier>", "INVOICE CREATE;USER;<email>",
                "INVOICE CREATE;GROUP;<groupIdentifier>", "HELP EXTENDED"}) {
            assertTrue(help.contains(syntax), syntax);
        }
    }

    @Test
    void extendedHelpDescribesEveryCommand() {
        String help = processor.process("HELP EXTENDED").output();
        for (CommandHelp command : CommandCatalog.commands()) {
            assertTrue(help.contains(command.syntax() + System.lineSeparator() + "    " + command.description()),
                    command.syntax());
        }
        assertTrue(processor.process("HELP EXTENDED;x").output().startsWith("ERROR:"));
    }

    @Test
    void helpGeneratorUsesInjectedFormatter() {
        HelpGenerator generator = new HelpGenerator(List.of(new CommandHelp("A;<b>", "does a")),
                commands -> "custom " + commands.size());
        assertEquals("custom 1", generator.generate());
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertEquals("USER CREATED", processor.process("  " + CREATE_ANA + "  ").output());
        assertNotNull(repository.findByEmail("ana@correo.com"));
    }
}
