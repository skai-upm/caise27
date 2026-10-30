package es.upm.amusementpark.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GroupAccess;
import es.upm.amusementpark.business.model.GroupAttraction;
import es.upm.amusementpark.business.model.IndividualAccess;
import es.upm.amusementpark.business.model.IndividualAttraction;
import es.upm.amusementpark.business.model.SpecialAttraction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypeTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, "SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15");
        s.createGroup("G01", 2, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15");
        s.run("ATTRACTION CREATE;GEN;General;20", "ATTRACTION CREATE;IND;Individual;20;INDIVIDUAL",
                "ATTRACTION CREATE;GRP;Grupo;20;group", "ATTRACTION CREATE;SPE;Especial;20;SPECIAL");
    }

    private AccessTicket ticket(String id) {
        return TicketController.getInstance().findById(id);
    }

    @Test
    void typesAreCreatedAndShown() {
        AttractionController attractions = AttractionController.getInstance();
        assertEquals(AttractionType.GENERAL, attractions.findById("GEN").getType());
        assertEquals(AttractionType.INDIVIDUAL, attractions.findById("IND").getType());
        assertEquals(AttractionType.GROUP, attractions.findById("GRP").getType());
        assertEquals(AttractionType.SPECIAL, attractions.findById("SPE").getType());
        assertTrue(s.run("ATTRACTION STATUS;GRP").contains("Type: GROUP"));
        assertTrue(s.run("ATTRACTION CREATE;X;Otra;10;VIP").startsWith("ERROR"));
        assertNull(attractions.findById("X"));
    }

    @Test
    void generalAcceptsBothKinds() {
        assertTrue(s.run("ATTRACTION ENTER;GEN;T001").startsWith("OK"));
        assertTrue(s.run("ATTRACTION ENTER;GEN;TG01").startsWith("OK"));
        assertEquals(3, AttractionController.getInstance().findById("GEN").getPresent().size());
    }

    @Test
    void individualRejectsGroupTickets() {
        assertTrue(s.run("ATTRACTION ENTER;IND;TG01").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION ENTER;IND;T001").startsWith("OK"));
        assertTrue(s.run("ATTRACTION ENTER;IND;TG01-1").startsWith("OK"));
    }

    @Test
    void groupRejectsIndividualTickets() {
        assertTrue(s.run("ATTRACTION ENTER;GRP;T001").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION ENTER;GRP;TG01-1").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION ENTER;GRP;TG01").startsWith("OK"));
        assertEquals(2, AttractionController.getInstance().findById("GRP").getPresent().size());
    }

    @Test
    void specialRejectsGroupTickets() {
        assertTrue(s.run("ATTRACTION ENTER;SPE;TG01").startsWith("ERROR"));
        assertTrue(AttractionController.getInstance().findById("SPE").getPresent().isEmpty());
    }

    @Test
    void boundsRestrictAcceptedTickets() {
        Optional<IndividualAccess> individual = new IndividualAttraction("I", "I", 5).accept(ticket("T001"));
        assertTrue(individual.isPresent());
        assertFalse(new IndividualAttraction("I", "I", 5).accept(ticket("TG01")).isPresent());
        Optional<GroupAccess> group = new GroupAttraction("G", "G", 5).accept(ticket("TG01"));
        assertEquals("G01", group.orElseThrow().getGroup().getId());
        assertFalse(new SpecialAttraction("S", "S", 5).accept(ticket("TG01")).isPresent());
    }

    @Test
    void e2SyntaxStillCreatesGeneral() {
        assertTrue(s.run("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK: GENERAL"));
    }
}
