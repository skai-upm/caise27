package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.InteractiveSession;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ExecutionControlTest {

    @Test
    void helpListsAllCommands() {
        String help = new CommandProcessor(new UserService(new UserRepository())).process("HELP");
        assertTrue(help.contains("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>"));
        assertTrue(help.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(help.contains("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void helpListsDeliveryTwoSyntaxInOrder() {
        String help = new CommandProcessor(new UserService(new UserRepository())).process("HELP");
        String[] expected = {"USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>", "SYSTEM PRICE;<Price>", "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>", "GROUP REMOVE;<Identifier>;<Email>", "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>", "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>", "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>", "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>", "HELP", "EXIT"};
        int from = 0;
        for (String line : expected) {
            int at = help.indexOf(line, from);
            assertTrue(at >= 0, line + " missing or out of order");
            from = at + line.length();
        }
    }

    @Test
    void exitSetsStateAndExtraArgumentsRejected() {
        CommandProcessor p = new CommandProcessor(new UserService(new UserRepository()));
        assertTrue(p.process("EXIT;now").startsWith("ERROR"));
        assertTrue(p.process("HELP;x").startsWith("ERROR"));
        assertFalse(p.isExitRequested());
        assertEquals("GOODBYE", p.process("EXIT"));
        assertTrue(p.isExitRequested());
    }

    @Test
    void noCommandRunsAfterExit() {
        UserRepository repo = new UserRepository();
        CommandProcessor p = new CommandProcessor(new UserService(repo));
        String input = "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "EXIT\n"
                + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = new InteractiveSession(new BufferedReader(new StringReader(input)),
                new PrintStream(out, true, StandardCharsets.UTF_8), p).run();
        assertEquals(2, read);
        assertEquals(1, repo.count());
        assertTrue(repo.findByDni("87654321X").isEmpty());
    }
}
