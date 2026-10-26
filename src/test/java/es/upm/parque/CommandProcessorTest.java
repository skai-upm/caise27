package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private final SystemDate systemDate = new SystemDate(LocalDate.of(2026, 9, 1));
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        processor = newProcessor();
    }

    private CommandProcessor newProcessor() throws IOException {
        UserManager users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        GroupManager groups = new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
        return new CommandProcessor(users, groups, systemDate);
    }

    @Test
    void createDeleteUpdateFlow() {
        assertEquals("OK USER CREATED", processor.process(CREATE_ANA));
        assertTrue(processor.process(CREATE_ANA).startsWith("ERROR"));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana.nueva@correo.com;Clave1!"));
    }

    @Test
    void reportsInvalidData() {
        assertTrue(processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456")
                .startsWith("ERROR Invalid DNI"));
    }

    @Test
    void reportsWrongParameterCount() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME").startsWith("ERROR Wrong number of parameters"));
        assertTrue(processor.process("USER DELETE;a;b;c").startsWith("ERROR Wrong number of parameters"));
    }

    @Test
    void reportsUnknownCommand() {
        assertEquals("ERROR Unknown command: FOO BAR", processor.process("FOO BAR;1"));
        assertEquals("ERROR Unknown command: USER", processor.process("USER"));
    }

    @Test
    void helpListsEveryCommand() {
        String help = processor.process("HELP");
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(help.contains("HELP"));
        assertTrue(help.contains("EXIT"));
    }

    @Test
    void exitSetsExitFlag() {
        assertFalse(processor.isExitRequested());
        assertEquals("OK EXIT", processor.process("EXIT"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void systemDateCanMoveForwardAndBackward() {
        assertEquals("OK SYSTEM DATE 2026-09-15", processor.process("SYSTEM DATE;2026-09-15"));
        assertEquals(LocalDate.of(2026, 9, 15), systemDate.get());
        assertEquals("OK SYSTEM DATE 2026-01-01", processor.process("SYSTEM DATE;2026-01-01"));
        assertEquals(LocalDate.of(2026, 1, 1), systemDate.get());
    }

    @Test
    void invalidSystemDateIsRejected() {
        assertTrue(processor.process("SYSTEM DATE;15/09/2026").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE;2026-02-30").startsWith("ERROR Invalid date"));
        assertTrue(processor.process("SYSTEM DATE").startsWith("ERROR Wrong number of parameters"));
        assertEquals(LocalDate.of(2026, 9, 1), systemDate.get());
        assertTrue(processor.process("HELP").contains("SYSTEM DATE;yyyy-MM-dd"));
    }

    @Test
    void groupCommands() throws IOException {
        processor.process(CREATE_ANA);
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        assertEquals("OK GROUP CREATED", processor.process("GROUP CREATE;G01;Familia Garcia"));
        assertTrue(processor.process("GROUP CREATE;G01;Otra").startsWith("ERROR"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;ana@correo.com"));
        assertEquals("OK USER ADDED TO GROUP", processor.process("GROUP ADD;G01;pedro@correo.com"));
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G01;nadie@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertEquals("OK USER REMOVED FROM GROUP", processor.process("GROUP REMOVE;G01;pedro@correo.com"));
        assertTrue(processor.process("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("GROUP CREATE;G02").startsWith("ERROR Wrong number of parameters"));

        Group restored = newProcessorGroups().find("G01");
        assertEquals("Familia Garcia", restored.getName());
        assertEquals(1, restored.getMembers().size());
        assertEquals("12345678Z", restored.getMembers().get(0).getDni());

        assertEquals("OK GROUP DELETED", processor.process("GROUP DELETE;G01"));
        assertTrue(processor.process("GROUP DELETE;G01").startsWith("ERROR"));
        assertTrue(processor.process("HELP").contains("GROUP ADD;IdGrupo;Correo"));
    }

    @Test
    void deletedUserLeavesGroups() throws IOException {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(newProcessorGroups().find("G01").getMembers().isEmpty());
    }

    private GroupManager newProcessorGroups() throws IOException {
        UserManager users = new UserManager(new UserRepository(dir.resolve("users.txt")));
        return new GroupManager(new GroupRepository(dir.resolve("groups.txt")), users);
    }

    @Test
    void rejectsDniUpdate() {
        processor.process(CREATE_ANA);
        assertEquals("ERROR Invalid field: DNI", processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
    }
}
