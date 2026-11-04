package es.upm.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

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
    private UserManager manager;
    private GroupManager groups;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        manager = new UserManager(new UserFileStore(dir.resolve("users.txt")));
        groups = new GroupManager(new GroupFileStore(dir.resolve("groups.txt")), manager);
        processor = new CommandProcessor(manager, groups);
    }

    @Test
    void createsUserFromExampleCommand() {
        Result r = processor.process(CREATE_ANA);
        assertTrue(r.success(), r.message());
        assertTrue(r.toString().startsWith("OK: "));
        assertEquals("Garcia Lopez", manager.getUsers().get(0).getSurnames());
    }

    @Test
    void runsSourceUpdateExamplesInOrder() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").success());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").success());
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").success());
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").success());
        User u = manager.getUsers().get(0);
        assertEquals("Ana Maria", u.getFirstName());
        assertEquals("9999888877776666", u.getCardNumber());
    }

    @Test
    void deleteExample() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").success());
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void dniIsNotAnEditableField() {
        processor.process(CREATE_ANA);
        Result r = processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X");
        assertFalse(r.success());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "FOO",
        "USER",
        "USER LIST",
        "USER CREATE;Ana",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        "USER DELETE;a@b.com",
        "USER DELETE;ana@correo.com;",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "user create;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456",
        ";;;",
        "   "
    })
    void malformedCommandsAreRejectedSafely(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertTrue(r.toString().startsWith("ERROR: "));
        assertTrue(manager.getUsers().isEmpty());
    }

    @Test
    void nullCommandIsRejected() {
        assertFalse(processor.process(null).success());
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertTrue(processor.process("  USER  CREATE ; Ana ;Garcia Lopez; 12345678Z ;ana@correo.com;Clave1!;612345678;1234567890123456 ").success());
        assertEquals("12345678Z", manager.getUsers().get(0).getDni());
    }

    @Test
    void helpListsAllCommandsAndChangesNothing() {
        processor.process(CREATE_ANA);
        Result r = processor.process("HELP");
        assertTrue(r.success());
        assertFalse(r.terminate());
        for (String syntax : new String[] {
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd",
            "HELP",
            "EXIT"}) {
            assertTrue(r.message().contains(syntax), syntax);
        }
        assertEquals(1, manager.getUsers().size());
    }

    @Test
    void exitTerminates() {
        Result r = processor.process("EXIT");
        assertTrue(r.success());
        assertTrue(r.terminate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"HELP;extra", "EXIT;now", "HELP ME", "help", "exit"})
    void generalCommandsRejectExtras(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertFalse(r.terminate());
    }

    @Test
    void userCommandsDoNotTerminate() {
        assertFalse(processor.process(CREATE_ANA).terminate());
        assertFalse(processor.process("FOO").terminate());
    }

    // ---- system date ----

    private static final LocalDate START = LocalDate.of(2026, 9, 15);

    private CommandProcessor processorAt(LocalDate date) {
        return new CommandProcessor(manager, groups, date);
    }

    @Test
    void defaultControllerStartsWithLocalDate() {
        LocalDate before = LocalDate.now();
        LocalDate date = processor.getCurrentDate();
        LocalDate after = LocalDate.now();
        assertTrue(!date.isBefore(before) && !date.isAfter(after));
    }

    @Test
    void injectedInitialDateIsReported() {
        assertEquals(START, processorAt(START).getCurrentDate());
    }

    @Test
    void systemDateSetsCurrentDate() {
        CommandProcessor p = processorAt(LocalDate.of(2025, 1, 1));
        Result r = p.process("SYSTEM DATE;2026-09-15");
        assertTrue(r.success(), r.message());
        assertEquals(START, p.getCurrentDate());
    }

    @Test
    void dateCanMoveBackwardAndForward() {
        CommandProcessor p = processorAt(START);
        assertTrue(p.process("SYSTEM DATE;2020-01-01").success());
        assertEquals(LocalDate.of(2020, 1, 1), p.getCurrentDate());
        assertTrue(p.process("SYSTEM DATE;2030-12-31").success());
        assertEquals(LocalDate.of(2030, 12, 31), p.getCurrentDate());
    }

    @Test
    void leapDayAccepted() {
        CommandProcessor p = processorAt(START);
        assertTrue(p.process("SYSTEM DATE;2028-02-29").success());
        assertEquals(LocalDate.of(2028, 2, 29), p.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "SYSTEM DATE;15-09-2026", "SYSTEM DATE;2026/09/15", "SYSTEM DATE;2026-9-15",
        "SYSTEM DATE;2026-02-30", "SYSTEM DATE;2027-02-29", "SYSTEM DATE;hoy",
        "SYSTEM DATE;2026-13-01", "SYSTEM DATE;+2026-09-15", "SYSTEM DATE;20260915",
        "SYSTEM DATE", "SYSTEM DATE;", "SYSTEM DATE;2026-09-15;x", "SYSTEM;2026-09-15",
        "SYSTEM TIME;2026-09-15"})
    void invalidDatesLeaveDateUnchanged(String command) {
        CommandProcessor p = processorAt(START);
        Result r = p.process(command);
        assertFalse(r.success());
        assertFalse(r.terminate());
        assertEquals(START, p.getCurrentDate());
    }

    @Test
    void helpDoesNotChangeDate() {
        CommandProcessor p = processorAt(START);
        p.process("HELP");
        assertEquals(START, p.getCurrentDate());
    }

    // ---- groups ----

    @Test
    void sourceGroupCommands() {
        processor.process(CREATE_ANA);
        assertTrue(processor.process("GROUP CREATE;G01;Familia Garcia").success());
        assertTrue(processor.process("GROUP ADD;G01;ana@correo.com").success());
        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
        assertTrue(processor.process("GROUP REMOVE;G01;ana@correo.com").success());
        assertTrue(processor.process("GROUP DELETE;G01").success());
        assertTrue(groups.getGroups().isEmpty());
        assertEquals(1, manager.getUsers().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUP CREATE;G01", "GROUP CREATE;;Familia", "GROUP CREATE;G01;Familia;x",
        "GROUP ADD;G01", "GROUP REMOVE;G01;", "GROUP DELETE", "GROUP DELETE;G01;x", "GROUP LIST", "GROUP"})
    void malformedGroupCommandsRejected(String command) {
        Result r = processor.process(command);
        assertFalse(r.success());
        assertTrue(groups.getGroups().isEmpty());
    }

    @Test
    void deletingUserRemovesItFromGroups() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertTrue(processor.process("USER DELETE;ana@correo.com;Clave1!").success());
        assertTrue(groups.find("G01").orElseThrow().getMembers().isEmpty());
    }

    @Test
    void failedUserDeleteKeepsMembership() {
        processor.process(CREATE_ANA);
        processor.process("GROUP CREATE;G01;Familia Garcia");
        processor.process("GROUP ADD;G01;ana@correo.com");
        assertFalse(processor.process("USER DELETE;ana@correo.com;Mala1!").success());
        assertEquals(1, groups.find("G01").orElseThrow().getMembers().size());
    }
}
