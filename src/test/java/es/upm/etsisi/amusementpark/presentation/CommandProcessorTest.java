package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.controller.GroupController;
import es.upm.etsisi.amusementpark.business.controller.TicketController;
import es.upm.etsisi.amusementpark.business.controller.UserController;
import es.upm.etsisi.amusementpark.persistence.AttractionFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupFileStorage;
import es.upm.etsisi.amusementpark.persistence.GroupTicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.TicketFileStorage;
import es.upm.etsisi.amusementpark.persistence.UserFileStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CommandProcessorTest {


    @TempDir
    Path dir;
    UserController service;
    CommandProcessor processor;

    @BeforeEach
    void setUp() {
        service = UserController.initialize(new UserFileStorage(dir.resolve("users.txt")), TestData.HASHER);
        GroupController groups = GroupController.initialize(new GroupFileStorage(dir.resolve("groups.txt")), service);
        processor = TestData.processor(ParkController.initialize(service, groups, TicketController.initialize(new TicketFileStorage(dir.resolve("tickets.txt")),
                new GroupTicketFileStorage(dir.resolve("grouptickets.txt")), service, groups),
                AttractionController.initialize(new AttractionFileStorage(dir.resolve("attractions.txt")))));
    }

    @Test
    void createDeleteAndUpdateCommands() {
        assertTrue(processor.process(TestData.CREATE_ANA).startsWith("OK:"));
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria").startsWith("OK:"));
        assertEquals("Ana Maria", service.getUsers().get(0).getName());
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com").startsWith("OK:"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#").startsWith("OK:"));
        assertTrue(processor.process("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666").startsWith("OK:"));
        assertEquals("9999888877776666", service.getUsers().get(0).getCard());
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Clave1!").startsWith("ERROR:"));
        assertTrue(processor.process("USER DELETE;ana.nueva@correo.com;Nueva2#").startsWith("OK:"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void duplicateCreationRejected() {
        processor.process(TestData.CREATE_ANA);
        assertTrue(processor.process(
                "USER CREATE;Otra;X;12345678Z;otro@correo.com;Clave1!;6;1234567890123456").startsWith("ERROR:"));
        assertTrue(processor.process(
                "USER CREATE;Otra;X;87654321X;ana@correo.com;Clave1!;6;1234567890123456").startsWith("ERROR:"));
        assertEquals(1, service.getUsers().size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "USER RENAME;ana@correo.com",
        "USER",
        "HELLO WORLD",
        "USER CREATE;Ana;Garcia Lopez",
        "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456;extra",
        "USER DELETE;ana@correo.com",
        "USER UPDATE;ana@correo.com;Clave1!;NAME",
        "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X",
        "USER CREATE;Ana;Garcia Lopez;12345678A;ana@correo.com;Clave1!;612345678;1234567890123456",
        ";;;;"
    })
    void invalidCommandsAreReportedWithoutSideEffects(String command) {
        assertTrue(processor.process(command).startsWith("ERROR:"));
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    void dniCannotBeUpdated() {
        processor.process(TestData.CREATE_ANA);
        assertTrue(processor.process("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").startsWith("ERROR:"));
        assertEquals("12345678Z", service.getUsers().get(0).getDni());
    }

    @Test
    void helpListsAllCommandsAndChangesNothing() {
        processor.process(TestData.CREATE_ANA);
        String help = processor.process("HELP");
        for (String syntax : new String[] {
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña", "USER UPDATE;Correo;Contraseña;Campo;NuevoValor", "HELP", "EXIT"}) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertEquals(1, service.getUsers().size());
        assertEquals(false, processor.isExitRequested());
    }

    @Test
    void exitSetsTheExitFlag() {
        assertEquals(false, processor.isExitRequested());
        assertEquals("OK: Bye", processor.process(" exit "));
        assertTrue(processor.isExitRequested());
    }

    @Test
    void generalCommandsRejectParameters() {
        assertTrue(processor.process("EXIT;now").startsWith("ERROR:"));
        assertTrue(processor.process("HELP;USER").startsWith("ERROR:"));
        assertEquals(false, processor.isExitRequested());
    }
}
