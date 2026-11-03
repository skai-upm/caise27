package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttractionCommandsTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(CommandProcessorTest.CREATE_ANA);
        p.process(AppTest.CREATE_PEDRO);
        p.process(GroupTicketCommandsTest.CREATE_LUIS);
        p.process("GROUP CREATE;G01;Amigos");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("GROUP ADD;G01;luis@correo.com");
        p.process("SYSTEM DATE;2026-10-15");
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(p.process("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK:"));
    }

    private List<String> status(String id) {
        return p.process("ATTRACTION STATUS;" + id).lines().toList();
    }

    @Test
    void sourceSequence() {
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION ENTER;A01;TG01").startsWith("OK:"));
        List<String> s = status("A01");
        assertEquals("Attraction A01 - Tornado", s.get(0));
        assertEquals("Capacity: 20", s.get(1));
        assertEquals("Users present: 3", s.get(2));
        assertEquals("Available places: 17", s.get(3));
        assertTrue(s.get(4).contains("ana@correo.com") && s.get(4).contains("pedro@correo.com")
                && s.get(4).contains("luis@correo.com"));
        assertTrue(p.process("ATTRACTION EXIT;A01;T001").startsWith("OK:"));
        s = status("A01");
        assertEquals("Users present: 2", s.get(2));
        assertEquals("Available places: 18", s.get(3));
        assertTrue(!s.get(4).contains("ana@correo.com"));
    }

    @Test
    void groupDoesNotFitWhenTooFewPlacesRemain() {
        p.process("ATTRACTION CREATE;A02;Mini;3");
        p.process("USER CREATE;Eva;Diaz;22222222J;eva@correo.com;Clave4%;600;2222333344445555");
        p.process("USER CREATE;Leo;Ruiz;33333333P;leo@correo.com;Clave5&;600;3333444455556666");
        p.process("GROUP CREATE;G02;Cuatro");
        for (String e : new String[] {"ana", "pedro", "eva", "leo"}) {
            p.process("GROUP ADD;G02;" + e + "@correo.com");
        }
        p.process("GROUPTICKET CREATE;TG02;G02;2026-10-15");
        assertTrue(p.process("ATTRACTION ENTER;A02;TG02").startsWith("ERROR:"));
        assertEquals("Users present: 0", status("A02").get(2));
        assertTrue(p.process("ATTRACTION ENTER;A02;TG01").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION ENTER;A02;T001").startsWith("OK:"));
        assertEquals("Available places: 0", status("A02").get(3));
    }

    @Test
    void ticketMustBeValidToday() {
        p.process("TICKET CREATE;T002;ana@correo.com;2026-10-16");
        assertTrue(p.process("ATTRACTION ENTER;A01;T002").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR:"));
        assertTrue(p.process("ATTRACTION ENTER;A01;T002").startsWith("OK:"));
    }

    @Test
    void exitIsNotDateCheckedAndAcceptsEmail() {
        p.process("ATTRACTION ENTER;A01;T001");
        p.process("ATTRACTION ENTER;A01;TG01");
        p.process("SYSTEM DATE;2026-10-20");
        assertTrue(p.process("ATTRACTION EXIT;A01;TG01").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION EXIT;A01;ana@correo.com").startsWith("OK:"));
        assertEquals("Users present: 0", status("A01").get(2));
        assertTrue(p.process("ATTRACTION EXIT;A01;T001").startsWith("ERROR:"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION ENTER;A99;T001", "ATTRACTION ENTER;A01;T999", "ATTRACTION ENTER;A01;TG01-1",
        "ATTRACTION ENTER;A01", "ATTRACTION EXIT;A01;T001", "ATTRACTION EXIT;A99;T001", "ATTRACTION EXIT;A01;nadie",
        "ATTRACTION STATUS;A99", "ATTRACTION STATUS", "ATTRACTION CREATE;A01;Otra;5", "ATTRACTION CREATE;A03;X;0"})
    void rejectedCommands(String command) {
        assertTrue(p.process(command).startsWith("ERROR:"), command);
        assertEquals("Users present: 0", status("A01").get(2));
    }

    @Test
    void helpListsNineteenCommands() {
        String help = p.process("HELP");
        assertEquals(19, CommandProcessor.COMMAND_SYNTAX.size());
        for (String syntax : CommandProcessor.COMMAND_SYNTAX) {
            assertTrue(help.contains(syntax), syntax);
        }
    }
}
