package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.amusementpark.business.controller.ApplicationInitializer;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.persistence.repository.UserRepository;

class AttractionTypeCommandsTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("GROUP CREATE;G01;F");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL");
        processor.process("ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL");
        processor.process("ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP");
        processor.process("ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
    }

    private AttractionType type(String id) {
        return ParkController.getInstance().getAttractions().findById(id).getType();
    }

    @Test
    void createsTypedAttractions() {
        assertEquals(AttractionType.GENERAL, type("A01"));
        assertEquals(AttractionType.INDIVIDUAL, type("A02"));
        assertEquals(AttractionType.GROUP, type("A03"));
        assertEquals(AttractionType.SPECIAL, type("A04"));
        assertEquals("ATTRACTION CREATED", processor.process("ATTRACTION CREATE;A05;Clasica;5"));
        assertEquals(AttractionType.GENERAL, type("A05"));
        assertTrue(processor.process("ATTRACTION CREATE;A06;X;10;VIP").startsWith("ERROR: Invalid attraction type"));
        assertNull(ParkController.getInstance().getAttractions().findById("A06"));
    }

    @Test
    void acceptanceMatrix() {
        assertEquals("ENTERED A01: ana@correo.com", processor.process("ATTRACTION ENTER;A01;T001"));
        assertEquals("ENTERED A01: pedro@correo.com", processor.process("ATTRACTION ENTER;A01;TG01"));
        assertEquals("ENTERED A02: ana@correo.com", processor.process("ATTRACTION ENTER;A02;T001"));
        assertEquals("ENTERED A03: pedro@correo.com", processor.process("ATTRACTION ENTER;A03;TG01"));
    }

    @Test
    void rejectsUnacceptedTicketTypes() {
        assertEquals("ERROR: Attraction A02 (INDIVIDUAL) does not accept this ticket type",
                processor.process("ATTRACTION ENTER;A02;TG01"));
        assertEquals("ERROR: Attraction A03 (GROUP) does not accept this ticket type",
                processor.process("ATTRACTION ENTER;A03;T001"));
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertEquals("No users in A02", processor.process("ATTRACTION LIST;A02"));
        assertEquals("No users in A03", processor.process("ATTRACTION LIST;A03"));
    }

    @Test
    void statusShowsType() {
        assertTrue(processor.process("ATTRACTION STATUS;A03").contains("\nType: GROUP\n"));
    }

    @Test
    void typePersistsAndUntypedLinesAreGeneral(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("attractions.txt"), "A01;Dragon Rojo;20\n");
        run(dir, "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL");
        run(dir);
        assertEquals(AttractionType.GENERAL, type("A01"));
        assertEquals(AttractionType.SPECIAL, type("A04"));
    }

    private static void run(Path dir, String... lines) {
        String input = String.join("\n", lines) + "\n";
        Main.run(new String[0], new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(new ByteArrayOutputStream()), new PrintStream(new ByteArrayOutputStream()), dir);
    }
}
