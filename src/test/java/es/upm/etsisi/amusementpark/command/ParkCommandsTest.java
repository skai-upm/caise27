package es.upm.etsisi.amusementpark.command;

import static es.upm.etsisi.amusementpark.command.CommandProcessorTest.CREATE_ANA;
import static es.upm.etsisi.amusementpark.command.CommandProcessorTest.CREATE_PEDRO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import es.upm.etsisi.amusementpark.park.DataPaths;
import es.upm.etsisi.amusementpark.park.Park;

class ParkCommandsTest {

    @TempDir
    Path dir;

    private Park park;
    private CommandProcessor processor;

    @BeforeEach
    void setUp() throws IOException {
        park = Park.getInstance();
        park.load(DataPaths.in(dir));
        processor = new CommandProcessor(park);
        run("SYSTEM DATE;2027-04-15");
    }

    private String run(String line) {
        return processor.process(line).message();
    }

    private void anaAndPedroInG01() {
        run(CREATE_ANA);
        run(CREATE_PEDRO);
        run("GROUP CREATE;G01;Familia Garcia");
        run("GROUP ADD;G01;ana@correo.com");
        run("GROUP ADD;G01;pedro@correo.com");
    }

    @Test
    void systemDate() {
        assertEquals("SYSTEM DATE SET: 2027-04-16", run("SYSTEM DATE;2027-04-16"));
        assertEquals(LocalDate.of(2027, 4, 16), park.getCurrentDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SYSTEM DATE;2027-13-40", "SYSTEM DATE;15/04/2027", "SYSTEM DATE;2027-02-30",
        "SYSTEM DATE;"})
    void invalidSystemDateIsRejected(String command) {
        assertTrue(run(command).startsWith("ERROR:"));
        assertEquals(LocalDate.of(2027, 4, 15), park.getCurrentDate());
    }

    @Test
    void systemPrice() {
        assertEquals("SYSTEM PRICE SET: 45.50 EUR", run("SYSTEM PRICE;45.50"));
        assertEquals("SYSTEM PRICE SET: 30.00 EUR", run("SYSTEM PRICE;30"));
        assertEquals(new BigDecimal("30.00"), park.getBasePrice());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-3", "abc", "1.234", ".5", "5.", "1e3", "0.00", ""})
    void invalidSystemPriceIsRejected(String price) {
        assertTrue(run("SYSTEM PRICE;" + price).startsWith("ERROR:"));
        assertEquals(new BigDecimal("40.00"), park.getBasePrice());
    }

    @Test
    void groupCommands() {
        anaAndPedroInG01();
        assertEquals(2, park.findGroup("G01").getMembers().size());
        assertTrue(run("GROUP ADD;G01;ana@correo.com").startsWith("ERROR:"));
        assertEquals("GROUP USER REMOVED", run("GROUP REMOVE;G01;pedro@correo.com"));
        assertEquals("GROUP DELETED", run("GROUP DELETE;G01"));
        assertEquals("GROUP CREATED", run("GROUP CREATE;G01;Otra"));
        assertEquals("GROUP USER ADDED", run("GROUP ADD;G01;ana@correo.com"));
    }

    @Test
    void ticketCommandsDisplayPrices() {
        anaAndPedroInG01();
        assertEquals("TICKET CREATED. PRICE: 40.00 EUR", run("TICKET CREATE;T001;ana@correo.com;2027-04-15"));
        assertTrue(run("TICKET CREATE;T002;ana@correo.com;2027-04-14").startsWith("ERROR:"));
        assertTrue(run("TICKET CREATE;T002;ana@correo.com;tomorrow").startsWith("ERROR:"));
        run("SYSTEM PRICE;45.50");
        assertEquals("TICKET CREATED. PRICE: 45.50 EUR", run("TICKET CREATE;T003;ana@correo.com;2027-04-16"));
        assertEquals("TICKET DELETED", run("TICKET DELETE;T003"));
        assertTrue(run("TICKET DELETE;T001").startsWith("ERROR:"));
        run("SYSTEM PRICE;40");
        assertEquals("GROUPTICKET CREATED. TOTAL PRICE: 80.00 EUR", run("GROUPTICKET CREATE;TG01;G01;2027-04-16"));
        assertTrue(run("GROUP ADD;G01;x@correo.com").startsWith("ERROR:"));
        assertEquals("GROUPTICKET DELETED", run("GROUPTICKET DELETE;TG01"));
    }

    @Test
    void attractionCommands() {
        anaAndPedroInG01();
        assertEquals("ATTRACTION CREATED", run("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        assertTrue(run("ATTRACTION CREATE;A02;X;0").startsWith("ERROR:"));
        assertTrue(run("ATTRACTION CREATE;A02;X;abc").startsWith("ERROR:"));
        assertTrue(run("ATTRACTION CREATE;A01;X;5").startsWith("ERROR:"));
        assertEquals("USERS IN A01: (none)", run("ATTRACTION LIST;A01"));

        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        run("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertEquals("ATTRACTION ENTERED: 1 user(s)", run("ATTRACTION ENTER;A01;T001"));
        assertTrue(run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR:"));
        assertEquals("ATTRACTION EXITED", run("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals("ATTRACTION ENTERED: 2 user(s)", run("ATTRACTION ENTER;A01;TG01"));
        assertEquals("USERS IN A01: ana@correo.com, pedro@correo.com", run("ATTRACTION LIST;A01"));
        assertEquals("ATTRACTION EXITED", run("ATTRACTION EXIT;A01;ana@correo.com"));

        String status = run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01 - Dragon Rojo"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Inside: 1"));
        assertTrue(status.contains("Available: 19"));
        assertTrue(status.contains("pedro@correo.com"));
        assertTrue(run("ATTRACTION STATUS;A99").startsWith("ERROR:"));
        assertTrue(run("ATTRACTION LIST;A99").startsWith("ERROR:"));
    }

    @Test
    void userDeleteGoesThroughParkRules() {
        run(CREATE_ANA);
        run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertTrue(run("USER DELETE;ana@correo.com;Clave1!").startsWith("ERROR:"));
        run("SYSTEM DATE;2027-04-16");
        assertEquals("USER DELETED", run("USER DELETE;ana@correo.com;Clave1!"));
    }

    @Test
    void wrongArityForNewCommands() {
        assertTrue(run("SYSTEM DATE").startsWith("ERROR: wrong number of elements"));
        assertTrue(run("GROUP CREATE;G01").startsWith("ERROR: wrong number of elements"));
        assertTrue(run("ATTRACTION ENTER;A01").startsWith("ERROR: wrong number of elements"));
    }
}
