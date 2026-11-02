package com.amusementpark.presentation;

import com.amusementpark.business.model.Attraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.group("G01", 2, 80000000);
        f.ok("SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20");
    }

    private Attraction a01() {
        return f.park.getAttractionManager().find("A01");
    }

    @Test
    void createsAttraction() {
        assertEquals("Tornado", a01().getName());
        assertEquals(20, a01().getCapacity());
        assertTrue(a01().getVisitors().isEmpty());
    }

    @Test
    void rejectsInvalidAttractions() {
        assertTrue(f.run("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION CREATE;A02;Otra;0").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION CREATE;A02;Otra;-3").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION CREATE;A02;Otra;muchos").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION CREATE;A02;;5").startsWith("ERROR:"));
        assertEquals(1, f.park.getAttractionManager().getAttractions().size());
    }

    @Test
    void entryExitAndStatus() {
        f.ok("ATTRACTION ENTER;A01;T001", "ATTRACTION ENTER;A01;TG01");
        String status = f.run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Tornado"));
        assertTrue(status.contains("Capacity: 20 | Present: 3 | Available: 17"));
        assertTrue(status.contains("ana@correo.com"));
        assertTrue(status.contains("g01m0@correo.com"));
        assertTrue(status.contains("g01m1@correo.com"));

        f.ok("ATTRACTION EXIT;A01;T001");
        status = f.run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 2 | Available: 18"));
        assertTrue(!status.contains("ana@correo.com"));

        f.ok("ATTRACTION EXIT;A01;g01m0@correo.com");
        assertEquals(1, a01().getVisitors().size());
        f.ok("ATTRACTION EXIT;A01;TG01");
        assertTrue(a01().getVisitors().isEmpty());
        assertTrue(f.run("ATTRACTION STATUS;A01").contains("Users present: none"));
    }

    @Test
    void groupEntryIsAtomicOnCapacity() {
        f.ok("ATTRACTION CREATE;A02;Mini;2", "ATTRACTION ENTER;A02;T001");
        assertTrue(f.run("ATTRACTION ENTER;A02;TG01").startsWith("ERROR:"));
        assertEquals(1, f.park.getAttractionManager().find("A02").getVisitors().size());
    }

    @Test
    void capacityIsNeverExceeded() {
        f.ok("ATTRACTION CREATE;A02;Solo;1", "ATTRACTION ENTER;A02;T001");
        assertTrue(f.run("ATTRACTION ENTER;A02;TG01-1").startsWith("ERROR:"));
        assertEquals(0, f.park.getAttractionManager().find("A02").getAvailable());
    }

    @Test
    void ticketMustBeForSystemDate() {
        f.ok("TICKET CREATE;T002;ana@correo.com;2026-10-16");
        assertTrue(f.run("ATTRACTION ENTER;A01;T002").startsWith("ERROR:"));
        f.ok("SYSTEM DATE;2026-10-16");
        assertTrue(f.run("ATTRACTION ENTER;A01;T001").startsWith("ERROR:"));
        f.ok("ATTRACTION ENTER;A01;T002");
    }

    @Test
    void sameUserCannotEnterTwice() {
        f.ok("ATTRACTION ENTER;A01;TG01-1");
        assertTrue(f.run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR:"));
        assertEquals(1, a01().getVisitors().size());
    }

    @Test
    void exitNeedsNoDateRevalidation() {
        f.ok("ATTRACTION ENTER;A01;T001", "SYSTEM DATE;2026-12-01", "ATTRACTION EXIT;A01;T001");
        assertTrue(a01().getVisitors().isEmpty());
    }

    @Test
    void rejectsUnknownReferences() {
        assertTrue(f.run("ATTRACTION ENTER;A99;T001").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION ENTER;A01;NOPE").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION EXIT;A01;T001").startsWith("ERROR:"), "holder not present");
        assertTrue(f.run("ATTRACTION EXIT;A01;nadie@correo.com").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION STATUS;A99").startsWith("ERROR:"));
    }

    @Test
    void definitionsPersistButStartEmpty() throws IOException {
        f.ok("ATTRACTION ENTER;A01;T001");
        f.restart();
        assertEquals("Tornado", a01().getName());
        assertEquals(20, a01().getCapacity());
        assertTrue(a01().getVisitors().isEmpty());
    }
}
