package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private Path file;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws Exception {
        file = dir.resolve("users.txt");
        processor = newProcessor();
    }

    private CommandProcessor newProcessor() throws Exception {
        return new CommandProcessor(new UserManager(new UserFileRepository(file)));
    }

    private static void assertOk(String result) {
        assertTrue(result.startsWith("OK"), result);
    }

    private static void assertError(String result) {
        assertTrue(result.startsWith("ERROR"), result);
    }

    @Test
    void appendixExampleSequence() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertEquals(List.of("Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertOk(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertOk(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666"));
        assertEquals(List.of("Ana Maria;Garcia Lopez;12345678Z;ana.nueva@correo.com;Nueva2#;612345678;9999888877776666"),
                Files.readAllLines(file, StandardCharsets.UTF_8));

        CommandProcessor restarted = newProcessor();
        assertError(restarted.process("USER DELETE;ana.nueva@correo.com;Clave1!"));
        assertOk(restarted.process("USER DELETE;ana.nueva@correo.com;Nueva2#"));
        assertTrue(Files.readAllLines(file, StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    void duplicateIdentitiesRejected() {
        assertOk(processor.process(CREATE_ANA));
        assertError(processor.process("USER CREATE;Otra;X;12345678Z;otro@correo.com;Clave1!;1;1234567890123456"));
        assertError(processor.process("USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;1;1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "USER RENAME;ana@correo.com",
        "HELLO",
        "USER",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER DELETE;ana@correo.com",
        "USER DELETE;ana@correo.com;Clave1!;extra",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
        "USER UPDATE;ana@correo.com;Clave1!;AGE;30",
        ";;;"
    })
    void invalidCommandsAreRejectedWithoutChanges(String command) throws Exception {
        assertOk(processor.process(CREATE_ANA));
        List<String> before = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertError(processor.process(command));
        assertEquals(before, Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void dniUpdateKeepsDni() throws Exception {
        assertOk(processor.process(CREATE_ANA));
        assertError(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X"));
        assertTrue(Files.readString(file).contains("12345678Z"));
    }

    @Test
    void fieldNameIsCaseInsensitiveAndOperationWhitespaceTolerated() {
        assertOk(processor.process(CREATE_ANA.replace("USER CREATE", "user  create")));
        assertOk(processor.process("USER UPDATE;ana@correo.com;Clave1!;phone;699999999"));
    }

    @Test
    void helpListsAllCommands() {
        String help = processor.process("HELP");
        assertOk(help);
        for (String syntax : List.of("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
                "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT")) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertError(processor.process("HELP;extra"));
    }

    @Test
    void exitSetsFlagAndRejectsParameters() {
        assertError(processor.process("EXIT;now"));
        assertFalse(processor.isExitRequested());
        assertOk(processor.process("exit"));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void blankLinesAreIgnored() {
        assertNull(processor.process(""));
        assertNull(processor.process("   "));
    }
}
