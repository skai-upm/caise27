package es.upm.amusementpark.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.persistence.repository.Repositories;
import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.presentation.CommandProcessor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PassEntryTest {

    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        p = ApplicationInitializer.initialize(Repositories.inMemory());
        for (String c : List.of("SYSTEM DATE;2027-04-01",
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "USER CREATE;Luis;Martin;00000011B;luis@correo.com;Clave1!;6;1111222233334444",
                "GROUP CREATE;G01;F", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15", "TICKET CREATE;T003;luis@correo.com;2027-04-15",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "SPEEDPASS CREATE;SP01;T001;1", "SPEEDPASS CREATE;SP02;T001;1", "SPECIALPASS CREATE;PE01;T001",
                "ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION CREATE;A02;Mini;1;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting;24;GROUP", "ATTRACTION CREATE;A04;Terror;15;SPECIAL",
                "SYSTEM DATE;2027-04-15")) {
            assertFalse(p.process(c).startsWith("ERROR"), c);
        }
    }

    private Pass pass(String id) {
        return Park.getInstance().passes().findById(id).orElseThrow();
    }

    private Attraction<?> a(String id) {
        return Park.getInstance().attractions().findById(id).orElseThrow();
    }

    @Test
    void individualSpeedEntryConsumesExactlyOne() {
        assertEquals("ENTRY GRANTED: 1 user(s) entered A01. SpeedPass consumed: SP01:1", p.process("ATTRACTION ENTER;A01;T001;SPEED"));
        assertTrue(pass("SP01:1").isConsumed());
        assertFalse(pass("SP02:1").isConsumed());
        assertTrue(p.process("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR"));
    }

    @Test
    void noAvailableSpeedPassAndFailureKeepsPasses() {
        assertTrue(p.process("ATTRACTION ENTER;A01;T003;SPEED").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
        p.process("ATTRACTION ENTER;A02;T003");
        assertTrue(p.process("ATTRACTION ENTER;A02;T001;SPEED").startsWith("ERROR"));
        assertFalse(pass("SP01:1").isConsumed());
        assertFalse(pass("SP02:1").isConsumed());
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        p.process("SPEEDPASS CREATE;SP10;TG01:12345678Z;1");
        assertTrue(p.process("ATTRACTION ENTER;A03;TG01;SPEED").startsWith("ERROR"));
        assertTrue(a("A03").getOccupants().isEmpty());
        assertFalse(pass("SP10:1").isConsumed());
        p.process("SPEEDPASS CREATE;SP11;TG01:87654321X;1");
        String out = p.process("ATTRACTION ENTER;A03;TG01;SPEED");
        assertTrue(out.startsWith("ENTRY GRANTED: 2 user(s) entered A03. SpeedPass consumed: SP10:1, SP11:1"), out);
        assertTrue(pass("SP10:1").isConsumed() && pass("SP11:1").isConsumed());
    }

    @Test
    void specialEntry() {
        assertTrue(p.process("ATTRACTION ENTER;A04;T001;SPEED").startsWith("ERROR"));
        assertFalse(pass("PE01").isConsumed());
        assertFalse(pass("SP01:1").isConsumed());
        assertTrue(p.process("ATTRACTION ENTER;A04;T003").startsWith("ERROR"));
        assertEquals("ENTRY GRANTED: 1 user(s) entered A04. Special Pass consumed: PE01", p.process("ATTRACTION ENTER;A04;T001"));
        assertTrue(pass("PE01").isConsumed());
        p.process("ATTRACTION EXIT;A04;ana@correo.com");
        assertTrue(p.process("ATTRACTION ENTER;A04;T001").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
    }
}
