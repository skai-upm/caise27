package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path tempDir;

    private UserManager manager;
    private CommandProcessor processor;

    @BeforeEach
    public void setUp() {
        manager = new UserManager(new UserRepository(tempDir.resolve("users.txt")));
        processor = new CommandProcessor(manager);
    }

    private void assertError(CommandProcessor.Result result) {
        assertTrue(result.output().startsWith("ERROR"), result.output());
        assertFalse(result.exit());
    }

    @Test
    public void createsUser() {
        CommandProcessor.Result result = processor.process(CREATE_ANA);
        assertEquals("OK USER CREATED", result.output());
        assertFalse(result.exit());
        assertNotNull(manager.findByEmail("ana@correo.com"));
    }

    @Test
    public void createWithMissingOrExtraParametersFails() {
        assertError(processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z"));
        assertError(processor.process(CREATE_ANA + ";extra"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    public void createWithInvalidDataFails() {
        assertError(processor.process("USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertError(processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;"));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    public void duplicateUsersAreRejected() {
        processor.process(CREATE_ANA);
        assertError(processor.process("USER CREATE;Otro;X;12345678Z;otro@correo.com;Clave1!;600000000;1234567890123456"));
        assertError(processor.process("USER CREATE;Otro;X;87654321X;ana@correo.com;Clave1!;600000000;1234567890123456"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    public void deletesUser() {
        processor.process(CREATE_ANA);
        assertError(processor.process("USER DELETE;ana@correo.com;Mala1!"));
        assertError(processor.process("USER DELETE;ana@correo.com"));
        assertEquals("OK USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!").output());
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    public void updatesUserThroughSpecSequence() {
        processor.process(CREATE_ANA);
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").output());
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").output());
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").output());
        assertEquals("OK USER UPDATED", processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").output());
        User ana = manager.findByDni("12345678Z");
        assertEquals("Ana Maria", ana.getName());
        assertEquals("9999888877776666", ana.getCard());
    }

    @Test
    public void dniAndUnknownFieldsCannotBeUpdated() {
        processor.process(CREATE_ANA);
        assertError(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
        assertError(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30"));
        assertError(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME"));
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
        assertNull(manager.findByDni("87654321X"));
    }

    @Test
    public void helpListsAllCommands() {
        CommandProcessor.Result result = processor.process("HELP");
        assertFalse(result.exit());
        assertTrue(result.output().contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(result.output().contains("USER DELETE;Correo;Contraseña"));
        assertTrue(result.output().contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        assertTrue(result.output().contains("HELP"));
        assertTrue(result.output().contains("EXIT"));
    }

    @Test
    public void exitRequestsStop() {
        assertTrue(processor.process("EXIT").exit());
    }

    @Test
    public void unknownCommandIsReported() {
        assertError(processor.process("FOO BAR;1"));
        assertError(processor.process("USER"));
        assertError(processor.process(";;;"));
    }
}
