package es.upm.park.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandProcessorTest {

    private final CommandProcessor processor = new CommandProcessor();

    @Test
    void helpListsMandatedCommandSyntax() {
        CommandResult result = processor.process("HELP");
        assertFalse(result.terminate());
        String out = result.output();
        assertTrue(out.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(out.contains("USER DELETE;<email>;<password>"));
        assertTrue(out.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").terminate());
    }

    @Test
    void unknownCommandReportsErrorAndContinues() {
        CommandResult result = processor.process("FOO");
        assertFalse(result.terminate());
        assertTrue(result.output().startsWith("ERROR"));
        assertTrue(result.output().contains("FOO"));
    }
}
