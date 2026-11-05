package es.upm.amusementpark.business.service;

import es.upm.amusementpark.presentation.ApplicationInitializer;
import es.upm.amusementpark.persistence.repository.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.presentation.CommandProcessor;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.persistence.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionAccessTest {

    private Park park;
    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        park = Park.getInstance();
        park.start(Repositories.inMemory(new UserRepository()));
        p = ApplicationInitializer.initialize(Repositories.inMemory(park.users()));
        p.process("SYSTEM DATE;2027-04-01");
        p.process("USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456");
        p.process("USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654");
        p.process("TICKET CREATE;T001;ana@correo.com;2027-04-15");
        p.process("TICKET CREATE;T002;pedro@correo.com;2027-04-15");
        p.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        p.process("ATTRACTION CREATE;A02;Mini;1");
        p.process("SYSTEM DATE;2027-04-15");
    }

    private Attraction a(String id) {
        return park.attractions().findById(id).orElseThrow();
    }

    @Test
    void validTicketAdmitsUserAndPresenceSurvivesDateChange() {
        assertEquals("ENTRY GRANTED: 1 user(s) entered A01", p.process("ATTRACTION ENTER;A01;T001"));
        assertEquals(List.of("ana@correo.com"), a("A01").getOccupants().stream().map(u -> u.getEmail()).toList());
        p.process("SYSTEM DATE;2027-04-20");
        assertEquals(1, a("A01").getOccupants().size());
    }

    @Test
    void ticketNotValidToday() {
        p.process("SYSTEM DATE;2027-04-14");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        p.process("SYSTEM DATE;2027-04-16");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void noCapacityUnknownAndAlreadyInside() {
        assertTrue(p.process("ATTRACTION ENTER;A02;T002").startsWith("ENTRY GRANTED"));
        assertTrue(p.process("ATTRACTION ENTER;A02;T001").startsWith("ERROR"));
        assertEquals(1, a("A02").getOccupants().size());
        assertTrue(p.process("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION ENTER;A01;T999").startsWith("ERROR"));
        p.process("ATTRACTION ENTER;A01;T001");
        assertTrue(p.process("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertEquals(1, a("A01").getOccupants().size());
    }
}
