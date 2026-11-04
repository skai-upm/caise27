package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;
    UserManager manager;
    CommandProcessor processor;

    @BeforeEach
    void setUp() {
        manager = new UserManager(new UserFileStorage(dir.resolve("users.txt")));
        processor = new CommandProcessor(manager);
    }

    @Test
    void createDeleteAndUpdateExamples() {
        assertTrue(processor.process(CREATE_ANA).startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").startsWith("OK"));
        assertEquals("Ana Maria", manager.findByEmail("ana.nueva@correo.com").getName());
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Nueva2#").startsWith("OK"));
        assertEquals(0, manager.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "hello", "USER RENAME;ana@correo.com", "USER CREATE;Ana;Garcia Lopez;12345678Z",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER DELETE;ana@correo.com", "USER DELETE;ana@correo.com;Clave1!;x", "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER", ";;;"})
    void invalidCommandsProduceErrors(String line) {
        assertTrue(processor.process(line).startsWith("ERROR"), line);
        assertEquals(0, manager.getUsers().size());
    }

    @Test
    void dniAndUnknownFieldsCannotBeUpdated() {
        processor.process(CREATE_ANA);
        String result = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertTrue(result.startsWith("ERROR"), result);
        assertEquals("12345678Z", manager.findByEmail("ana@correo.com").getDni());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;AGE;30").startsWith("ERROR"));
    }

    @Test
    void invalidCreationNamesTheField() {
        String result = processor.process(
                "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456");
        assertTrue(result.startsWith("ERROR") && result.contains("DNI"), result);
    }

    @Test
    void lowercaseFieldNamesAreAccepted() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;phone;600000000").startsWith("OK"));
    }

    @Test
    void helpListsAllCommandsAndFields() {
        String help = processor.process("HELP");
        assertTrue(help.startsWith("OK"));
        for (String usage : new String[] {
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT",
            "NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD"}) {
            assertTrue(help.contains(usage), usage);
        }
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void helpAndExitRejectParameters() {
        assertTrue(processor.process("HELP;extra").startsWith("ERROR"));
        assertTrue(processor.process("EXIT;now").startsWith("ERROR"));
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void exitSetsFlag() {
        assertTrue(processor.process("EXIT").startsWith("OK"));
        assertTrue(processor.isExitRequested());
    }
}
