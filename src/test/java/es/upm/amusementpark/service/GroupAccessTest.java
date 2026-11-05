package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupAccessTest {

    private Park park;
    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        park = Park.getInstance();
        park.start(new UserRepository());
        p = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()),
                new GroupService(park.users(), park.groups(), park.tickets()),
                new TicketService(park.users(), park.tickets(), park.groups()),
                new AttractionService(park.attractions(), park.tickets()));
        for (String cmd : List.of("SYSTEM DATE;2027-04-01",
                "USER CREATE;Ana;Garcia;12345678Z;ana@correo.com;Clave1!;6;1234567890123456",
                "USER CREATE;Pedro;Lopez;87654321X;pedro@correo.com;Otra2#;6;9876543210987654",
                "USER CREATE;Luis;Martin;00000011B;luis@correo.com;Clave1!;6;1111222233334444",
                "GROUP CREATE;G01;Familia", "GROUP ADD;G01;ana@correo.com", "GROUP ADD;G01;pedro@correo.com",
                "GROUPTICKET CREATE;TG01;G01;2027-04-15", "TICKET CREATE;T003;luis@correo.com;2027-04-15",
                "ATTRACTION CREATE;A01;Dragon Rojo;20", "ATTRACTION CREATE;A02;Mini;1", "SYSTEM DATE;2027-04-15")) {
            assertTrue(!p.process(cmd).startsWith("ERROR"), cmd);
        }
    }

    private Attraction a(String id) {
        return park.attractions().findById(id).orElseThrow();
    }

    @Test
    void allMembersEnterTogether() {
        assertEquals("ENTRY GRANTED: 2 user(s) entered A01", p.process("ATTRACTION ENTER;A01;TG01"));
        assertEquals(2, a("A01").getOccupants().size());
    }

    @Test
    void insufficientCapacityAdmitsNobody() {
        assertTrue(p.process("ATTRACTION ENTER;A02;TG01").startsWith("ERROR"));
        assertTrue(a("A02").getOccupants().isEmpty());
    }

    @Test
    void wrongDateAdmitsNobody() {
        p.process("SYSTEM DATE;2027-04-16");
        assertTrue(p.process("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(a("A01").getOccupants().isEmpty());
    }

    @Test
    void exitAndStatus() {
        p.process("ATTRACTION ENTER;A01;TG01");
        p.process("ATTRACTION ENTER;A01;T003");
        assertEquals("USER LEFT ATTRACTION", p.process("ATTRACTION EXIT;A01;ana@correo.com"));
        assertTrue(p.process("ATTRACTION EXIT;A01;ana@correo.com").startsWith("ERROR"));
        assertTrue(p.process("ATTRACTION EXIT;A99;pedro@correo.com").startsWith("ERROR"));
        assertEquals(List.of("ATTRACTION A01 - Dragon Rojo", "Capacity: 20", "Inside: 2", "Available: 18",
                "Users: pedro@correo.com, luis@correo.com"), p.process("ATTRACTION STATUS;A01").lines().toList());
        assertTrue(p.process("ATTRACTION STATUS;A02").contains("Users: none"));
        assertTrue(p.process("ATTRACTION STATUS;A99").startsWith("ERROR"));
    }
}
