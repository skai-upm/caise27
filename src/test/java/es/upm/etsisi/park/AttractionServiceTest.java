package es.upm.etsisi.park;

import es.upm.etsisi.park.business.*;
import es.upm.etsisi.park.persistence.*;
import es.upm.etsisi.park.presentation.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AttractionServiceTest {

    @TempDir
    Path dir;
    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.run(ParkFixture.ANA, ParkFixture.PEDRO, ParkFixture.LUIS, ParkFixture.MARTA,
                "ATTRACTION CREATE;A01;Dragon Rojo;20");
    }

    private List<String> inside(String attraction) {
        return f.park.findAttraction(attraction).getOccupants().stream().map(User::getEmail).toList();
    }

    @Test
    void createsAttraction() {
        Attraction a = f.park.findAttraction("A01");
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ATTRACTION CREATE;A03;Noria;0", "ATTRACTION CREATE;A03;Noria;diez",
            "ATTRACTION CREATE;A03;Noria;-2", "ATTRACTION CREATE;A01;Otra;5", "ATTRACTION CREATE;;Noria;5"})
    void rejectsInvalidAttraction(String command) {
        assertTrue(f.run(command).startsWith("ERROR"));
        assertEquals(1, f.park.getAttractions().size());
        assertEquals("Dragon Rojo", f.park.findAttraction("A01").getName());
    }

    @Test
    void listIsReadOnly() {
        assertEquals("NO USERS INSIDE", f.run("ATTRACTION LIST;A01"));
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15", "ATTRACTION ENTER;A01;T001");
        assertEquals("ana@correo.com", f.run("ATTRACTION LIST;A01"));
        assertEquals("ana@correo.com", f.run("ATTRACTION LIST;A01"));
        assertEquals(List.of("ana@correo.com"), inside("A01"));
        assertTrue(f.run("ATTRACTION LIST;A99").startsWith("ERROR: no attraction"));
    }

    @Test
    void individualEntry() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        assertEquals("ATTRACTION ENTERED: 1 USER(S)", f.run("ATTRACTION ENTER;A01;T001"));
        assertEquals(List.of("ana@correo.com"), inside("A01"));
    }

    @Test
    void entryRejections() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-16", "TICKET CREATE;T002;pedro@correo.com;2027-04-15",
                "ATTRACTION CREATE;A02;Mini;1", "TICKET CREATE;T003;luis@correo.com;2027-04-15");
        assertTrue(f.run("ATTRACTION ENTER;A01;T001").contains("valid on 2027-04-16"));
        assertTrue(f.run("ATTRACTION ENTER;A01;T999").startsWith("ERROR: no ticket"));
        assertTrue(f.run("ATTRACTION ENTER;A99;T002").startsWith("ERROR: no attraction"));
        f.run("ATTRACTION ENTER;A02;T002");
        assertTrue(f.run("ATTRACTION ENTER;A02;T003").contains("not enough places"));
        assertTrue(f.run("ATTRACTION ENTER;A02;T002").startsWith("ERROR"));
        assertTrue(inside("A01").isEmpty());
        assertEquals(List.of("pedro@correo.com"), inside("A02"));
    }

    @Test
    void presenceSurvivesDateChange() {
        f.run("TICKET CREATE;T001;ana@correo.com;2027-04-15", "ATTRACTION ENTER;A01;T001", "SYSTEM DATE;2027-04-16");
        assertEquals(List.of("ana@correo.com"), inside("A01"));
    }

    @Test
    void groupEntryIsAllOrNothing() {
        f.run("GROUP CREATE;G01;Cuatro", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUP ADD;G01;luis@correo.com", "GROUP ADD;G01;marta@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "ATTRACTION CREATE;A02;Torre del Cielo;3");
        assertTrue(f.run("ATTRACTION ENTER;A02;TG01").contains("not enough places"));
        assertTrue(inside("A02").isEmpty());
        assertEquals("ATTRACTION ENTERED: 4 USER(S)", f.run("ATTRACTION ENTER;A01;TG01"));
        assertEquals(4, inside("A01").size());
    }

    @Test
    void groupMembersExitIndividuallyAndStatusReflectsIt() {
        f.run("GROUP CREATE;G01;Pareja", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "ATTRACTION ENTER;A01;TG01");
        assertEquals("ATTRACTION EXITED", f.run("ATTRACTION EXIT;A01;ana@correo.com"));
        assertEquals(List.of("pedro@correo.com"), inside("A01"));
        String status = f.run("ATTRACTION STATUS;A01");
        assertEquals(String.join(System.lineSeparator(), "ATTRACTION: A01", "NAME: Dragon Rojo", "TYPE: GENERAL", "CAPACITY: 20",
                "INSIDE: 1", "AVAILABLE: 19", "USERS: pedro@correo.com"), status);
    }

    @Test
    void exitRejections() {
        assertTrue(f.run("ATTRACTION EXIT;A01;luis@correo.com").contains("not inside"));
        assertTrue(f.run("ATTRACTION EXIT;A99;luis@correo.com").startsWith("ERROR: no attraction"));
        assertTrue(f.run("ATTRACTION STATUS;A99").startsWith("ERROR: no attraction"));
        assertTrue(f.run("ATTRACTION STATUS;A01").endsWith("USERS: -"));
    }

    @Test
    void composedTicketCanBeUsedIndividually() {
        f.run("GROUP CREATE;G01;Pareja", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15");
        assertEquals("ATTRACTION ENTERED: 1 USER(S)", f.run("ATTRACTION ENTER;A01;TG01:87654321X"));
        assertEquals(List.of("pedro@correo.com"), inside("A01"));
    }
}
