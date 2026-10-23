package es.upm.amusementpark.presentation;

import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_ANA;
import static es.upm.amusementpark.presentation.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.ApplicationContext;
import es.upm.amusementpark.TestParks;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.controller.Park;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroupCommandsTest {

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = TestParks.open(dir);
        processor = new CommandProcessor(park);
        run(CREATE_ANA);
        run(CREATE_PEDRO);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    private List<String> members(String groupId) {
        return park.findGroup(groupId).getMembers().stream().map(u -> u.getEmail()).toList();
    }

    @Test
    void createsEmptyGroup() {
        assertEquals("GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        Group group = park.findGroup("G01");
        assertNotNull(group);
        assertEquals("Familia Garcia", group.getName());
        assertTrue(group.isEmpty());
    }

    @Test
    void rejectsDuplicateOrEmptyGroup() {
        run("GROUP CREATE;G01;Familia Garcia");
        assertTrue(run("GROUP CREATE;G01;Otro").startsWith("ERROR"));
        assertEquals("Familia Garcia", park.findGroup("G01").getName());
        assertTrue(run("GROUP CREATE;;Nombre").startsWith("ERROR"));
        assertTrue(run("GROUP CREATE;G02; ").startsWith("ERROR"));
        assertEquals(1, park.getGroups().size());
    }

    @Test
    void addsAndRemovesMembers() {
        run("GROUP CREATE;G01;Familia");
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;ana@correo.com"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;PEDRO@correo.com"));
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), members("G01"));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G01;ana@correo.com"));
        assertEquals(List.of("pedro@correo.com"), members("G01"));
    }

    @Test
    void rejectsInvalidMembershipChanges() {
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP ADD;G99;ana@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP ADD;G01;nobody@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP REMOVE;G01;pedro@correo.com").startsWith("ERROR"));
        assertTrue(run("GROUP REMOVE;G99;ana@correo.com").startsWith("ERROR"));
        assertEquals(List.of("ana@correo.com"), members("G01"));
    }

    @Test
    void groupsSurviveRestart() throws IOException {
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP CREATE;G02;Vacia");
        assertEquals(List.of("G01;Familia Garcia;12345678Z;87654321X", "G02;Vacia"),
                Files.readAllLines(dir.resolve("groups.txt")));

        park = TestParks.open(dir);
        assertEquals(List.of("ana@correo.com", "pedro@correo.com"), members("G01"));
        assertTrue(park.findGroup("G02").isEmpty());
        assertTrue(park.findGroup("G01").getMembers().get(0) == park.findUserByEmail("ana@correo.com"));
    }

    @Test
    void failedSaveUndoesTheChange() {
        run("GROUP CREATE;G01;Familia");
        run("GROUP ADD;G01;ana@correo.com");
        assertTrue(dir.toFile().setWritable(false));
        try {
            assertTrue(run("GROUP CREATE;G02;Otro").startsWith("ERROR"));
            assertTrue(run("GROUP ADD;G01;pedro@correo.com").startsWith("ERROR"));
            assertTrue(run("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR"));
        } finally {
            assertTrue(dir.toFile().setWritable(true));
        }
        assertEquals(null, park.findGroup("G02"));
        assertEquals(List.of("ana@correo.com"), members("G01"));
    }

    @Test
    void malformedGroupLinesAreSkippedWithWarning() throws IOException {
        Files.write(dir.resolve("groups.txt"), List.of(
                "G01;Familia;12345678Z;99999999R;12345678Z",
                "broken",
                "G01;Duplicado",
                "",
                "G02;Otra;87654321X"));
        ByteArrayOutputStream warnings = new ByteArrayOutputStream();
        Park reopened = TestParks.start(ApplicationContext.fileRepositories(dir,
                new PrintStream(warnings, true, StandardCharsets.UTF_8)), LocalDate.of(2027, 4, 15));
        assertEquals(2, reopened.getGroups().size());
        assertEquals(1, reopened.findGroup("G01").getMembers().size());
        String text = warnings.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("line 1"));
        assertTrue(text.contains("line 2"));
        assertTrue(text.contains("line 3"));
    }

    @Test
    void helpListsGroupCommands() {
        String help = run("HELP");
        assertTrue(help.contains("GROUP CREATE;<identifier>;<name>"));
        assertTrue(help.contains("GROUP ADD;<identifier>;<email>"));
        assertTrue(help.contains("GROUP REMOVE;<identifier>;<email>"));
    }
}
