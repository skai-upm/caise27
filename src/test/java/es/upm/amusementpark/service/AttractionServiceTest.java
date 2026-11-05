package es.upm.amusementpark.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttractionServiceTest {

    private Park park;
    private CommandProcessor p;

    @BeforeEach
    void setUp() {
        park = Park.getInstance();
        park.start(new UserRepository());
        p = new CommandProcessor(new UserService(park.users(), park.groups(), park.tickets()),
                new GroupService(park.users(), park.groups(), park.tickets()),
                new TicketService(park.users(), park.tickets(), park.groups()), new AttractionService(park.attractions()));
    }

    @Test
    void createAndRejectInvalid() {
        assertEquals("ATTRACTION CREATED", p.process("ATTRACTION CREATE;A01;Dragon Rojo;20"));
        Attraction a = park.attractions().findById("A01").orElseThrow();
        assertEquals("Dragon Rojo", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getOccupants().isEmpty());
        for (String bad : List.of("ATTRACTION CREATE;A01;Otra;10", "ATTRACTION CREATE;A02;Torre;0",
                "ATTRACTION CREATE;A02;Torre;-3", "ATTRACTION CREATE;A02;Torre;doce", "ATTRACTION CREATE;;Torre;5")) {
            assertTrue(p.process(bad).startsWith("ERROR"), bad);
        }
        assertEquals(1, park.attractions().findAll().size());
    }

    @Test
    void listEmptyAndOccupied() {
        p.process("ATTRACTION CREATE;A01;Dragon Rojo;20");
        assertEquals("ATTRACTION A01 (Dragon Rojo) 0/20: no users", p.process("ATTRACTION LIST;A01"));
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        User pedro = new User("Pedro", "L", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.attractions().findById("A01").orElseThrow().admitAll(List.of(ana, pedro));
        String out = p.process("ATTRACTION LIST;A01");
        assertEquals(List.of("ATTRACTION A01 (Dragon Rojo) 2/20:", "ana@correo.com", "pedro@correo.com"), out.lines().toList());
        assertEquals(out, p.process("ATTRACTION LIST;A01"));
        assertTrue(p.process("ATTRACTION LIST;A99").startsWith("ERROR"));
    }

    @Test
    void capacityIsNeverExceededAndAdmissionIsAllOrNothing() {
        Attraction a = new Attraction("A", "Mini", 1);
        User u1 = new User("A", "B", "12345678Z", "a@b.com", "Clave1!", "6", "1234567890123456");
        User u2 = new User("C", "D", "87654321X", "c@d.com", "Clave1!", "6", "1234567890123456");
        assertFalse(a.admitAll(List.of(u1, u2)));
        assertTrue(a.getOccupants().isEmpty());
        assertTrue(a.admitAll(List.of(u1)));
        assertFalse(a.admitAll(List.of(u2)));
    }

    @Test
    void userHasNoAttractionReference() {
        assertTrue(Arrays.stream(User.class.getDeclaredFields()).noneMatch(f -> f.getType() == Attraction.class));
    }
}
