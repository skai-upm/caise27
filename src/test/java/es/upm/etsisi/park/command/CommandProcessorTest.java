package es.upm.etsisi.park.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.park.controller.ParkController;
import es.upm.etsisi.park.persistence.ParkStorage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CommandProcessorTest {

    private static final String CREATE_ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";
    private static final String CREATE_PEDRO =
            "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654";
    private static final String CREATE_LAURA =
            "USER CREATE;Laura;Martin Ruiz;11111111H;laura@correo.com;Laura3$;634567890;1111222233334444";

    @TempDir
    Path dir;

    private ParkController park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.configure(ParkStorage.inDirectory(dir, new PrintStream(new ByteArrayOutputStream())));
        processor = new CommandProcessor(park);
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void fullUserLifecycle() {
        assertEquals("USER CREATED", run(CREATE_ANA));
        assertEquals("USER CREATED", run(CREATE_PEDRO));
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999"));
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER DELETED", run("USER DELETE;pedro@correo.com;Otra2#"));
        assertTrue(park.users().findByEmail("ana.nueva@correo.com").isPresent());
        assertFalse(park.users().findByEmail("pedro@correo.com").isPresent());
    }

    @Test
    void keywordsAreCaseInsensitiveAndTrimmed() {
        assertEquals("USER CREATED", run(
                "  user   create ; Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456"));
        assertTrue(run("help").contains("USER DELETE"));
        assertEquals("ATTRACTION CREATED", run("attraction create;A01;Dragon Rojo;20"));
        assertTrue(run("attraction status;A01").contains("A01"));
    }

    @Test
    void unknownCommandIsReported() {
        CommandResult result = processor.process("FOO BAR;1;2");
        assertTrue(result.output().startsWith("ERROR"));
        assertTrue(result.output().contains("Unknown command"));
        assertFalse(result.exit());
    }

    @Test
    void wrongArgumentCountShowsUsage() {
        String output = run("USER DELETE;ana@correo.com");
        assertTrue(output.startsWith("ERROR"));
        assertTrue(output.contains("USER DELETE;<Email>;<Password>"));
        assertTrue(run("USER CREATE;Ana").startsWith("ERROR"));
        assertTrue(run("USER UPDATE;a;b;c").startsWith("ERROR"));
        assertTrue(run("GROUP ADD;G01").startsWith("ERROR"));
        assertTrue(run("ATTRACTION ENTER;A01").startsWith("ERROR"));
    }

    @Test
    void rejectedOperationsAreErrors() {
        run(CREATE_ANA);
        assertTrue(run(CREATE_ANA).startsWith("ERROR"));
        assertTrue(run("USER DELETE;ana@correo.com;Mala1!").startsWith("ERROR"));
        assertTrue(run("USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X").contains("not modifiable"));
        assertTrue(run("USER CREATE;Ana;Garcia Lopez;12345678A;x@correo.com;Clave1!;612345678;1234567890123456")
                .contains("DNI"));
    }

    @Test
    void helpListsAllCommands() {
        String help = run("HELP");
        for (String syntax : CommandProcessor.ALL_SYNTAX) {
            assertTrue(help.contains(syntax), syntax);
        }
        assertTrue(help.contains("GROUPTICKET CREATE;<identifier>;<group>;<date>"));
        assertTrue(help.contains("ATTRACTION STATUS;<attractionIdentifier>"));
        assertTrue(help.contains("SYSTEM DATE;<yyyy-MM-dd>"));
    }

    @Test
    void exitRequestsTermination() {
        assertTrue(processor.process("EXIT").exit());
        assertFalse(processor.process("HELP").exit());
    }

    @Test
    void blankLineProducesNoOutput() {
        assertEquals("", run("   "));
        assertEquals("", processor.process(null).output());
    }

    @Test
    void systemSettingsMessages() {
        assertEquals("SYSTEM DATE SET TO 2027-04-15", run("SYSTEM DATE;2027-04-15"));
        assertEquals("SYSTEM PRICE SET TO 45.50", run("SYSTEM PRICE;45.50"));
        assertTrue(run("SYSTEM DATE;2027-13-45").startsWith("ERROR"));
        assertTrue(run("SYSTEM PRICE;abc").startsWith("ERROR"));
    }

    @Test
    void assignmentExampleFlow() {
        run("SYSTEM DATE;2027-04-15");
        run("SYSTEM PRICE;40.00");
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run(CREATE_LAURA);
        assertEquals("USER CREATED",
                run("USER CREATE;Marta;Sanchez Perez;00000000T;marta@correo.com;Marta4%;645678901;2222333344445555"));
        assertEquals("GROUP CREATED", run("GROUP CREATE;G01;Familia Garcia"));
        assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G01;ana@correo.com"));
        run("GROUP ADD;G01;pedro@correo.com");
        run("GROUP ADD;G01;laura@correo.com");
        run("ATTRACTION CREATE;A01;Dragon Rojo;20");
        run("ATTRACTION CREATE;A02;Torre del Cielo;12");
        assertEquals("GROUP TICKET CREATED. MEMBERS: 3, DISCOUNT: 10%, TOTAL PRICE: 108.00 EUR",
                run("GROUPTICKET CREATE;TG01;G01;2027-04-15"));
        assertEquals("3 USER(S) ENTERED A01", run("ATTRACTION ENTER;A01;TG01"));
        String list = run("ATTRACTION LIST;A01");
        assertTrue(list.contains("ana@correo.com") && list.contains("pedro@correo.com")
                && list.contains("laura@correo.com"));
        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("ATTRACTION: A01"));
        assertTrue(status.contains("NAME: Dragon Rojo"));
        assertTrue(status.contains("CAPACITY: 20"));
        assertTrue(status.contains("USERS INSIDE: 3"));
        assertTrue(status.contains("AVAILABLE PLACES: 17"));
        assertEquals("USER LEFT A01", run("ATTRACTION EXIT;A01;ana@correo.com"));
        run("ATTRACTION EXIT;A01;pedro@correo.com");
        run("ATTRACTION EXIT;A01;laura@correo.com");
        assertTrue(run("ATTRACTION STATUS;A01").contains("USERS INSIDE: 0"));
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        assertEquals("1 USER(S) ENTERED A02", run("ATTRACTION ENTER;A02;T001"));
        assertEquals("ana@correo.com", run("ATTRACTION LIST;A02"));
        run("ATTRACTION EXIT;A02;ana@correo.com");
        assertEquals("NO USERS IN A02", run("ATTRACTION LIST;A02"));
        run("SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED. PRICE: 45.50 EUR", run("TICKET CREATE;T002;marta@correo.com;2027-04-20"));
        assertTrue(run("USER DELETE;marta@correo.com;Marta4%").startsWith("ERROR"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T002"));
        assertEquals("USER DELETED", run("USER DELETE;marta@correo.com;Marta4%"));
        run("GROUP CREATE;G02;Visita Mayo");
        run("GROUP ADD;G02;pedro@correo.com");
        run("GROUP ADD;G02;laura@correo.com");
        assertEquals("GROUP TICKET CREATED. MEMBERS: 2, DISCOUNT: 0%, TOTAL PRICE: 91.00 EUR",
                run("GROUPTICKET CREATE;TG02;G02;2027-04-20"));
        assertTrue(run("GROUP REMOVE;G02;laura@correo.com").startsWith("ERROR"));
        assertEquals("GROUP TICKET DELETED", run("GROUPTICKET DELETE;TG02"));
        assertEquals("USER REMOVED FROM GROUP", run("GROUP REMOVE;G02;laura@correo.com"));
        assertEquals("GROUP DELETED", run("GROUP DELETE;G02"));
    }
}
