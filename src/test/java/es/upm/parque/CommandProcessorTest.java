package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager users;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        processor = new CommandProcessor(users);
    }

    @Test
    void createsUser() {
        assertEquals("OK USER CREATED", processor.process(CREATE_ANA));
        assertEquals("ERROR DUPLICATE DNI", processor.process(CREATE_ANA));
    }

    @Test
    void operationIgnoresCaseAndParametersAreTrimmed() {
        assertEquals("OK USER CREATED", processor.process(
                " user create ; Ana ; Garcia Lopez ; 12345678Z ; ana@correo.com ; Clave1! ; 612345678 ; 1234567890123456"));
        assertEquals("Ana", users.findByEmail("ana@correo.com").getName());
    }

    @Test
    void updatesAndDeletesUser() {
        processor.process(CREATE_ANA);
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("699999999", users.findByEmail("ana@correo.com").getPhone());
        assertEquals("ERROR INVALID CREDENTIALS", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void rejectsUnknownCommands() {
        assertEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process("USER FLY;x"));
        assertEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process("HOLA"));
        assertEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process(";;;"));
    }

    @Test
    void rejectsWrongParameterCounts() {
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("USER CREATE;Ana;Garcia Lopez"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process(CREATE_ANA + ";extra"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("USER DELETE;ana@correo.com"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertEquals(0, users.count());
    }

    @Test
    void unexpectedFailuresBecomeErrors() {
        CommandProcessor broken = new CommandProcessor(null);
        assertEquals(CommandProcessor.UNEXPECTED_ERROR, broken.process(CREATE_ANA));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.lines().anyMatch("HELP"::equals));
        assertTrue(help.lines().anyMatch("EXIT"::equals));
        assertFalse(processor.isFinished());
    }

    @Test
    void everyDocumentedCommandIsRecognized() {
        for (String syntax : CommandProcessor.COMMAND_SYNTAX) {
            assertNotEquals(CommandProcessor.UNKNOWN_COMMAND, processor.process(syntax));
        }
    }

    @Test
    void exitFinishesProcessing() {
        assertFalse(processor.isFinished());
        assertEquals("OK EXIT", processor.process(" exit "));
        assertTrue(processor.isFinished());
    }

    @Test
    void helpAndExitTakeNoParameters() {
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("HELP;x"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("EXIT;x"));
        assertFalse(processor.isFinished());
    }
}
