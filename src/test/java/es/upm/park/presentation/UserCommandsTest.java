package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.controller.ParkControllerTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserCommandsTest {

    private static final String ANA =
            "USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456";

    private final CommandProcessor processor = new CommandProcessor();

    @BeforeEach
    void setUp() {
        ParkControllerTestSupport.reset();
        ParkController.getInstance().setCurrentDate(LocalDate.of(2027, 4, 15));
    }

    @AfterEach
    void tearDown() {
        ParkControllerTestSupport.reset();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void createUserAndUseIt() {
        assertEquals("USER CREATED", run(ANA));
        assertEquals("TICKET CREATED. PRICE: 40.00", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        assertTrue(run("INVOICE CREATE;USER;ana@correo.com").contains("DNI: 12345678Z"));
    }

    @Test
    void deleteUserCommand() {
        run(ANA);
        assertTrue(run("USER DELETE;ana@correo.com;Clave2!").startsWith("ERROR"));
        assertTrue(run("USER DELETE;ana@correo.com").startsWith("ERROR"));
        assertEquals("USER DELETED", run("USER DELETE;ana@correo.com;Clave1!"));
        assertTrue(run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR"));
    }

    @Test
    void updateUserCommand() {
        run(ANA);
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;NAME;Ana Maria"));
        assertEquals("USER UPDATED", run("USER UPDATE;ana@correo.com;Clave1!;EMAIL;ana.nueva@correo.com"));
        assertEquals("USER UPDATED", run("USER UPDATE;ana.nueva@correo.com;Clave1!;PASSWORD;Nueva2#"));
        assertEquals("USER UPDATED", run("USER UPDATE;ana.nueva@correo.com;Nueva2#;CARD;9999888877776666"));
        for (String line : new String[] {"USER UPDATE;ana.nueva@correo.com;Nueva2#;DNI;87654321X",
                "USER UPDATE;ana.nueva@correo.com;Nueva2#;AGE;30", "USER UPDATE;ana.nueva@correo.com;Clave1!;NAME;X",
                "USER UPDATE;ana@correo.com;Clave1!;NAME;X", "USER UPDATE;ana.nueva@correo.com;Nueva2#;NAME"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
    }

    @Test
    void errorsAddNoUser() {
        run(ANA);
        for (String line : new String[] {ANA,
                "USER CREATE;Ana;Garcia;12345678Z;otra@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia;00000000T;ana@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia;12345678A;x@correo.com;Clave1!;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia;00000000T;x@correo.es;Clave1!;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia;00000000T;x@correo.com;Clave12;612345678;1234567890123456",
                "USER CREATE;Ana;Garcia;00000000T;x@correo.com;Clave1!;612345678;123456789012345",
                "USER CREATE;Ana;Garcia;00000000T;x@correo.com;Clave1!;612345678",
                "USER CREATE"}) {
            assertTrue(run(line).startsWith("ERROR"), line);
        }
        assertTrue(run("TICKET CREATE;T001;x@correo.com;2027-04-15").startsWith("ERROR"), "x was never created");
    }
}
