package es.upm.etsisi.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.etsisi.amusementpark.App;
import es.upm.etsisi.amusementpark.TestData;
import es.upm.etsisi.amusementpark.business.controller.AttractionController;
import es.upm.etsisi.amusementpark.business.model.AttractionType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypeCommandsTest {

    @TempDir
    Path dir;
    CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = App.createProcessor(dir);
        p.process(TestData.CREATE_ANA);
        p.process(TestData.CREATE_PEDRO);
        p.process("GROUP CREATE;G01;Amigos");
        p.process("GROUP ADD;G01;pedro@correo.com");
        p.process("SYSTEM DATE;2026-10-15");
        p.process("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        p.process("GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    private AttractionType typeOf(String id) {
        return AttractionController.getInstance().get(id).getType();
    }

    @Test
    void creationWithAndWithoutType() {
        assertTrue(p.process("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION CREATE;A02;Coches;10;GROUP").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION CREATE;A03;Noria;10;individual").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION CREATE;A04;Vip;10;SPECIAL").startsWith("OK:"));
        assertEquals(AttractionType.GENERAL, typeOf("A01"));
        assertEquals(AttractionType.GROUP, typeOf("A02"));
        assertEquals(AttractionType.INDIVIDUAL, typeOf("A03"));
        assertEquals(AttractionType.SPECIAL, typeOf("A04"));
        assertTrue(p.process("ATTRACTION CREATE;A05;X;10;VIP").startsWith("ERROR: Unknown attraction type"));
        assertTrue(p.process("ATTRACTION CREATE;A05;X;10;GROUP;extra").startsWith("ERROR:"));
        assertTrue(AttractionController.getInstance().find("A05").isEmpty());
    }

    @Test
    void entryFollowsTheAttractionType() {
        p.process("ATTRACTION CREATE;A01;Tornado;20");
        p.process("ATTRACTION CREATE;A02;Coches;10;GROUP");
        p.process("ATTRACTION CREATE;A03;Noria;10;INDIVIDUAL");
        p.process("ATTRACTION CREATE;A04;Vip;10;SPECIAL");
        assertEquals("ERROR: Attraction A02 (GROUP) does not admit ticket T001", p.process("ATTRACTION ENTER;A02;T001"));
        assertTrue(p.process("ATTRACTION ENTER;A02;TG01").startsWith("OK:"));
        assertEquals("ERROR: Attraction A03 (INDIVIDUAL) does not admit ticket TG01", p.process("ATTRACTION ENTER;A03;TG01"));
        assertTrue(p.process("ATTRACTION ENTER;A03;T001").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR:"));
        assertTrue(p.process("ATTRACTION EXIT;A03;T001").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION ENTER;A04;T001").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION EXIT;A02;TG01").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("OK:"));
        assertTrue(p.process("ATTRACTION ENTER;A01;TG01").startsWith("OK:"));
        assertEquals("Users present: 0", p.process("ATTRACTION STATUS;A02").lines().toList().get(2));
    }

    @Test
    void typeIsPersistedAndOldLinesAreGeneral() throws IOException {
        p.process("ATTRACTION CREATE;A02;Coches;10;GROUP");
        assertEquals(List.of("A02;Coches;10;GROUP"), Files.readAllLines(dir.resolve("attractions.txt")));
        Files.write(dir.resolve("attractions.txt"), List.of("A09;Antigua;5", "A02;Coches;10;GROUP", "A08;Mala;5;VIP"));
        App.createProcessor(dir);
        assertEquals(AttractionType.GENERAL, typeOf("A09"));
        assertEquals(AttractionType.GROUP, typeOf("A02"));
        assertTrue(AttractionController.getInstance().find("A08").isEmpty());
    }
}
