package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupCommandsTest {

    private ByteArrayOutputStream buffer;
    private CommandProcessor processor;
    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.setUserDirectory(email -> Set.of("ana@correo.com").contains(email));
        buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        processor = new CommandProcessor(out);
        GroupCommands.register(processor, park, out);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullLifecycle() {
        processor.execute("GROUP CREATE;G01;Familia Garcia");
        processor.execute("GROUP ADD;G01;ana@correo.com");
        processor.execute("GROUP REMOVE;G01;ana@correo.com");
        processor.execute("GROUP DELETE;G01");
        String out = output();
        assertTrue(out.contains("GROUP CREATED"));
        assertTrue(out.contains("USER ADDED TO GROUP"));
        assertTrue(out.contains("USER REMOVED FROM GROUP"));
        assertTrue(out.contains("GROUP DELETED"));
        assertNull(park.findGroup("G01"));
    }

    @Test
    void duplicateAddReportsError() {
        processor.execute("GROUP CREATE;G01;Familia Garcia");
        processor.execute("GROUP ADD;G01;ana@correo.com");
        processor.execute("GROUP ADD;G01;ana@correo.com");
        assertTrue(output().contains("ERROR: User ana@correo.com already belongs to group G01"));
        assertEquals(1, park.findGroup("G01").getMembers().size());
    }

    @Test
    void malformedCommandsReportUsage() {
        processor.execute("GROUP CREATE;G01");
        processor.execute("GROUP DELETE");
        processor.execute("GROUP ADD;G01;a;b");
        assertEquals(3, output().split("ERROR: Invalid syntax", -1).length - 1);
        assertNull(park.findGroup("G01"));
    }

    @Test
    void malformedAndRefusedCommandsDoNotStopProcessing() throws java.io.IOException {
        processor.execute("GROUP CREATE;G01;Familia Garcia");
        processor.run(new java.io.BufferedReader(new java.io.StringReader("GROUP CREATE;G01\nGROUP CREATE;G01;Otra\nHELP")));
        assertTrue(output().contains("ERROR: Invalid syntax. Usage: GROUP CREATE;<Identifier>;<Name>"));
        assertTrue(output().contains("ERROR: Group G01 already exists"));
        assertTrue(output().contains("Available commands:"));
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
    }

    @Test
    void helpListsGroupCommands() {
        processor.execute("HELP");
        assertTrue(output().contains("GROUP CREATE;<Identifier>;<Name>"));
        assertTrue(output().contains("GROUP DELETE;<Identifier>"));
    }
}
