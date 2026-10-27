package es.upm.amusementpark.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.repository.UserRepository;

class CommandProcessorTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private UserRepository users;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        processor = new CommandProcessor(users);
    }

    private static String create(String dni, String email, String password, String card) {
        return "USER CREATE;Ana;Garcia Lopez;" + dni + ";" + email + ";" + password + ";612345678;" + card;
    }

    @Test
    void createsValidUser() {
        assertEquals("USER CREATED", processor.process(ANA));
        assertEquals(1, users.size());
        assertNotNull(users.findByDni("12345678Z"));
        assertEquals("612345678", users.findByEmail("ana@correo.com").getPhone());
    }

    @Test
    void rejectsMissingOrEmptyFields() {
        assertTrue(processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z").startsWith("ERROR"));
        assertTrue(processor.process(create("12345678Z", "ana@correo.com", "Clave1!", "1234567890123456") + ";extra")
                .startsWith("ERROR"));
        assertTrue(processor.process("USER CREATE;;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456")
                .startsWith("ERROR"));
        assertEquals(0, users.size());
    }

    @Test
    void rejectsInvalidData() {
        assertEquals("ERROR: Invalid DNI", processor.process(create("12345678A", "ana@correo.com", "Clave1!", "1234567890123456")));
        assertEquals("ERROR: Invalid email", processor.process(create("12345678Z", "ana@correo.es", "Clave1!", "1234567890123456")));
        assertEquals("ERROR: Invalid password", processor.process(create("12345678Z", "ana@correo.com", "Clave12", "1234567890123456")));
        assertEquals("ERROR: Invalid card number", processor.process(create("12345678Z", "ana@correo.com", "Clave1!", "1234 5678 9012 3456")));
        assertEquals(0, users.size());
    }

    @Test
    void rejectsDuplicateDni() {
        processor.process(ANA);
        assertEquals("ERROR: DNI already registered", processor.process(create("12345678Z", "otra@correo.com", "Clave1!", "1234567890123456")));
        assertEquals(1, users.size());
    }

    @Test
    void rejectsDuplicateEmail() {
        processor.process(ANA);
        assertEquals("ERROR: Email already registered", processor.process(create("87654321X", "ana@correo.com", "Clave1!", "1234567890123456")));
        assertEquals(1, users.size());
    }

    @Test
    void unknownCommandsAreReportedWithoutFailing() {
        assertEquals("ERROR: Unknown command", processor.process("FOO;bar"));
        assertEquals("ERROR: Unknown command", processor.process("user create;x"));
        assertEquals("USER CREATED", processor.process(ANA));
    }

    @Test
    void deletesUserWithCorrectPassword() {
        processor.process(ANA);
        assertEquals("USER DELETED", processor.process("USER DELETE;ana@correo.com;Clave1!"));
        assertEquals(0, users.size());
        assertEquals(null, users.findByDni("12345678Z"));
    }

    @Test
    void deleteRejectsUnknownUserWrongPasswordAndBadFormat() {
        processor.process(ANA);
        assertEquals("ERROR: User not found", processor.process("USER DELETE;nadie@correo.com;Clave1!"));
        assertEquals("ERROR: Incorrect password", processor.process("USER DELETE;ana@correo.com;Otra1!"));
        assertTrue(processor.process("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertTrue(processor.process("USER DELETE;ana@correo.com;").startsWith("ERROR"));
        assertEquals(1, users.size());
    }
}
