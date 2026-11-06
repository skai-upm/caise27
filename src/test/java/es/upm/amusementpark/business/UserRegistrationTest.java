package es.upm.amusementpark.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.App;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRegistrationTest {

    private ParkController park;

    @BeforeEach
    void setUp() {
        park = ParkController.getInstance();
        park.reset();
    }

    private User ana() {
        return park.registerUser("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678",
                "1234567890123456");
    }

    @Test
    void validUserRegisteredWithProtectedPassword() {
        User user = ana();
        assertEquals(user, park.findUser("ana@correo.com"));
        assertNotEquals("Clave1!", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));
        assertTrue(park.verifyPassword("Clave1!", user.getPassword()));
    }

    @Test
    void invalidDataRejected() {
        assertThrows(ParkException.class, () -> park.registerUser("Ana", "G", "12345678A", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> park.registerUser("Ana", "G", "12345678Z", "ana@correo.es",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> park.registerUser("Ana", "G", "12345678Z", "ana@correo.com",
                "clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> park.registerUser("Ana", "G", "12345678Z", "ana@correo.com",
                "Clave1!", "6", "123"));
        assertTrue(park.getUsers().isEmpty());
    }

    @Test
    void duplicatesRejected() {
        ana();
        assertThrows(ParkException.class, () -> park.registerUser("Otra", "X", "12345678Z", "otra@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertThrows(ParkException.class, () -> park.registerUser("Otra", "X", "87654321X", "ana@correo.com",
                "Clave1!", "6", "1234567890123456"));
        assertEquals(1, park.getUsers().size());
        assertNull(park.findUser("otra@correo.com"));
    }

    @Test
    void registeredUserUsableByParkOperations() {
        ana();
        park.createIndividualTicket("T001", "ana@correo.com", park.getCurrentDate());
        park.createGroup("G01", "F");
        park.addUserToGroup("G01", "ana@correo.com");
        park.createGroupTicket("TG01", "G01", park.getCurrentDate());
        assertTrue(park.findTicket("TG01:12345678Z") != null);
    }

    @Test
    void commandCreatesUserAndReportsErrors() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        CommandProcessor processor = new CommandProcessor(out);
        App.registerCommands(processor, park, out);
        processor.execute("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.execute("USER CREATE;Pedro;Lopez;12345678Z;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.execute("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;623456789;123");
        processor.execute("USER CREATE;Ana;Garcia Lopez;12345678Z");
        processor.execute("HELP");
        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("USER CREATED"));
        assertTrue(text.contains("ERROR: User with DNI 12345678Z already exists"));
        assertTrue(text.contains("ERROR: Invalid card number"));
        assertTrue(text.contains("ERROR: Invalid syntax"));
        assertTrue(text.contains("Available commands:"));
        assertEquals(1, park.getUsers().size());
    }
}
