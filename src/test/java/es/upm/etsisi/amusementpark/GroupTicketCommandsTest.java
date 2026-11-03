package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GroupTicketCommandsTest {

    static final String CREATE_LUIS =
            "USER CREATE;Luis;Perez;11111111H;luis@correo.com;Clave3$;600000000;1111222233334444";

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(CommandProcessorTest.CREATE_ANA);
        p.process(AppTest.CREATE_PEDRO);
        p.process(CREATE_LUIS);
        p.process("GROUP CREATE;G01;Familia Garcia");
        p.process("GROUP ADD;G01;ana@correo.com");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("SYSTEM DATE;2026-10-01");
    }

    @Test
    void purchaseCreatesOneTicketPerMember() throws IOException {
        assertEquals("OK GROUP TICKET CREATED -> 80.00", p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        assertEquals(List.of("TG01;G01;2026-10-15;80.00;40.00;12345678Z,87654321X"),
                Files.readAllLines(dir.resolve("grouptickets.txt")));
    }

    @Test
    void threeMembersGetTenPercent() {
        p.process("GROUP ADD;G01;luis@correo.com");
        assertEquals("OK GROUP TICKET CREATED -> 108.00", p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15"));
        p.process("SYSTEM PRICE;60.00");
        assertEquals("OK GROUP TICKET CREATED -> 162.00", p.process("GROUPTICKET CREATE;TG02;G01;2026-10-16"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GROUPTICKET CREATE;TG09;G99;2026-10-15", "GROUPTICKET CREATE;TG09;G02;2026-10-15",
        "GROUPTICKET CREATE;TG09;G01;2026-09-30", "GROUPTICKET CREATE;TG09;G01;2026-02-30",
        "GROUPTICKET CREATE;TG09;G01", "GROUPTICKET CREATE;T001;G01;2026-10-15",
        "GROUPTICKET CREATE;TG01;G01;2026-10-15", "GROUPTICKET DELETE", "GROUPTICKET DELETE;TG99"})
    void rejectedCommands(String command) {
        p.process("GROUP CREATE;G02;Vacio");
        p.process("TICKET CREATE;T001;luis@correo.com;2026-10-15");
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-20");
        assertTrue(p.process(command).startsWith("ERROR:"), command);
        assertTrue(p.process("GROUPTICKET DELETE;TG09").startsWith("ERROR:"));
    }

    @Test
    void individualTicketCannotReuseAConstituentId() {
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(p.process("TICKET CREATE;TG01-1;luis@correo.com;2026-10-15").startsWith("ERROR:"));
        assertTrue(p.process("TICKET DELETE;TG01-1").startsWith("ERROR:"));
    }

    @Test
    void groupIsLockedUntilTicketsArePast() {
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(p.process("GROUP ADD;G01;luis@correo.com").startsWith("ERROR:"));
        assertTrue(p.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR:"));
        assertTrue(p.process("GROUP DELETE;G01").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("GROUP REMOVE;G01;ana@correo.com").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("GROUP ADD;G01;luis@correo.com").startsWith("OK:"));
        assertTrue(p.process("GROUP REMOVE;G01;ana@correo.com").startsWith("OK:"));
        assertTrue(p.process("GROUP DELETE;G01").startsWith("OK:"));
    }

    @Test
    void membersCannotBeDeletedWhileGroupTicketIsValid() {
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR:"));
        assertTrue(p.process("USER DELETE;luis@correo.com;Clave3$").startsWith("OK:"));
        p.process("SYSTEM DATE;2026-10-16");
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK:"));
    }

    @Test
    void deletionOnlyForFutureTicketsAndCascades() {
        p.process("GROUPTICKET CREATE;TG001;G01;2026-10-15");
        p.process("SYSTEM DATE;2026-10-15");
        assertTrue(p.process("GROUPTICKET DELETE;TG001").startsWith("ERROR:"));
        p.process("SYSTEM DATE;2026-10-14");
        assertEquals("OK: Group ticket TG001 deleted", p.process("GROUPTICKET DELETE;TG001"));
        assertTrue(p.process("USER DELETE;ana@correo.com;Clave1!").startsWith("OK:"));
        assertTrue(p.process("GROUP ADD;G01;luis@correo.com").startsWith("OK:"));
    }

    @Test
    void helpListsFifteenCommands() {
        String help = p.process("HELP");
        assertTrue(CommandProcessor.COMMAND_SYNTAX.size() >= 15);
        assertTrue(help.contains("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd"));
        assertTrue(help.contains("GROUPTICKET DELETE;Id"));
    }
}
