package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
        assertTrue(output().contains("USER DELETE;<email>;<password>"));
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
    void blankLineIsIgnored() {
        assertTrue(processor.process("   "));
        assertTrue(output().isEmpty());
    }
}
