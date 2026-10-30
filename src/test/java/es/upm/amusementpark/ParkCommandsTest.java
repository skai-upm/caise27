package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParkCommandsTest extends ParkTestSupport {

    private CommandProcessor processor;

    @BeforeEach
    void createProcessor() {
        processor = new CommandProcessor();
    }

    private String run(String line) {
        return processor.process(line).output();
    }

    @Test
    void systemCommands() {
        assertEquals("SYSTEM DATE SET TO 2027-04-15", run("SYSTEM DATE;2027-04-15"));
        assertEquals("SYSTEM PRICE SET TO 45.50", run("SYSTEM PRICE;45.50"));
        assertTrue(run("SYSTEM DATE;2027-02-30").startsWith("ERROR:"));
        assertTrue(run("SYSTEM PRICE;-1").startsWith("ERROR:"));
    }

    @Test
    void fullScenario() {
        run("SYSTEM DATE;2027-04-15");
        for (int n = 1; n <= 6; n++) {
            assertEquals("USER CREATED", run("USER CREATE;U" + n + ";S" + n + ";" + dni(n) + ";" + email(n)
                    + ";Clave1!;600000000;1234567890123456"));
        }
        assertEquals("GROUP CREATED", run("GROUP CREATE;G02;Familia Grande"));
        for (int n = 1; n <= 6; n++) {
            assertEquals("USER ADDED TO GROUP", run("GROUP ADD;G02;" + email(n)));
        }
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", run("TICKET CREATE;T001;" + email(1) + ";2027-04-15"));
        assertEquals("GROUP TICKET CREATED FOR 6 USER(S). TOTAL PRICE: 192.00 EUR",
                run("GROUPTICKET CREATE;TG02;G02;2027-04-15"));
        assertTrue(run("GROUP REMOVE;G02;" + email(1)).startsWith("ERROR:"));
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertEquals("ENTERED ATTRACTION A01: " + email(1), run("ATTRACTION ENTER;A01;T001"));
        assertTrue(run("ATTRACTION ENTER;A01;TG02").startsWith("ERROR:"));
        assertEquals("EXITED ATTRACTION A01: " + email(1), run("ATTRACTION EXIT;A01;" + email(1)));
        assertTrue(run("ATTRACTION ENTER;A01;TG02").startsWith("ENTERED ATTRACTION A01: " + email(1) + ", "));
        assertEquals(String.join(System.lineSeparator(), "ATTRACTION: A01", "NAME: Dragon Rojo", "CAPACITY: 20",
                "USERS INSIDE: 6", "AVAILABLE PLACES: 14",
                "OCCUPANTS: " + String.join(", ", email(1), email(2), email(3), email(4), email(5), email(6))),
                run("ATTRACTION STATUS;A01"));
        assertTrue(run("ATTRACTION LIST;A01").contains(email(6)));
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR:"));
        assertTrue(run("GROUPTICKET DELETE;TG02").startsWith("ERROR:"));
        assertTrue(run("GROUP DELETE;G02").startsWith("ERROR:"));
        assertTrue(run("USER DELETE;" + email(2) + ";Clave1!").startsWith("ERROR:"));
        run("SYSTEM DATE;2027-04-16");
        assertEquals("GROUP DELETED", run("GROUP DELETE;G02"));
        assertEquals("USER DELETED", run("USER DELETE;" + email(2) + ";Clave1!"));
    }

    @Test
    void futureTicketsDeletable() {
        run("USER CREATE;U1;S1;" + dni(1) + ";" + email(1) + ";Clave1!;600000000;1234567890123456");
        run("GROUP CREATE;G01;Uno");
        run("GROUP ADD;G01;" + email(1));
        run("TICKET CREATE;T001;" + email(1) + ";2027-05-01");
        run("GROUPTICKET CREATE;TG01;G01;2027-05-01");
        assertTrue(run("TICKET DELETE;TG01:" + dni(1)).startsWith("ERROR:"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T001"));
        assertEquals("GROUP TICKET DELETED", run("GROUPTICKET DELETE;TG01"));
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;X;1"));
        assertEquals("NO USERS IN ATTRACTION A01", run("ATTRACTION LIST;A01"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE", "SYSTEM PRICE;1;2", "GROUP CREATE;G01", "GROUP ADD;;x",
            "TICKET CREATE;T1;a@b.com", "GROUPTICKET DELETE", "ATTRACTION CREATE;A;B", "ATTRACTION LIST",
            "ATTRACTION STATUS;A99", "ATTRACTION EXIT;A99;x@y.com", "GROUP DELETE;G99", "TICKET DELETE;T999"})
    void malformedOrUnknownTargetsAreErrors(String line) {
        CommandProcessor.Result result = processor.process(line);
        assertTrue(result.output().startsWith("ERROR:"), result.output());
    }
}
