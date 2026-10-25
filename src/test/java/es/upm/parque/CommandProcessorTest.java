package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private UserManager users;
    private GroupManager groups;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
        processor = new CommandProcessor(users, groups, new SystemDate(LocalDate.of(2026, 1, 1)));
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
        CommandProcessor broken = new CommandProcessor(null, null, new SystemDate(LocalDate.of(2026, 1, 1)));
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

    @Test
    void setsSystemDateBackwardsAndForwards() {
        assertEquals("OK SYSTEM DATE -> 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), processor.getSystemDate().get());
        assertEquals("OK SYSTEM DATE -> 2025-01-01", processor.process("system date; 2025-01-01 "));
        assertEquals(LocalDate.of(2025, 1, 1), processor.getSystemDate().get());
    }

    @Test
    void rejectsInvalidSystemDate() {
        assertEquals(CommandProcessor.INVALID_DATE, processor.process("SYSTEM DATE;15/09/2026"));
        assertEquals(CommandProcessor.INVALID_DATE, processor.process("SYSTEM DATE;2026-02-30"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("SYSTEM DATE"));
        assertEquals(LocalDate.of(2026, 1, 1), processor.getSystemDate().get());
    }

    @Test
    void helpListsSystemDate() {
        assertTrue(processor.process("HELP").lines().anyMatch("SYSTEM DATE;yyyy-MM-dd"::equals));
    }

    @Test
    void runsGroupCommands() {
        processor.process(CREATE_ANA);
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("ERROR USER ALREADY IN GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("ERROR USER NOT FOUND", processor.process("GROUP ADD;G01;nadie@correo.com"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("group remove;G01;ana@correo.com"));
        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertEquals("ERROR GROUP NOT FOUND", processor.process("GROUP DELETE;G01"));
    }

    @Test
    void groupCommandsCheckParameterCounts() {
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP CREATE;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP ADD;G01"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP REMOVE;G01;a;b"));
        assertEquals(CommandProcessor.WRONG_PARAMETERS, processor.process("GROUP DELETE"));
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("ERROR INVALID CREDENTIALS", processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertEquals(1, groups.find("G01").getMembers().size());
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(groups.find("G01").getMembers().isEmpty());
    }

    @Test
    void helpListsGroupCommands() {
        String help = processor.process("HELP");
        for (String command : List.of("GROUP CREATE;Id;Nombre", "GROUP ADD;Id;Correo", "GROUP REMOVE;Id;Correo",
                "GROUP DELETE;Id")) {
            assertTrue(help.lines().anyMatch(command::equals), command);
        }
    }
}
