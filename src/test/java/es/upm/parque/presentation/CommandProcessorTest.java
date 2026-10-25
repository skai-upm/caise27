package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;

import es.upm.parque.ParkInitializer;
import es.upm.parque.ParkTestSupport;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.security.BCryptPasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    @TempDir
    Path dir;

    private ByteArrayOutputStream buffer;
    private UserController manager;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        processor = ParkInitializer.initialize(dir, LocalDate.of(2026, 10, 1),
                new BCryptPasswordHasher(ParkTestSupport.TEST_LOG_ROUNDS),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        manager = UserController.getInstance();
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void createDeleteUpdateCommands() {
        assertTrue(processor.process(CREATE_ANA));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("Ana Maria", manager.findByEmail("ana@correo.com").getName());
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(manager.getUsers().isEmpty());
        assertEquals(String.join(System.lineSeparator(), "OK USER CREATED", "OK USER UPDATED", "OK USER DELETED", ""),
                output());
    }

    @Test
    void rejectedOperationPrintsError() {
        processor.process(CREATE_ANA);
        buffer.reset();
        processor.process("USER DELETE;ana@correo.com;Mala1!");
        assertTrue(output().startsWith("ERROR"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void unknownCommandAndWrongParameterCountDoNotStopProcessing() {
        assertTrue(processor.process("FOO BAR;1"));
        assertTrue(processor.process("USER CREATE;Ana"));
        assertTrue(processor.process("USER"));
        assertTrue(processor.process("USER DELETE;a@b.com;Clave1!;extra"));
        String[] lines = output().split(System.lineSeparator());
        assertEquals(4, lines.length);
        for (String line : lines) {
            assertTrue(line.startsWith("ERROR"), line);
        }
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void emptyTrailingFieldIsAnEmptyValueNotAMissingParameter() {
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;");
        assertTrue(output().startsWith("ERROR Invalid card"));
    }

    @Test
    void commandNameIsCaseInsensitive() {
        processor.process(CREATE_ANA.replace("USER CREATE", "user create"));
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void helpListsAllCommands() {
        assertTrue(processor.process("HELP"));
        String help = output();
        assertTrue(help.contains("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta"));
        assertTrue(help.contains("USER DELETE;Correo;Contraseña"));
        assertTrue(help.contains("USER UPDATE;Correo;Contraseña;Campo;NuevoValor"));
        for (String syntax : new String[] {"SYSTEM DATE;yyyy-MM-dd", "SYSTEM PRICE;Precio",
                "GROUP CREATE;IdGrupo;Nombre", "GROUP ADD;IdGrupo;Correo", "GROUP REMOVE;IdGrupo;Correo",
                "GROUP DELETE;IdGrupo", "TICKET CREATE;IdEntrada;Correo;Fecha", "TICKET DELETE;IdEntrada",
                "GROUPTICKET CREATE;IdEntrada;IdGrupo;Fecha", "GROUPTICKET DELETE;IdEntrada",
                "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]", "ATTRACTION DELETE;IdAtraccion",
                "ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]", "ATTRACTION EXIT;IdAtraccion;IdEntrada",
                "ATTRACTION STATUS;IdAtraccion", "INVOICE CREATE;USER;Correo", "INVOICE CREATE;GROUP;IdGrupo",
                "SPEEDPASS CREATE;IdEntrada;Cantidad", "SPEEDPASS DELETE;IdSpeedPass",
                "SPECIALPASS CREATE;IdEntrada", "SPECIALPASS DELETE;IdEntrada", "HELP", "HELP EXTENDED", "EXIT"}) {
            assertTrue(help.contains("  " + syntax + System.lineSeparator()), syntax);
        }
    }

    @Test
    void exitStopsProcessing() {
        assertFalse(processor.process("EXIT"));
    }

    @Test
    void extendedHelpShowsSyntaxAndDescriptionOfEveryCommand() {
        processor.process("HELP");
        String[] concise = output().split(System.lineSeparator());
        buffer.reset();
        assertTrue(processor.process("HELP EXTENDED"));
        String extended = output();
        for (int i = 1; i < concise.length; i++) {
            int at = extended.indexOf(concise[i] + System.lineSeparator());
            assertTrue(at >= 0, concise[i]);
            String next = extended.substring(at + concise[i].length() + System.lineSeparator().length());
            assertTrue(next.startsWith("      ") && next.trim().length() > 10, concise[i]);
        }
        assertTrue(extended.contains("SpeedPass"));
    }

    @Test
    void optionalParametersAreAcceptedWithinRange() {
        assertTrue(processor.process("ATTRACTION CREATE;A01;Tornado;20"));
        assertTrue(processor.process("ATTRACTION CREATE;A02;Casa;10;SPECIAL"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;SPEED;X"));
        assertTrue(processor.process("ATTRACTION CREATE;A03"));
        String[] lines = output().split(System.lineSeparator());
        assertEquals("OK ATTRACTION CREATED", lines[0]);
        assertEquals("OK ATTRACTION CREATED", lines[1]);
        assertEquals("ERROR ATTRACTION ENTER expects 2-3 parameter(s) but received 4. "
                + "Usage: ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]", lines[2]);
        assertTrue(lines[3].startsWith("ERROR ATTRACTION CREATE expects 3-4"));
    }

    @Test
    void keywordCommandsAreDispatchedByTheirFirstParameter() {
        processor.process("INVOICE CREATE;OTHER;x");
        processor.process("INVOICE CREATE;USER");
        String[] lines = output().split(System.lineSeparator());
        assertTrue(lines[0].startsWith("ERROR Unknown command"));
        assertTrue(lines[1].startsWith("ERROR INVOICE CREATE;USER expects 1 parameter(s)"));
    }
}
