package com.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parque.ApplicationContext;
import com.parque.TestPark;
import com.parque.business.model.AttractionType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTypeCommandsTest {
    @TempDir
    Path dir;
    CommandProcessor p;

    private void ok(String c) {
        String r = p.process(c);
        assertTrue(r.startsWith("OK"), c + " -> " + r);
    }

    private void setUpPark() {
        ok("SYSTEM DATE;2026-10-15");
        ok("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        ok("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        ok("GROUP CREATE;G01;Familia");
        ok("GROUP ADD;G01;pedro@correo.com");
        ok("TICKET CREATE;T001;ana@correo.com;2026-10-15");
        ok("GROUPTICKET CREATE;TG01;G01;2026-10-15");
    }

    @BeforeEach
    void setUp() throws Exception {
        p = new CommandProcessor(TestPark.start(dir));
    }

    @AfterEach
    void tearDown() {
        ApplicationContext.stop();
    }

    @Test
    void creationDefaultAndExplicitTypes() throws Exception {
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION CREATE;A02;Familiar;30;GROUP");
        ok("ATTRACTION CREATE;A03;Solo;10;INDIVIDUAL");
        ok("ATTRACTION CREATE;A04;Magia;5;SPECIAL");
        ok("ATTRACTION CREATE;A05;Todo;5;GENERAL");
        assertTrue(p.process("ATTRACTION CREATE;A06;X;10;VIP").startsWith("ERROR: "));
        assertTrue(p.process("ATTRACTION CREATE;A06;X;10;group").startsWith("ERROR: "));
        assertTrue(p.process("ATTRACTION CREATE;A06;X;10;GROUP;x").startsWith("ERROR: "));
        assertEquals(AttractionType.GENERAL, p.getController().attraction("A01").getType());
        assertEquals(AttractionType.GROUP, p.getController().attraction("A02").getType());
        assertTrue(p.process("ATTRACTION STATUS;A02").contains("Type: GROUP"));
        assertEquals(List.of("A01;Tornado;20;GENERAL", "A02;Familiar;30;GROUP", "A03;Solo;10;INDIVIDUAL",
                "A04;Magia;5;SPECIAL", "A05;Todo;5;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
    }

    @Test
    void entryRulesByType() {
        setUpPark();
        ok("ATTRACTION CREATE;A01;Tornado;20");
        ok("ATTRACTION CREATE;A02;Familiar;30;GROUP");
        ok("ATTRACTION CREATE;A03;Solo;10;INDIVIDUAL");
        ok("ATTRACTION CREATE;A04;Magia;5;SPECIAL");
        assertTrue(p.process("ATTRACTION ENTER;A02;T001").startsWith("ERROR: "));
        assertTrue(p.process("ATTRACTION ENTER;A03;TG01").startsWith("ERROR: "));
        assertTrue(p.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR: "));
        ok("ATTRACTION ENTER;A02;TG01");
        ok("ATTRACTION ENTER;A03;T001");
        ok("SPECIALPASS CREATE;S01;TG01-1");
        ok("ATTRACTION ENTER;A04;TG01-1");
        ok("ATTRACTION ENTER;A01;T001");
        ok("ATTRACTION EXIT;A02;TG01");
    }

    @Test
    void legacyLineReadAsGeneralAndTypesSurviveRestart() throws Exception {
        ApplicationContext.stop();
        Files.write(dir.resolve("attractions.txt"), List.of("A01;Tornado;20", "A02;Familiar;30;GROUP"));
        p = new CommandProcessor(TestPark.start(dir));
        assertEquals(AttractionType.GENERAL, p.getController().attraction("A01").getType());
        assertEquals(AttractionType.GROUP, p.getController().attraction("A02").getType());
        assertEquals(20, p.getController().attraction("A01").getCapacity());
    }
}
