package com.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amusementpark.business.controller.AttractionController;
import com.amusementpark.business.model.Admission;
import com.amusementpark.business.model.AttractionType;
import com.amusementpark.business.model.GroupAdmission;
import com.amusementpark.business.model.GroupAttraction;
import com.amusementpark.business.model.IndividualAdmission;
import com.amusementpark.business.model.IndividualAttraction;
import com.amusementpark.business.model.SpecialAttraction;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypeTest {

    @TempDir
    Path dir;

    private ParkFixture f;

    @BeforeEach
    void setUp() throws IOException {
        f = new ParkFixture(dir);
        f.user("Ana", 12345678);
        f.group("G01", 2, 40000000);
        f.ok("SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    private AttractionType type(String id) {
        return AttractionController.getInstance().find(id).getType();
    }

    @Test
    void e2SyntaxCreatesGeneralAndTypeCanBeGiven() {
        assertEquals("OK: Attraction created: A01 (GENERAL)", f.run("ATTRACTION CREATE;A01;Tornado;20"));
        f.ok("ATTRACTION CREATE;A02;Solo;5;INDIVIDUAL", "ATTRACTION CREATE;A03;Familiar;5;group",
                "ATTRACTION CREATE;A04;Magica;5;SPECIAL");
        assertEquals(AttractionType.GENERAL, type("A01"));
        assertEquals(AttractionType.INDIVIDUAL, type("A02"));
        assertEquals(AttractionType.GROUP, type("A03"));
        assertEquals(AttractionType.SPECIAL, type("A04"));
        assertTrue(f.run("ATTRACTION STATUS;A04").contains("[SPECIAL]"));
    }

    @Test
    void rejectsUnknownType() {
        assertTrue(f.run("ATTRACTION CREATE;A05;Vip;5;VIP").startsWith("ERROR:"));
        assertEquals(null, AttractionController.getInstance().find("A05"));
    }

    @Test
    void generalAdmitsBothKinds() {
        f.ok("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001", "ATTRACTION ENTER;A01;TG01");
        assertEquals(3, AttractionController.getInstance().find("A01").getVisitors().size());
    }

    @Test
    void individualRejectsGroupTickets() {
        f.ok("ATTRACTION CREATE;A02;Solo;20;INDIVIDUAL");
        assertTrue(f.run("ATTRACTION ENTER;A02;TG01").startsWith("ERROR:"));
        f.ok("ATTRACTION ENTER;A02;T001", "ATTRACTION ENTER;A02;TG01-1");
    }

    @Test
    void groupRejectsIndividualTickets() {
        f.ok("ATTRACTION CREATE;A03;Familiar;20;GROUP");
        assertTrue(f.run("ATTRACTION ENTER;A03;T001").startsWith("ERROR:"));
        assertTrue(f.run("ATTRACTION ENTER;A03;TG01-1").startsWith("ERROR:"));
        f.ok("ATTRACTION ENTER;A03;TG01");
    }

    @Test
    void specialUsesIndividualTicketsOnly() {
        f.ok("ATTRACTION CREATE;A04;Magica;20;SPECIAL");
        assertTrue(f.run("ATTRACTION ENTER;A04;TG01").startsWith("ERROR:"));
        assertEquals(0, AttractionController.getInstance().find("A04").getVisitors().size());
    }

    @Test
    void genericBoundsMatchAdmittedCapabilities() {
        assertEquals(IndividualAdmission.class, typeArgument(IndividualAttraction.class));
        assertEquals(GroupAdmission.class, typeArgument(GroupAttraction.class));
        assertEquals(IndividualAdmission.class, typeArgument(SpecialAttraction.class));
        assertEquals(Admission.class, typeArgument(com.amusementpark.business.model.GeneralAttraction.class));
        assertFalse(new GroupAttraction("X", "x", 1).accepts(new com.amusementpark.business.model.IndividualTicket(
                "T", java.time.LocalDate.now(), java.math.BigDecimal.ONE, null)));
    }

    private static Object typeArgument(Class<?> c) {
        return ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
