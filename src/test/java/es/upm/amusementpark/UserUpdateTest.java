package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.InvoiceController;
import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.presentation.CommandProcessor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserUpdateTest {

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

    private static int errors(String out) {
        return out.split("Error:", -1).length - 1;
    }

    @Test
    void enumHoldsExactlyTheModifiableFields() {
        assertEquals(List.of("NAME", "SURNAME", "EMAIL", "PASSWORD", "PHONE", "CARD"),
                Arrays.stream(UserField.values()).map(Enum::name).toList());
    }

    @Test
    void sourceExamples() throws IOException {
        String out = run(USERS + "USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;Nueva2#\n"
                + "USER UPDATE;ana@correo.com;Nueva2#;CARD;9999888877776666\n"
                + "USER UPDATE;ana@correo.com;Nueva2#;EMAIL;ana.nueva@correo.com\n");
        assertEquals(4, out.split("USER UPDATED", -1).length - 1);
        assertEquals(0, errors(out));
        assertNull(park.findUser("ana@correo.com"));
        User ana = park.findUser("ana.nueva@correo.com");
        assertEquals("Ana Maria", ana.getFirstName());
        assertEquals("9999888877776666", ana.getCard());
        assertEquals("12345678Z", ana.getDni());
        park.verifyPassword("ana.nueva@correo.com", "Nueva2#");
    }

    @Test
    void rejectedUpdatesChangeNothing() throws IOException {
        String out = run(USERS + "USER UPDATE;ana@correo.com;Clave1!;DNI;87654321X\n"
                + "USER UPDATE;ana@correo.com;clave1!;PHONE;699999999\n"
                + "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana@correo.es\n"
                + "USER UPDATE;ana@correo.com;Clave1!;EMAIL;pedro@correo.com\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PASSWORD;clave1!\n"
                + "USER UPDATE;ana@correo.com;Clave1!;CARD;123\n"
                + "USER UPDATE;ana@correo.com;Clave1!;SURNAME; \n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE\n");
        assertEquals(8, errors(out));
        assertTrue(out.contains("unknown or non-modifiable field"));
        assertTrue(out.contains("invalid password for user ana@correo.com"));
        assertTrue(out.contains("a user with email pedro@correo.com already exists"));
        User ana = park.findUser("ana@correo.com");
        assertEquals("12345678Z", ana.getDni());
        assertEquals("612345678", ana.getPhone());
        assertEquals("1234567890123456", ana.getCard());
        assertEquals("Garcia Lopez", ana.getSurnames());
        park.verifyPassword("ana@correo.com", "Clave1!");
    }

    @Test
    void emailChangeKeepsPurchases() throws IOException {
        run(USERS + "SYSTEM DATE;2027-04-15\nTICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com\n");
        User ana = park.findUser("ana.nueva@correo.com");
        assertSame(ana, ((IndividualTicket) park.findTicket("T001")).getUser());
        assertTrue(InvoiceController.getInstance().createUserInvoice("ana.nueva@correo.com").toText().contains("T001"));
    }

    private String app(String stdin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        App.run(new String[0], base, new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(out, true, StandardCharsets.UTF_8));
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void changesPersist() {
        app(USERS + "SYSTEM DATE;2027-04-15\nTICKET CREATE;T001;ana@correo.com;2027-04-15\n"
                + "USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999\n"
                + "USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com\nEXIT\n");
        app("EXIT\n");
        User ana = park.findUser("ana.nueva@correo.com");
        assertEquals("699999999", ana.getPhone());
        assertSame(ana, ((IndividualTicket) park.findTicket("T001")).getUser());
        assertNull(park.findUser("ana@correo.com"));
    }
}
