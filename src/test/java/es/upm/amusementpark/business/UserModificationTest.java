package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.App;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserModificationTest {

    private ParkController park;

    @TempDir
    Path dir;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
        park.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
        park.registerUser("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "623456789",
                "9876543210987654");
    }

    @Test
    void simpleFieldsAndPassword() {
        park.updateUser("ana@correo.com", "Clave1!", UserField.NAME, "Ana Maria");
        park.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "9999888877776666");
        park.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "Nueva2#");
        User ana = park.findUser("ana@correo.com");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("9999888877776666", ana.getCard());
        assertTrue(park.verifyPassword("Nueva2#", ana.getPassword()));
        assertFalse(park.verifyPassword("Clave1!", ana.getPassword()));
        assertEquals("12345678Z", ana.getDni());
    }

    @Test
    void invalidValuesAndCredentialsRejected() {
        assertThrows(ParkException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana@correo.es"));
        assertThrows(ParkException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "pedro@correo.com"));
        assertThrows(ParkException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.PASSWORD, "nueva2#"));
        assertThrows(ParkException.class,
                () -> park.updateUser("ana@correo.com", "Clave1!", UserField.CARD, "123"));
        assertThrows(ParkException.class,
                () -> park.updateUser("ana@correo.com", "Mala1!", UserField.PHONE, "699999999"));
        assertThrows(ParkException.class,
                () -> park.updateUser("nadie@correo.com", "Clave1!", UserField.PHONE, "699999999"));
        User ana = park.findUser("ana@correo.com");
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
        assertTrue(park.verifyPassword("Clave1!", ana.getPassword()));
    }

    @Test
    void emailChangeKeepsRelationships() {
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createIndividualTicket("T001", "ana@correo.com", park.getCurrentDate());
        park.createAttraction("A01", "Dragon", 10);
        park.enterAttraction("A01", "T001");
        park.updateUser("ana@correo.com", "Clave1!", UserField.EMAIL, "ana.nueva@correo.com");
        assertNull(park.findUser("ana@correo.com"));
        assertEquals("12345678Z", park.findUser("ana.nueva@correo.com").getDni());
        assertTrue(park.findGroup("G01").hasMember("ana.nueva@correo.com"));
        assertEquals("ana.nueva@correo.com", ((IndividualTicket) park.findTicket("T001")).getUserEmail());
        assertTrue(park.findAttraction("A01").contains("ana.nueva@correo.com"));
        assertEquals(1, park.createUserInvoice("ana.nueva@correo.com").sections().size());
    }

    @Test
    void commandRejectsDniAndPersistsChanges() {
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        App.run(new String[0], new BufferedReader(new StringReader(
                "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n"
                + "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X\n"
                + "USER UPDATE;ana@correo.com;Clave1!;AGE;30\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE\nEXIT\n")),
                new PrintStream(first, true, StandardCharsets.UTF_8), dir);
        String text = first.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("USER UPDATED"));
        assertTrue(text.contains("ERROR: DNI cannot be modified"));
        assertTrue(text.contains("ERROR: Invalid field"));
        assertTrue(text.contains("ERROR: Invalid syntax"));
        App.run(new String[0], new BufferedReader(new StringReader("EXIT\n")),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8), dir);
        assertEquals("699999999", ParkController.getInstance().findUser("ana@correo.com").getPhone());
        assertEquals("12345678Z", ParkController.getInstance().findUser("ana@correo.com").getDni());
    }
}
