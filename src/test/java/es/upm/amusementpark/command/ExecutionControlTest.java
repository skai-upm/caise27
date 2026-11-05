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
        assertTrue(help.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(help.contains("USER DELETE;<email>;<password>"));
        assertTrue(help.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
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
