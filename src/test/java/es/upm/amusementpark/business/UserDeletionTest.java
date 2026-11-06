package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.App;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserDeletionTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n";

    @TempDir
    Path dir;

    private String run(String input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        App.run(new String[0], new BufferedReader(new StringReader(input)),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), dir);
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void correctCredentialsDeleteAndPersist() {
        String out = run(ANA + "USER DELETE;ana@correo.com;Clave1!\nEXIT\n");
        assertTrue(out.contains("USER DELETED"));
        run("EXIT\n");
        assertNull(ParkController.getInstance().findUser("ana@correo.com"));
    }

    @Test
    void wrongPasswordUnknownUserAndMalformed() {
        String out = run(ANA + "USER DELETE;ana@correo.com;Otra1!\nUSER DELETE;nadie@correo.com;Clave1!\n"
                + "USER DELETE;ana@correo.com\nEXIT\n");
        assertTrue(out.contains("ERROR: Incorrect password for user ana@correo.com"));
        assertTrue(out.contains("ERROR: User nadie@correo.com does not exist"));
        assertTrue(out.contains("ERROR: Invalid syntax"));
        assertNotNull(ParkController.getInstance().findUser("ana@correo.com"));
    }

    @Test
    void activeTicketBlocksDeletion() {
        String out = run(ANA + "TICKET CREATE;T001;ana@correo.com;" + LocalDate.now()
                + "\nUSER DELETE;ana@correo.com;Clave1!\nEXIT\n");
        assertTrue(out.contains("has active tickets and cannot be deleted"));
        assertNotNull(ParkController.getInstance().findUser("ana@correo.com"));
    }

    @Test
    void removedFromGroupsAndHistoryKept() {
        ParkController park = ParkController.getInstance();
        park.reset();
        park.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", park.getCurrentDate());
        park.setCurrentDate(park.getCurrentDate().plusDays(1));
        park.deleteUser("ana@correo.com", "Clave1!");
        assertNull(park.findUser("ana@correo.com"));
        assertFalse(park.findGroup("G01").hasMember("ana@correo.com"));
        assertNotNull(park.findTicket("T001"));
    }
}
