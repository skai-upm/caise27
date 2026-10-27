package es.upm.amusementpark.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.upm.amusementpark.business.controller.ApplicationInitializer;
import es.upm.amusementpark.persistence.repository.UserRepository;

class PassEntryCommandsTest {

    private CommandProcessor processor;

    @BeforeEach
    void setUp() {
        ApplicationInitializer.startInMemory(new UserRepository());
        processor = new CommandProcessor();
        processor.process("USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456");
        processor.process("USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654");
        processor.process("SYSTEM DATE;2027-04-15");
        processor.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        processor.process("GROUP CREATE;G01;F");
        processor.process("GROUP ADD;G01;ana@correo.com");
        processor.process("GROUP ADD;G01;pedro@correo.com");
        processor.process("GROUPTICKET CREATE;TG01;G01;2027-04-15");
        processor.process("ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL");
        processor.process("ATTRACTION CREATE;A02;Mini;1;INDIVIDUAL");
        processor.process("ATTRACTION CREATE;A03;Rafting;24;GROUP");
        processor.process("ATTRACTION CREATE;A04;Terror;15;SPECIAL");
    }

    @Test
    void individualSpeedEntry() {
        assertEquals("ERROR: Ticket T001 has no available SpeedPass", processor.process("ATTRACTION ENTER;A01;T001;SPEED"));
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        assertEquals("ENTERED A01: ana@correo.com (consumed SpeedPass SP01:1)",
                processor.process("ATTRACTION ENTER;A01;T001;SPEED"));
        processor.process("ATTRACTION EXIT;A01;ana@correo.com");
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;SPEED").startsWith("ERROR"));
        assertEquals("ENTERED A01: ana@correo.com", processor.process("ATTRACTION ENTER;A01;T001"));
    }

    @Test
    void failedSpeedEntryKeepsPass() {
        processor.process("SPEEDPASS CREATE;SP01;T001;1");
        processor.process("TICKET CREATE;T002;pedro@correo.com;2027-04-15");
        processor.process("ATTRACTION ENTER;A02;T002");
        assertEquals("ERROR: Not enough capacity in A02", processor.process("ATTRACTION ENTER;A02;T001;SPEED"));
        processor.process("ATTRACTION EXIT;A02;pedro@correo.com");
        assertEquals("ENTERED A02: ana@correo.com (consumed SpeedPass SP01:1)",
                processor.process("ATTRACTION ENTER;A02;T001;SPEED"));
    }

    @Test
    void groupSpeedEntryIsAtomic() {
        processor.process("SPEEDPASS CREATE;SPA;TG01:12345678Z;1");
        assertEquals("ERROR: Ticket TG01:87654321X has no available SpeedPass",
                processor.process("ATTRACTION ENTER;A03;TG01;SPEED"));
        assertEquals("No users in A03", processor.process("ATTRACTION LIST;A03"));
        processor.process("SPEEDPASS CREATE;SPB;TG01:87654321X;1");
        assertEquals("ENTERED A03: ana@correo.com pedro@correo.com (consumed SpeedPass SPA:1) (consumed SpeedPass SPB:1)",
                processor.process("ATTRACTION ENTER;A03;TG01;SPEED"));
    }

    @Test
    void specialAttraction() {
        assertEquals("ERROR: Ticket T001 has no unconsumed Special Pass", processor.process("ATTRACTION ENTER;A04;T001"));
        processor.process("SPECIALPASS CREATE;PE01;T001");
        assertEquals("ERROR: SpeedPasses are not accepted at special attractions",
                processor.process("ATTRACTION ENTER;A04;T001;SPEED"));
        assertEquals("ENTERED A04: ana@correo.com (consumed Special Pass PE01)", processor.process("ATTRACTION ENTER;A04;T001"));
        processor.process("ATTRACTION EXIT;A04;ana@correo.com");
        assertEquals("ERROR: Ticket T001 has no unconsumed Special Pass", processor.process("ATTRACTION ENTER;A04;T001"));
        assertTrue(processor.process("ATTRACTION ENTER;A04;TG01").startsWith("ERROR"));
        assertEquals("ERROR: Consumed passes cannot be refunded", processor.process("SPECIALPASS DELETE;PE01"));
    }

    @Test
    void badVariantAndHelp() {
        assertTrue(processor.process("ATTRACTION ENTER;A01;T001;FAST").startsWith("ERROR: Invalid format"));
        assertTrue(processor.process("HELP").contains("ATTRACTION ENTER;<attraction>;<ticket>;SPEED"));
    }
}
