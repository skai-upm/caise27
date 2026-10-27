package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandProcessorTest {

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
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
        assertTrue(out.contains("USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>"));
        assertTrue(out.contains("USER DELETE;<email>;<password>"));
        assertTrue(out.contains("USER UPDATE;<email>;<password>;<field>;<value>"));
        assertTrue(out.contains("HELP"));
        assertTrue(out.contains("EXIT"));
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }

    @Test
    void unknownCommandIsRejectedWithoutStopping() {
        assertTrue(processor.process("FOO"));
        assertTrue(output().contains("Error"));
    }

    @Test
    void blankLineIsIgnored() {
        assertTrue(processor.process("   "));
        assertTrue(output().isEmpty());
    }
}
