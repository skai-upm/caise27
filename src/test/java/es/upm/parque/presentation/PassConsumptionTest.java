package es.upm.parque.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.parque.business.ParkController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Pass;
import es.upm.parque.persistence.UserRepository;

class PassConsumptionTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        processor = TestSupport.processor(new UserRepository());
        run("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;600000000;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;600000001;1234567890123457",
                "SYSTEM DATE;2027-04-01",
                "TICKET CREATE;T001;ana@correo.com;2027-04-15",
                "TICKET CREATE;T002;pedro@correo.com;2027-04-15",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL",
                "ATTRACTION CREATE;A02;Montana;12;INDIVIDUAL",
                "ATTRACTION CREATE;A03;Rafting;24;GROUP",
                "ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL",
                "ATTRACTION CREATE;A05;Torre;1;GENERAL",
                "SPEEDPASS CREATE;SP02;T001;10",
                "SPECIALPASS CREATE;PE01;T001",
                "SYSTEM DATE;2027-04-15");
    }

    private void run(String... lines) {
        for (String line : lines) {
            processor.process(line);
        }
    }

    private static Pass pass(String id) {
        return ParkController.getInstance().getPasses().findById(id);
    }

    private static long consumedCount() {
        return ParkController.getInstance().getPasses().findAll().stream().filter(Pass::isConsumed).count();
    }

    private static Attraction<?> attraction(String id) {
        return ParkController.getInstance().getAttractions().findById(id);
    }

    @Test
    void individualSpeedEntryConsumesExactlyOneSpeedPass() {
        String r = processor.process("ATTRACTION ENTER;A01;T001;SPEED");
        assertEquals("OK: 1 user(s) entered attraction A01 (Dragon Rojo): ana@correo.com; SpeedPass consumed: SP02:1", r);
        assertTrue(pass("SP02:1").isConsumed());
        assertEquals(1, consumedCount());
        processor.process("ATTRACTION EXIT;A01;ana@correo.com");
        assertTrue(processor.process("ATTRACTION ENTER;A02;T001;SPEED").endsWith("SpeedPass consumed: SP02:2"));
        assertEquals(2, consumedCount());
    }

    @Test
    void normalEntryConsumesNoSpeedPass() {
        String r = processor.process("ATTRACTION ENTER;A01;T001");
        assertEquals("OK: 1 user(s) entered attraction A01 (Dragon Rojo): ana@correo.com", r);
        assertEquals(0, consumedCount());
    }

    @Test
    void speedEntryWithoutAvailableSpeedPassIsRejected() {
        String r = processor.process("ATTRACTION ENTER;A01;T002;SPEED");
        assertTrue(r.startsWith("ERROR") && r.contains("no unconsumed SpeedPass"), r);
        assertTrue(attraction("A01").getOccupants().isEmpty());
    }

    @Test
    void failedSpeedEntryKeepsTheSpeedPasses() {
        processor.process("ATTRACTION ENTER;A05;T002");
        assertTrue(processor.process("ATTRACTION ENTER;A05;T001;SPEED").contains("has room for 0"));
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;SPEED").startsWith("ERROR"));
        assertEquals(0, consumedCount());
    }

    @Test
    void groupSpeedEntryIsIndivisible() {
        String r = processor.process("ATTRACTION ENTER;A03;TG01;SPEED");
        assertTrue(r.startsWith("ERROR") && r.contains("TG01:"), r);
        assertTrue(attraction("A03").getOccupants().isEmpty());
        assertEquals(0, consumedCount());

        run("SYSTEM DATE;2027-04-01", "SPEEDPASS CREATE;SP05;TG01:12345678Z;1", "SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("ATTRACTION ENTER;A01;TG01;SPEED").startsWith("ERROR"), "Pedro has none");
        assertFalse(pass("SP05:1").isConsumed());
        assertTrue(attraction("A01").getOccupants().isEmpty());

        run("SYSTEM DATE;2027-04-01", "SPEEDPASS CREATE;SP06;TG01:87654321X;1", "SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("ATTRACTION ENTER;A05;TG01;SPEED").contains("nobody entered"), "capacity 1");
        assertEquals(0, consumedCount());

        String ok = processor.process("ATTRACTION ENTER;A03;TG01;SPEED");
        assertEquals("OK: 2 user(s) entered attraction A03 (Rafting): ana@correo.com, pedro@correo.com;"
                + " SpeedPasses consumed: SP05:1, SP06:1", ok);
        assertTrue(pass("SP05:1").isConsumed() && pass("SP06:1").isConsumed());
        assertEquals(2, consumedCount());
    }

    @Test
    void speedIsRejectedWhereNotPermitted() {
        String special = processor.process("ATTRACTION ENTER;A04;T001;SPEED");
        assertTrue(special.startsWith("ERROR") && special.contains("does not accept SpeedPasses"), special);
        assertTrue(processor.process("ATTRACTION ENTER;A02;TG01;SPEED").contains("does not accept group tickets"));
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").contains("does not accept group tickets"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR: ATTRACTION ENTER expects"));
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;SPEED;x").startsWith("ERROR"));
        assertEquals(0, consumedCount());
        assertFalse(pass("PE01").isConsumed());
    }

    @Test
    void specialPassIsConsumedOnSuccessfulEntryOnly() {
        String r = processor.process("ATTRACTION ENTER;A04;T001");
        assertEquals("OK: 1 user(s) entered attraction A04 (Pasaje del Terror): ana@correo.com;"
                + " Special Pass consumed: PE01", r);
        assertTrue(pass("PE01").isConsumed());
        processor.process("ATTRACTION EXIT;A04;ana@correo.com");
        assertTrue(processor.process("ATTRACTION ENTER;A04;T001").contains("already been consumed"));
        assertTrue(processor.process("SPECIALPASS DELETE;PE01").contains("consumed"), "not refundable");
        assertTrue(processor.process("INVOICE CREATE;USER;ana@correo.com").contains("PE01 for ticket T001 (consumed)"));
    }

    @Test
    void failedSpecialEntryKeepsTheSpecialPass() {
        run("SYSTEM DATE;2027-04-01", "SPECIALPASS CREATE;PE02;T002", "ATTRACTION CREATE;A06;Mini;1;SPECIAL",
                "SYSTEM DATE;2027-04-15");
        assertTrue(processor.process("ATTRACTION ENTER;A06;T002").startsWith("OK"));
        assertTrue(processor.process("ATTRACTION ENTER;A06;T001").startsWith("ERROR"), "full");
        assertFalse(pass("PE01").isConsumed());
        assertTrue(processor.process("ATTRACTION ENTER;A04;T002").startsWith("ERROR"), "PE02 already consumed");
        processor.process("SYSTEM DATE;2027-04-16");
        assertTrue(processor.process("ATTRACTION ENTER;A04;T001").startsWith("ERROR"), "wrong date");
        assertFalse(pass("PE01").isConsumed());
    }
}
