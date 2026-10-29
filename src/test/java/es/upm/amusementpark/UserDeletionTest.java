package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserDeletionTest {

    private static final String USERS =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
            + "USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654\n";

    private final ParkController park = ParkController.getInstance();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final CommandProcessor processor =
            new CommandProcessor(new PrintStream(buffer, true, StandardCharsets.UTF_8));

    @TempDir
    Path base;

    @BeforeEach
    void setUp() {
        park.reset();
    }

    private String run(String input) throws IOException {
        processor.run(new BufferedReader(new StringReader(input)));
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void sourceExampleAndRejections() throws IOException {
        String out = run(USERS + "USER DELETE;ana@correo.com;clave1!\nUSER DELETE;nadie@correo.com;Clave1!\n"
                + "USER DELETE;ana@correo.com\nUSER DELETE;ana@correo.com;Clave1!\n");
        assertTrue(out.contains("Error: invalid password for user ana@correo.com"));
        assertTrue(out.contains("Error: user not found: nadie@correo.com"));
        assertTrue(out.contains("Error: invalid arguments for USER DELETE"));
        assertTrue(out.contains("USER DELETED"));
        assertNull(park.findUser("ana@correo.com"));
        assertNotNull(park.findUser("pedro@correo.com"));
    }

    @Test
    void activeTicketBlocksDeletion() throws IOException {
        String out = run(USERS + "SYSTEM DATE;2027-04-15\nTICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "USER DELETE;ana@correo.com;Clave1!\n");
        assertTrue(out.contains("has an active ticket or belongs to a group with an active group ticket"));
        assertNotNull(park.findUser("ana@correo.com"));
    }

    @Test
    void memberOfLockedGroupCannotBeDeleted() throws IOException {
        run(USERS + "SYSTEM DATE;2027-04-15\nGROUP CREATE;G01;F\nGROUP ADD;G01;ana@correo.com\n"
                + "GROUPTICKET CREATE;TG01;G01;2027-04-20\nSYSTEM DATE;2027-04-16\n");
        park.setCurrentDate("2027-04-21");
        park.addUserToGroup("G01", "pedro@correo.com");
        park.setCurrentDate("2027-04-16");
        String out = run("USER DELETE;pedro@correo.com;Otra2#\n");
        assertTrue(out.contains("Error:"));
        assertNotNull(park.findUser("pedro@correo.com"));
    }

    private String app(String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(new String[0], base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void deletionPersistedAndHistoryKept() {
        app(USERS + "SYSTEM DATE;2027-04-15\nGROUP CREATE;G01;F\nGROUP ADD;G01;ana@correo.com\n"
                + "GROUP ADD;G01;pedro@correo.com\nGROUPTICKET CREATE;TG01;G01;2027-04-15\nEXIT\n");
        String out = app("SYSTEM DATE;2027-04-16\nUSER DELETE;pedro@correo.com;Otra2#\nEXIT\n");
        assertTrue(out.contains("USER DELETED"), out);
        app("EXIT\n");
        assertNull(park.findUser("pedro@correo.com"));
        assertEquals(1, park.findGroup("G01").getMembers().size());
        GroupTicket tg01 = (GroupTicket) park.findTicket("TG01");
        IndividualTicket pedroTicket = tg01.getComposition().get(1);
        assertEquals("pedro@correo.com", pedroTicket.getUser().getEmail());
        assertEquals("87654321X", pedroTicket.getUser().getDni());
        assertFalse(park.findGroup("G01").hasMember(pedroTicket.getUser()));
    }
}
